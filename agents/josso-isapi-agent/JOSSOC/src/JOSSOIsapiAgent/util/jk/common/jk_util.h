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
 * Description: Various utility functions                                  *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Author:      Henri Gomez <hgomez@apache.org>                            *
 * Author:      Rainer Jung <rjung@apache.org>                             *
 * Version:     $Revision: 704015 $                                          *
 ***************************************************************************/
#ifndef _JK_UTIL_H
#define _JK_UTIL_H

#include "jk_global.h"
#include "jk_logger.h"
#include "jk_map.h"
#include "jk_pool.h"

#define JK_SLEEP_DEF     (100)

const char *jk_get_bool(int v);

int jk_get_bool_code(const char *v, int def);

void jk_sleep(int ms);

void jk_set_time_fmt(jk_logger_t *l, const char *jk_log_fmt);

int jk_parse_log_level(const char *level);

int jk_open_file_logger(jk_logger_t **l, const char *file, int level);

int jk_attach_file_logger(jk_logger_t **l, int fd, int level);

int jk_close_file_logger(jk_logger_t **l);

int jk_log(jk_logger_t *l,
           const char *file, int line, const char *funcname, int level,
           const char *fmt, ...);

const char *jk_get_lb_session_cookie(jk_map_t *m, const char *wname, const char *def);

const char *jk_get_lb_session_path(jk_map_t *m, const char *wname, const char *def);

int jk_get_lb_factor(jk_map_t *m, const char *wname);

int jk_get_distance(jk_map_t *m, const char *wname);

int jk_get_is_sticky_session(jk_map_t *m, const char *wname);

int jk_get_is_sticky_session_force(jk_map_t *m, const char *wname);

int jk_get_lb_method(jk_map_t *m, const char *wname);

int jk_get_lb_lock(jk_map_t *m, const char *wname);

int jk_file_exists(const char *f);

int jk_is_list_property(const char *prp_name);

int jk_is_path_property(const char *prp_name);

int jk_is_cmd_line_property(const char *prp_name);

int jk_is_unique_property(const char *prp_name);

int jk_is_deprecated_property(const char *prp_name);

int jk_is_valid_property(const char *prp_name);

char **jk_parse_sysprops(jk_pool_t *p, const char *sysprops);

void jk_append_libpath(jk_pool_t *p, const char *libpath);


int jk_get_max_packet_size(jk_map_t *m, const char *wname);

int jk_get_is_read_only(jk_map_t *m, const char *wname);

int is_http_status_fail(unsigned int http_status_fail_num,
                        int *http_status_fail, int status);

int jk_wildchar_match(const char *str, const char *exp, int icase);

#define TC32_BRIDGE_TYPE    32
#define TC33_BRIDGE_TYPE    33
#define TC40_BRIDGE_TYPE    40
#define TC41_BRIDGE_TYPE    41
#define TC50_BRIDGE_TYPE    50

#ifdef AS400

#define S_IFREG _S_IFREG

#ifdef AS400_UTF8

void jk_ascii2ebcdic(char *src, char *dst);
void jk_ebcdic2ascii(char *src, char *dst);

#endif /* AS400_UTF8 */

#endif

/* i5/OS V5R4 need ASCII-EBCDIC conversion before stat() call */
/* added a stat() mapper function, jk_stat, for such purpose */

int jk_stat(const char *f, struct stat * statbuf);

#ifdef __cplusplus
extern "C"
{
#endif                          /* __cplusplus */


#ifdef __cplusplus
}
#endif                          /* __cplusplus */
#endif                          /* _JK_UTIL_H */
