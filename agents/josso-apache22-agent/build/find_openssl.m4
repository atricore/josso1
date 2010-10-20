dnl *********************************************************************************
dnl SYNOPSIS
dnl 
dnl   FIND_OPENSSL
dnl
dnl DESCRIPTION
dnl
dnl   In order for this macro to try auto-discovering OpenSSL, user must use '--enable-openssl' as argument to configure.
dnl	  This macro will check various standard spots for OpenSSL including a user-supplied directory.
dnl   The user can use '--with-openssl-includes=/path/to/ssl/includes' and '--with-openssl-libs=/path/to/ssl/libs', 
dnl	  or '--with-openssl=/path/to/ssl/base' as arguments to configure.
dnl   Before looking in the user-supplied directory (and some other standard directories)
dnl   the script will try to find OpenSSL using pkg-config (user can optionally
dnl   use '--with-pkg-config=/path/to/pkg-config' to specify path to pkg-config).
dnl   
dnl   If OpenSSL is found the include directory gets added to CFLAGS and
dnl   CPPFLAGS as well as '-lssl' & '-lcrypto' gets added to
dnl   LIBS, and the libraries location gets added to LDFLAGS.
dnl   Also, 'WITH_OPENSSL' gets set to '1' for enabling ssl in gsoap.
dnl
dnl *********************************************************************************

AC_DEFUN([FIND_OPENSSL],
[dnl
	AC_ARG_ENABLE(openssl, [  --enable-openssl        Attempt to use OpenSSL for SSL support.],
		[enable_openssl=$enableval], [enable_openssl=no])

	dnl detect OpenSSL
	if test "x${enable_openssl}" != "xno"; then
		SSL_CFLAGS=""
		SSL_LIBS=""
		
		saved_CPPFLAGS="$CPPFLAGS"
		saved_LIBS="$LIBS"
  		saved_LDFLAGS="$LDFLAGS"
  		
  		found_ssl="no"
  		
		AC_ARG_WITH(openssl-includes, [  --with-openssl-includes=PREFIX     Location of OpenSSL includes.],
			with_openssl_includes="$withval", with_openssl_includes="no")
		have_openssl_includes="no"
		if test "x${with_openssl_includes}" != "xno"; then
			AC_MSG_CHECKING(for OpenSSL includes)
			
			CPPFLAGS="$CPPFLAGS -I$with_openssl_includes"
			AC_CHECK_HEADERS(openssl/ssl.h, openssl_includes="yes", openssl_includes="no")
			CPPFLAGS="$saved_CPPFLAGS"
		
			if test "x${openssl_includes}" = "xyes"; then
				have_openssl_includes="yes"
				SSL_CFLAGS="-I$with_openssl_includes"
			fi
		fi
		
		AC_ARG_WITH(openssl-libs, [  --with-openssl-libs=PREFIX         Location of OpenSSL libs.],
			with_openssl_libs="$withval", with_openssl_libs="no")
		have_openssl_libs="no"
		if test "x${with_openssl_libs}" != "xno"; then
			AC_MSG_CHECKING(for OpenSSL libraries)
			
			case $with_openssl_libs in
				""|-L*) ;;
				*) with_openssl_libs="-L$with_openssl_libs" ;;
			esac
	
			AC_CHECK_LIB(dl, dlopen, DL_LIBS="-ldl", DL_LIBS="")
			LIBS="$LIBS $with_openssl_libs -lssl -lcrypto $DL_LIBS"
			AC_CHECK_LIB(ssl, SSL_library_init, openssl_libs="yes", openssl_libs="no")
			LIBS="$saved_LIBS"
			if test "x${openssl_libs}" = "xyes"; then
				have_openssl_libs="yes"
				SSL_LIBS="$with_openssl_libs -lssl -lcrypto $DL_LIBS"
			fi
		fi
		
		dnl try to find openssl using pkg-config
		if test "x${have_openssl_includes}" = "xno" -o "x${have_openssl_libs}" = "xno"; then
			AC_ARG_WITH(pkg-config, [  --with-pkg-config=PATH         Use pkg-config specified.],
				[withpkgconfig="$withval"], withpkgconfig=[])
			AC_PATH_PROG(PKGCONFIG, [$withpkgconfig pkg-config])
			if test "x${PKGCONFIG}" != "x" ; then
				SSL_LIBS="`$PKGCONFIG --libs-only-l openssl 2>&1`"
				if test $? -eq 0; then
					have_openssl_libs="yes"
				   	dnl trim whitespaces
				   	PKGC_SSL_CFLAGS="`$PKGCONFIG --cflags-only-I openssl | sed 's/^ *//;s/ *$//'`"
				   	if test "x${PKGC_SSL_CFLAGS}" != "x"; then
				   		have_openssl_includes="yes"
				   		SSL_CFLAGS="$PKGC_SSL_CFLAGS"
				   	fi
				fi
			fi
		fi
		
		dnl try to find openssl in standard directories
		if test "x${have_openssl_includes}" = "xno" -o "x${have_openssl_libs}" = "xno"; then
			AC_ARG_WITH(openssl, [  --with-openssl=PREFIX     Location of OpenSSL base dir.],
					with_openssl="$withval", with_openssl="no")
			for dir in ${with_openssl} /usr/local/ssl /usr/lib/ssl /usr/ssl /usr/pkg /usr/local /usr /opt/misc /opt/csw /opt/sfw; do
		    	ssldir="$dir"
		    	if test -f "$dir/include/openssl/ssl.h"; then
		      		printf "OpenSSL found in $ssldir\n";
		      		have_openssl_includes="yes";
		      		SSL_CFLAGS="-I$ssldir/include -I$ssldir/include/openssl";
		      		if test "x${have_openssl_libs}" = "xno"; then
		      			have_openssl_libs="yes"
		      			SSL_LIBS="-L$ssldir/lib -lssl -lcrypto -ldl -lz"
		      		fi
		      		break;
		    	fi
		    	if test -f "$dir/include/ssl.h"; then
		      		printf "OpenSSL found in $ssldir\n";
		      		have_openssl_includes="yes";
		      		SSL_CFLAGS="-I$ssldir/include/"
		      		if test "x${have_openssl_libs}" = "xno"; then
		      			have_openssl_libs="yes"
		      			SSL_LIBS="-L$ssldir/lib -lssl -lcrypto"
		      		fi
		      		break;
		    	fi
		  	done
		fi

		if test "x${have_openssl_includes}" = "xyes" -a "x${have_openssl_libs}" = "xyes"; then
			AC_DEFINE(WITH_OPENSSL, 1, [Define if you use OpenSSL to support SSL])
			APR_ADDTO(CPPFLAGS, [$SSL_CFLAGS])
    		APR_ADDTO(INCLUDES, [$SSL_CFLAGS])
    		APR_ADDTO(LDFLAGS, [$SSL_LIBS])
    		AC_SUBST(SSL_CFLAGS)
			AC_SUBST(SSL_LIBS)
		else
			AC_MSG_ERROR([Cannot find openssl library. Please install openssl or specify openssl base directory with --with-openssl, or openssl include and lib directory with --with-openssl-includes=(dir) and --with-openssl-libs=(dir).])
		fi
	
	fi
])
