#include "FormField.hpp"
#include "FormDataParser.hpp"

mime::FormField::FormField() {
    type = 0;
    FormFieldContent = NULL;

    FormFieldContentLength = 0;

}

mime::FormField::~FormField() {

    if (FormFieldContent) {
        delete FormFieldContent;
    }

    if (type == FileType) {
        if (file.is_open()) {
            file.close();
            remove((TempDir + "/" + TempFile).c_str());
        }

    }

}

void mime::FormField::SetType(int type) {
    if ((type == TextType) || (type == FileType)) {
        this->type = type;
    } else {
        throw mime::Exception("Trying to set type of field, but type is incorrect.");
    }

}

int mime::FormField::GetType() {
    if (type > 0) {
        return type;
    } else {
        throw mime::Exception("Trying to get type of field, but no type was set.");
    }
}

void mime::FormField::AcceptSomeData(char *data, long length) {
    if (type == TextType) {
        if (FormFieldContent == NULL) {
            FormFieldContent = new char[length + 1];
        } else {
            FormFieldContent = (char*) realloc(FormFieldContent, FormFieldContentLength + length + 1);
        }

        memcpy(FormFieldContent + FormFieldContentLength, data, length);
        FormFieldContent[FormFieldContentLength + length] = 0;
    } else if (type == FileType) {
        if (WhereToStoreUploadedFiles == FormDataParser::StoreUploadedFilesInFilesystem) {
            if (TempDir.length() > 0) {
                if (!file.is_open()) {
                    int i = 1;
                    std::ifstream testfile;
                    std::string tempfile;
                    do {
                        if (testfile.is_open()) {
                            testfile.close();
                        }

                        std::stringstream ss;
                        ss << "mime_Temp_" << i;
                        TempFile = ss.str();

                        tempfile = TempDir + "/" + TempFile;

                        testfile.open(tempfile.c_str(), std::ios::in);
                        i++;
                    } while (testfile.is_open());

                    file.open(tempfile.c_str(), std::ios::out | std::ios::binary | std::ios_base::trunc);
                }

                if (file.is_open()) {
                    file.write(data, length);
                    file.flush();
                } else {
                    throw Exception(std::string("Cannot write to file ") + TempDir + "/" + TempFile);
                }
            } else {
                throw mime::Exception("Trying to AcceptSomeData for a file but no TempDir is set.");
            }
        } else { // If files are stored in memory
            if (FormFieldContent == NULL) {
                FormFieldContent = new char[length];
            } else {
                FormFieldContent = (char*) realloc(FormFieldContent, FormFieldContentLength + length);
            }
            memcpy(FormFieldContent + FormFieldContentLength, data, length);
            FormFieldContentLength += length;
        }
    } else {
        throw mime::Exception("Trying to AcceptSomeData but no type was set.");
    }
}

void mime::FormField::SetTempDir(std::string dir) {
    TempDir = dir;
}

unsigned long mime::FormField::GetFileContentSize() {
    if (type == 0) {
        throw mime::Exception("Trying to get file content size, but no type was set.");
    } else {
        if (type == FileType) {
            if (WhereToStoreUploadedFiles == FormDataParser::StoreUploadedFilesInMemory) {
                return FormFieldContentLength;
            } else {
                throw mime::Exception("Trying to get file content size, but uploaded files are stored in filesystem.");
            }
        } else {
            throw mime::Exception("Trying to get file content size, but the type is not file.");
        }
    }
}

char * mime::FormField::GetFileContent() {
    if (type == 0) {
        throw mime::Exception("Trying to get file content, but no type was set.");
    } else {
        if (type == FileType) {
            if (WhereToStoreUploadedFiles == FormDataParser::StoreUploadedFilesInMemory) {
                return FormFieldContent;
            } else {
                throw mime::Exception("Trying to get file content, but uplaoded files are stored in filesystem.");
            }
        } else {
            throw mime::Exception("Trying to get file content, but the type is not file.");
        }
    }
}

std::string mime::FormField::GetTextTypeContent() {
    if (type == 0) {
        throw mime::Exception("Trying to get text content of the field, but no type was set.");
    } else {
        if (type != TextType) {
            throw mime::Exception("Trying to get content of the field, but the type is not text.");
        } else {
            if (FormFieldContent == NULL) {
                return std::string();
            } else {
                return std::string(FormFieldContent);
            }
        }
    }
}

std::string mime::FormField::GetTempFileName() {
    if (type == 0) {
        throw mime::Exception("Trying to get file temp name, but no type was set.");
    } else {
        if (type == FileType) {
            if (WhereToStoreUploadedFiles == FormDataParser::StoreUploadedFilesInFilesystem) {
                return std::string(TempDir + "/" + TempFile);
            } else {
                throw mime::Exception("Trying to get file temp name, but uplaoded files are stored in memory.");
            }
        } else {
            throw mime::Exception("Trying to get file temp name, but the type is not file.");
        }
    }
}

std::string mime::FormField::GetFileName() {
    if (type == 0) {
        throw mime::Exception("Trying to get file name, but no type was set.");
    } else {
        if (type == FileType) {
            return FileName;
        } else {
            throw mime::Exception("Trying to get file name, but the type is not file.");
        }
    }
}

void mime::FormField::SetFileName(std::string name) {
    FileName = name;

}

void mime::FormField::SetUploadedFilesStorage(int where) {
    WhereToStoreUploadedFiles = where;
}

void mime::FormField::SetFileContentType(std::string type) {
    FileContentType = type;
}

std::string mime::FormField::GetFileMimeType() {
    if (type == 0) {
        throw mime::Exception("Trying to get mime type of file, but no type was set.");
    } else {
        if (type != FileType) {
            throw mime::Exception("Trying to get mime type of the field, but the type is not File.");
        } else {
            return std::string(FileContentType);
        }
    }
}