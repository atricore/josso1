#ifndef _FORM_FIELD_H
#define	_FORM_FIELD_H

#include "Exception.hpp"
#include <iostream>
#include <fstream>
#include <stdlib.h>
#include <string.h>
#include <sstream>

namespace mime {

    class FormField {
    public:
        static const int TextType = 1, FileType = 2;

        FormField();
        virtual ~FormField();

        void SetType(int type);
        int GetType();

        void AcceptSomeData(char *data, long length);


        // File functions
        void SetUploadedFilesStorage(int where);
        void SetTempDir(std::string dir);

        void SetFileName(std::string name);
        std::string GetFileName();

        void SetFileContentType(std::string type);
        std::string GetFileMimeType();

        char * GetFileContent();
        unsigned long GetFileContentSize();

        std::string GetTempFileName();

        // Text field operations
        std::string GetTextTypeContent();




    private:
        unsigned long FormFieldContentLength;

        int WhereToStoreUploadedFiles;

        std::string TempDir, TempFile;
        std::string FileContentType, FileName;

        int type;
        char * FormFieldContent;
        std::ofstream file;

    };
}
#endif	

