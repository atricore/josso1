/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/***************************************************************************
 * Description: JOSSO version header file                                     *
 * Version:     $Revision: 708688 $                                           *
 ***************************************************************************/

#ifndef __JOSSO_VERSION_H
#define __JOSSO_VERSION_H

/************** START OF AREA TO MODIFY BEFORE RELEASING *************/
#define JOSSO_VERMAJOR     1
#define JOSSO_VERMINOR     8
#define JOSSO_VERFIX       10
#define JOSSO_VERSTRING    "1.8.10"

/* set JOSSO_VERISRELEASE to 1 when release (do not forget to commit!) */
#define JOSSO_VERISRELEASE 1
/* Beta number */
#define JOSSO_VERBETA      0
#define JOSSO_BETASTRING   "0"
/* Release candidate */
#define JOSSO_VERRC        0
#define JOSSO_RCSTRING     "0"
/* Source Control Revision as a suffix, e.g. "-r12345" */
#define JOSSO_REVISION "-"

/************** END OF AREA TO MODIFY BEFORE RELEASING *************/

#if !defined(PACKAGE)
#if defined(JOSSO_ISAPI)
#define PACKAGE "JOSSOIsapi"
#elif defined(JOSSO_NSAPI)
#define PACKAGE "nsapi_redirector"
#else
#define PACKAGE "JOSSOIsapiPlugin"
#endif
#endif

/* Build JOSSO_EXPOSED_VERSION and JOSSO_VERSION */
#define JOSSO_EXPOSED_VERSION_INT PACKAGE "/" JOSSO_VERSTRING

#if (JOSSO_VERBETA != 0)
#define JOSSO_EXPOSED_VERSION JOSSO_EXPOSED_VERSION_INT "-beta-" JOSSO_BETASTRING
#else
#undef JOSSO_VERBETA
#define JOSSO_VERBETA 255
#if (JOSSO_VERRC != 0)
#define JOSSO_EXPOSED_VERSION JOSSO_EXPOSED_VERSION_INT "-rc-" JOSSO_RCSTRING
#elif (JOSSO_VERISRELEASE == 1)
#define JOSSO_EXPOSED_VERSION JOSSO_EXPOSED_VERSION_INT
#else
#define JOSSO_EXPOSED_VERSION JOSSO_EXPOSED_VERSION_INT "-dev" JOSSO_REVISION
#endif
#endif

#define JOSSO_MAKEVERSION(major, minor, fix, beta) (((major) << 24) + ((minor) << 16) + ((fix) << 8) + (beta))

#define JOSSO_VERSION JOSSO_MAKEVERSION(JOSSO_VERMAJOR, JOSSO_VERMINOR, JOSSO_VERFIX, JOSSO_VERBETA)

#endif /* __JOSSO_VERSION_H */

#define VERSION_STRING "JOSSOC/IsapiAgent/" JOSSO_EXPOSED_VERSION

//----------------------------------------------------------------------------------

#define MAX_HEADER_SIZE	8192

#ifndef JOSSO_ISAPI_DEF
#define JOSSO_ISAPI_DEF
#include <JOSSOIsapiAgent/util/jk/common/jk_logger.h>

extern "C" {
#include <JOSSOIsapiAgent/util/jk/common/jk_util.h>
#include <strsafe.h>
}

#endif
