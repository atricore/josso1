#include "Exception.hpp"

mime::Exception::Exception(std::string error) {
    Error = error;
}

mime::Exception::Exception(const mime::Exception& orig) {
    Error = orig.Error;
}

mime::Exception::~Exception() {

}

std::string mime::Exception::GetError() {
    return Error;
}