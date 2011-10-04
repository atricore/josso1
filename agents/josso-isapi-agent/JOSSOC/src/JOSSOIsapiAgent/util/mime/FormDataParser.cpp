#include "FormDataParser.hpp"

std::map<std::string, mime::FormField *> mime::FormDataParser::GetFormFieldsMap() {
    return FormFields;
}

mime::FormDataParser::FormDataParser() {
    DataCollector = NULL;
    DataCollectorLength = 0;
    _HeadersOfTheFormFieldAreProcessed = false;
    CurrentStatus = Status_LookingForStartingBoundary;

    MaxDataCollectorLength = 16 * 1024 * 1024; // 16 Mb default data collector size.

    SetUploadedFilesStorage(StoreUploadedFilesInFilesystem);
}

mime::FormDataParser::~FormDataParser() {
    std::map<std::string, FormField *>::iterator it;
    for (it = FormFields.begin(); it != FormFields.end(); it++) {
        delete it->second;
    }

    if (DataCollector) {
        delete DataCollector;
    }
}

void mime::FormDataParser::SetContentType(const std::string type) {
    if (type.find("multipart/form-data;") != 0) {
        throw mime::Exception(std::string("Content type is not \"multipart/form-data\"\nIt is \"") + type + std::string("\""));
    }


    int bp = type.find("boundary=");

    if (bp == std::string::npos) {
        throw mime::Exception(std::string("Cannot find boundary in Content-type: \"") + type + std::string("\""));
    }

    Boundary = std::string("--") + type.substr(bp + 9, type.length() - bp);
}

void mime::FormDataParser::AcceptSomeData(const char *data, const long length) {
    if (Boundary.length() > 0) {
        // Append data to existing accumulator
        if (DataCollector == NULL) {
            DataCollector = new char[length];
            memcpy(DataCollector, data, length);
            DataCollectorLength = length;
        } else {
            DataCollector = (char*) realloc(DataCollector, DataCollectorLength + length);
            memcpy(DataCollector + DataCollectorLength, data, length);
            DataCollectorLength += length;
        }

        if (DataCollectorLength > MaxDataCollectorLength) {
            throw Exception("Maximum data collector length reached.");
        }

        _ProcessData();
    } else {
        throw mime::Exception("Accepting data, but content type was not set.");
    }

}

void mime::FormDataParser::_ProcessData() {
    // If some data left after truncate, process it right now.
    // Do not wait for AcceptSomeData called again
    bool NeedToRepeat;

    do {
        NeedToRepeat = false;
        switch (CurrentStatus) {
            case Status_LookingForStartingBoundary:
                if (FindStartingBoundaryAndTruncData()) {
                    CurrentStatus = Status_ProcessingHeaders;
                    NeedToRepeat = true;
                }
                break;

            case Status_ProcessingHeaders:
                if (WaitForHeadersEndAndParseThem()) {
                    CurrentStatus = Status_ProcessingContentOfTheFormField;
                    NeedToRepeat = true;
                }
                break;

            case Status_ProcessingContentOfTheFormField:
                if (ProcessContentOfTheFormField()) {
                    CurrentStatus = Status_LookingForStartingBoundary;
                    NeedToRepeat = true;
                }
                break;
        }
    } while (NeedToRepeat);
}

bool mime::FormDataParser::ProcessContentOfTheFormField() {
    long BoundaryPosition = BoundaryPositionInDataCollector();
    long DataLengthToSendToFormField;
    if (BoundaryPosition >= 0) {
        // 2 is the \r\n before boundary we do not need them
        DataLengthToSendToFormField = BoundaryPosition - 2;
    } else {
        // We need to save +2 chars for \r\n chars before boundary
        DataLengthToSendToFormField = DataCollectorLength - (Boundary.length() + 2);
    }

    if (DataLengthToSendToFormField > 0) {
        FormFields[ProcessingFormFieldName]->AcceptSomeData(DataCollector, DataLengthToSendToFormField);
        TruncateDataCollectorFromTheBeginning(DataLengthToSendToFormField);
    }

    if (BoundaryPosition >= 0) {
        CurrentStatus = Status_LookingForStartingBoundary;
        return true;
    } else {
        return false;
    }
}

bool mime::FormDataParser::WaitForHeadersEndAndParseThem() {
    for (int i = 0; i < DataCollectorLength - 3; i++) {
        if ((DataCollector[i] == 13) && (DataCollector[i + 1] == 10) && (DataCollector[i + 2] == 13) && (DataCollector[i + 3] == 10)) {
            long headers_length = i;
            char *headers = new char[headers_length + 1];
            memset(headers, 0, headers_length + 1);
            memcpy(headers, DataCollector, headers_length);

            _ParseHeaders(std::string(headers));

            TruncateDataCollectorFromTheBeginning(i + 4);

            delete headers;

            return true;
        }
    }
    return false;
}

void mime::FormDataParser::SetUploadedFilesStorage(int where) {
    WhereToStoreUploadedFiles = where;
}

void mime::FormDataParser::SetTempDirForFileUpload(std::string dir) {
    TempDirForFileUpload = dir;
}

void mime::FormDataParser::_ParseHeaders(std::string headers) {
    // Check if it is form data
    if (headers.find("Content-Disposition: form-data;") == std::string::npos) {
        throw Exception(std::string("Accepted headers of field does not contain \"Content-Disposition: form-data;\"\nThe headers are: \"") + headers + std::string("\""));
    }

    // Find name
    long name_pos = headers.find("name=\"");
    if (name_pos == std::string::npos) {
        throw Exception(std::string("Accepted headers of field does not contain \"name=\".\nThe headers are: \"") + headers + std::string("\""));
    } else {
        long name_end_pos = headers.find("\"", name_pos + 6);
        if (name_end_pos == std::string::npos) {
            throw Exception(std::string("Cannot find closing quote of \"name=\" attribute.\nThe headers are: \"") + headers + std::string("\""));
        } else {
            ProcessingFormFieldName = headers.substr(name_pos + 6, name_end_pos - (name_pos + 6));
            FormFields[ProcessingFormFieldName] = new FormField();
        }


        // find filename if exists
        long filename_pos = headers.find("filename=\"");
        if (filename_pos == std::string::npos) {
            FormFields[ProcessingFormFieldName]->SetType(FormField::TextType);
        } else {
            FormFields[ProcessingFormFieldName]->SetType(FormField::FileType);
            FormFields[ProcessingFormFieldName]->SetTempDir(TempDirForFileUpload);
            FormFields[ProcessingFormFieldName]->SetUploadedFilesStorage(WhereToStoreUploadedFiles);

            long filename_end_pos = headers.find("\"", filename_pos + 10);
            if (filename_end_pos == std::string::npos) {
                throw Exception(std::string("Cannot find closing quote of \"filename=\" attribute.\nThe headers are: \"") + headers + std::string("\""));
            } else {
                std::string filename = headers.substr(filename_pos + 10, filename_end_pos - (filename_pos + 10));
                FormFields[ProcessingFormFieldName]->SetFileName(filename);
            }

            // find Content-Type if exists
            long content_type_pos = headers.find("Content-Type: ");
            if (content_type_pos != std::string::npos) {
                long content_type_end_pos = 0;
                for (int i = content_type_pos + 14; (i < headers.length()) && (!content_type_end_pos); i++) {
                    if ((headers[i] == ' ') || (headers[i] == 10) || (headers[i] == 13)) {
                        content_type_end_pos = i - 1;
                    }
                }
                std::string content_type = headers.substr(content_type_pos + 14, content_type_end_pos - (content_type_pos + 14));
                FormFields[ProcessingFormFieldName]->SetFileContentType(content_type);
            }


        }

    }


}

void mime::FormDataParser::SetMaxCollectedDataLength(long max) {
    MaxDataCollectorLength = max;
}

void mime::FormDataParser::TruncateDataCollectorFromTheBeginning(long n) {
    long TruncatedDataCollectorLength = DataCollectorLength - n;

    char *tmp = DataCollector;

    DataCollector = new char[TruncatedDataCollectorLength];
    memcpy(DataCollector, tmp + n, TruncatedDataCollectorLength);

    DataCollectorLength = TruncatedDataCollectorLength;

    delete tmp;

}

long mime::FormDataParser::BoundaryPositionInDataCollector() {
    const char *b = Boundary.c_str();
    int bl = Boundary.length();
    if (DataCollectorLength >= bl) {
        bool found = false;
        for (int i = 0; (i <= DataCollectorLength - bl) && (!found); i++) {
            found = true;
            for (int j = 0; (j < bl) && (found); j++) {
                if (DataCollector[i + j] != b[j]) {
                    found = false;
                }
            }

            if (found) {
                return i;
            }
        }
    }
    return -1;

}

bool mime::FormDataParser::FindStartingBoundaryAndTruncData() {
    long n = BoundaryPositionInDataCollector();
    if (n >= 0) {
        TruncateDataCollectorFromTheBeginning(n + Boundary.length());
        return true;
    } else {
        return false;
    }
}