
#ifndef _FORM_DATA_PARSER_H
#define	_FORM_DATA_PARSER_H

#include <iostream>
#include <string>
#include <map>
#include "Exception.hpp"
#include "FormField.hpp"
#include <string.h>
#include <stdlib.h>

namespace mime {

    class FormDataParser {
    public:
        static const int StoreUploadedFilesInFilesystem = 1, StoreUploadedFilesInMemory = 2;


        FormDataParser();
        ~FormDataParser();

        void SetContentType(const std::string type);

        void AcceptSomeData(const char *data, const long length);

        

        void SetMaxCollectedDataLength(long max);
        void SetTempDirForFileUpload(std::string dir);
        void SetUploadedFilesStorage(int where);

        std::map<std::string, FormField *> GetFormFieldsMap();

    private:
        int WhereToStoreUploadedFiles;
        
        std::map<std::string, FormField *> FormFields;

        std::string TempDirForFileUpload;
        int CurrentStatus;

        // Work statuses
        static int const Status_LookingForStartingBoundary = 1;
        static int const Status_ProcessingHeaders = 2;
        static int const Status_ProcessingContentOfTheFormField = 3;

        std::string Boundary;
        std::string ProcessingFormFieldName;
        bool _HeadersOfTheFormFieldAreProcessed;
        long ContentLength;
        char *DataCollector;
        long DataCollectorLength, MaxDataCollectorLength;
        bool FindStartingBoundaryAndTruncData();
        void _ProcessData();
        void _ParseHeaders(std::string headers);
        bool WaitForHeadersEndAndParseThem();
        void TruncateDataCollectorFromTheBeginning(long n);
        long BoundaryPositionInDataCollector();
        bool ProcessContentOfTheFormField();
    };
}

#endif

