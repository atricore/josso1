#ifndef _EXCEPTION_H
#define	_EXCEPTION_H

#include <string>
#include <iostream>
#include <errno.h>


namespace mime {

    class Exception {
    public:
        Exception(std::string error);
        Exception(const Exception& orig);
        virtual ~Exception();
        
        std::string GetError();
     
    private:
        std::string Error;

    };
}

#endif

