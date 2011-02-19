/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * A hash table allocated&maintained in an RMM memory structure, so it can be used 
 * in shared memory
 *
 * Based on apr_hash.h
 * 
 *  Created on: 02/08/2010
 *      Author: Alex Wulms
 */

#ifndef RMM_HASH_H_
#define RMM_HASH_H_

#include <apr_general.h>
#include <apr_rmm.h>
#include <apr_hash.h>

#define RMM_OFF_NULL 0

#define RMM_OFF_T_DECLARE(type) typedef apr_rmm_off_t rmm_##type##_rmm_off_t
#define RMM_OFF_T(type) rmm_##type##_rmm_off_t

RMM_OFF_T_DECLARE(rmm_hash_index_t);
RMM_OFF_T_DECLARE(rmm_hash_t);

#define APR_RMM_ADDR_GET(type, rmm, offset) ((type *)apr_rmm_addr_get(rmm, offset))

/**
 * Create a hash table
 * @param rmm: the relocatable memory (structure) in which the hash table must be created
 * @return: the offset of the table in the rmm or RMM_OFF_NULL if the table can not be allocated
 */
APR_DECLARE(RMM_OFF_T(rmm_hash_t)) rmm_hash_make(apr_rmm_t *rmm);

/**
 * Create a hash table with a custom hash functon
 * @param rmm: the relocatable memory (structure) in which the hash table must be created
 * @param hash_func: the custom hash function
 * @return: the offset of the table in the rmm or RMM_OFF_NULL if the table can not be allocated
 */
APR_DECLARE(RMM_OFF_T(rmm_hash_t)) rmm_hash_make_custom(apr_rmm_t *rmm,
                                               apr_hashfunc_t hash_func);

/**
 * Associate a value with a key in a hash table
 * @param rmm The relocatable memory structure in which the hash table exists
 * @param ht The (offset of the) hash table in the rmm
 * @param key The (offset of the) key in the rmm. The key must exist in the same rmm as the hash table.
 *            The key will not be copied into the hash table. In stead, a reference to the key will
 *            be stored.
 * @param klen Length of the key. Can be APR_HASH_KEY_STRING to use the string length. See apr_hash.h for
 *   furher details (rmm_hash uses same hash function as apr_hash)
 * @param val The (offset of the) value in the rmm. Like the key, the rmm must exist in the same rmm as
 *            the hash table. 
 * @return NULL if the key is new. Otherwise the (RMM offset of the) old value. It is the responsability of the
 *            invoking application to free the old value from the RMM memory if it is no longer required
 * @remark When value RMM_OFF_NULL is passed, the hash entry itself is deleted but not the key nor the value 
 */
APR_DECLARE(apr_rmm_off_t) rmm_hash_set(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_t) ht,
                               apr_rmm_off_t key,
                               apr_ssize_t klen,
                               apr_rmm_off_t val);


/**
 * Look up the value associated with a key in a hash table.
 * @param rmm The relocatable memory structure in which the hash table exists
 * @param ht The (offset of the) hash table in the rmm
 * @param key A pointer to the key in the (local) address space of the caller.
 * @param klen Length of the key. Can be APR_HASH_KEY_STRING to use the string length. See apr_hash.h for
 *   furher details (rmm_hash uses same hash function as apr_hash)
 * @return Returns NULL if the key is not present.
 */

APR_DECLARE(apr_rmm_off_t) rmm_hash_get(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_t) ht,
                                 const void *key,
                                 apr_ssize_t klen);

/**
 * Start iterating over the entries in a hash table.
 * @param allocate_hi If false, an internal non-thread-safe iterator is used
 *                    Otherwise, a new iterator is allocated from the rmm
 * @param rmm The relocatable memory structure in which the hash table exists
 *            and/or from which to allocate the RMM_OFF_T(rmm_hash_index_t) iterator. 
 * @param ht  The (offset of the) hash table in the rmm
 * @return The (offset of the) hash index/iterator
 * @remark  There is no restriction on adding or deleting hash entries during
 * an iteration (although the results may be unpredictable unless all you do
 * is delete the current entry) and multiple iterations can be in
 * progress at the same time.
 */
/**
 * @example
 *
 * <PRE>
 *
 * int sum_values(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_t) ht)
 * {
 *     RMM_OFF_T(rmm_hash_index_t) hi;
 *     apr_rmm_off_t val;
 *     int sum = 0;
 *     for (hi = rmm_hash_first(0, rmm, ht); hi; hi = rmm_hash_next(rmm, hi)) {
 *         rmm_hash_this(rmm, hi, NULL, NULL, &val);
 *         sum += *(APR_RMM_ADDR_GET(int, rmm, val));
 *     }
 *     return sum;
 * }
 * </PRE>
 */
APR_DECLARE(RMM_OFF_T(rmm_hash_index_t)) rmm_hash_first(int allocate_hi, apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_t) ht);

/**
 * Continue iterating over the entries in a hash table.
 * @param rmm The relocatable memory structure in which the hash table exists
 * @param hi The iteration state
 * @return the RMM offset of the updated iteration state.  RMM_OFF_NULL if there are no more
 *         entries.
 */
APR_DECLARE(RMM_OFF_T(rmm_hash_index_t)) rmm_hash_next(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_index_t)hi);


/**
 * Get the current entry's details from the iteration state.
 * @param rmm The relocatable memory structure in which the hash table exists
 * @param hi The iteration state
 * @param key Return pointer for the RMM offset of the key.
 * @param klen Return pointer for the key length.
 * @param val Return pointer for the RMM offset of the associated value.
 * @remark The return pointers should point to a variable that will be set to the
 *         corresponding data, or they may be NULL if the data isn't interesting.
 */
APR_DECLARE(void) rmm_hash_this(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_index_t) hi,
								apr_rmm_off_t *key,
                                apr_ssize_t *klen,
                                apr_rmm_off_t *val);

/**
 * Get the number of key/value pairs in the hash table.
 * @param rmm The relocatable memory structure in which the hash table exists
 * @param ht The hash table
 * @return The number of key/value pairs in the hash table.
 */
APR_DECLARE(unsigned int) rmm_hash_count(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_t) ht);
                             
/**
 * Clear any key/value pairs in the hash table.
 * @param rmm The relocatable memory structure in which the hash table exists
 * @param free_keys If true, free the memory in the RMM used by the keys
 * @param free_values If true, free the memory in the RMM used by the values
 * @param ht The hash table
 */

APR_DECLARE(void) rmm_hash_clear(apr_rmm_t *rmm, int free_keys, int free_values, RMM_OFF_T(rmm_hash_t) ht);

#endif /* RMM_HASH_H_ */
