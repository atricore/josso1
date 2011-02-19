## This is the shared library to be built
lib_LTLIBRARIES = libmod_auth_josso.la
## Define the source file for the module
libmod_auth_josso_la_SOURCES = mod_auth_josso.c
libmod_auth_josso_la_LIBADD  = ../client/libssoidentityprovider.la ../rmm_hash/librmmhash.la

## Define that an include directory is required.
INCLUDES = -I@apache_include_dir@ -I@apr_include_dir@ -I../client -I../rmm_hash


