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
 * A hash table allocated/maintained in an RMM memory structure, so it can be used 
 * in shared memory
 * 
 * Based on apr_hash.c
 *
 *  Created on: 02/08/2010
 *      Author: Alex Wulms
 */


#if APR_HAVE_STDLIB_H
#include <stdlib.h>
#endif
#if APR_HAVE_STRING_H
#include <string.h>
#endif

#if APR_POOL_DEBUG && APR_HAVE_STDIO_H
#include <stdio.h>
#endif

#include "rmm_hash.h"
/*
 * The internal form of a hash table.
 *
 * The table is an array indexed by the hash of the key; collisions
 * are resolved by hanging a linked list of hash entries off each
 * element of the array. Although this is a really simple design it
 * isn't too bad given that RMM has a low allocation overhead.
 */

typedef struct rmm_hash_entry_t rmm_hash_entry_t;

RMM_OFF_T_DECLARE(rmm_hash_entry_t);

struct rmm_hash_entry_t {
    RMM_OFF_T(rmm_hash_entry_t) next;
    unsigned int      hash;
    apr_rmm_off_t      key;
    apr_ssize_t       klen;
    apr_rmm_off_t     val;
};

/*
 * Data structure for iterating through a hash table.
 *
 * We keep a pointer to the next hash entry here to allow the current
 * hash entry to be freed or otherwise mangled between calls to
 * apr_hash_next().
 */
typedef struct rmm_hash_index_t rmm_hash_index_t;
struct rmm_hash_index_t {
    RMM_OFF_T(rmm_hash_index_t) ht;
    RMM_OFF_T(rmm_hash_entry_t) this, next;
    unsigned int        index;
};

/*
 * The size of the array is always a power of two. We use the maximum
 * index rather than the size so that we can use bitwise-AND for
 * modular arithmetic.
 * The count of hash entries may be greater depending on the chosen
 * collision rate.
 */
RMM_OFF_T_DECLARE(rmm_hash_entry_t_ptr);
typedef struct rmm_hash_t rmm_hash_t;
struct rmm_hash_t {
    RMM_OFF_T(rmm_hash_entry_t_ptr)   array;
    rmm_hash_index_t     iterator;  /* For apr_hash_first(NULL, ...) */
    unsigned int         count, max;
    apr_hashfunc_t       hash_func;
    RMM_OFF_T(rmm_hash_entry_t)  free;  /* List of recycled entries */
};

#define INITIAL_MAX 15 /* tunable == 2^n - 1 */


/*
 * Hash creation functions.
 */

static RMM_OFF_T(rmm_hash_entry_t_ptr) alloc_array(apr_rmm_t *rmm, unsigned int max)
{
   return apr_rmm_malloc(rmm, sizeof(apr_rmm_off_t) * (max + 1));
}


APR_DECLARE(RMM_OFF_T(rmm_hash_t)) rmm_hash_make(apr_rmm_t *rmm)
{
    rmm_hash_t *ht_physical;
    RMM_OFF_T(rmm_hash_t) ht_offset;
    ht_offset = apr_rmm_malloc(rmm, sizeof(rmm_hash_t));
    if (ht_offset == RMM_OFF_NULL) {
    	return RMM_OFF_NULL;
    }
    ht_physical = APR_RMM_ADDR_GET(rmm_hash_t, rmm, ht_offset);
    ht_physical->free = RMM_OFF_NULL;
    ht_physical->count = 0;
    ht_physical->max = INITIAL_MAX;
    ht_physical->array = alloc_array(rmm, ht_physical->max);
    if (ht_physical == RMM_OFF_NULL)
    {
    	apr_rmm_free(rmm, ht_offset);
    	return RMM_OFF_NULL;
    }
    ht_physical->hash_func = apr_hashfunc_default;
    return ht_offset;
}

APR_DECLARE(RMM_OFF_T(rmm_hash_t)) rmm_hash_make_custom(apr_rmm_t *rmm,
                                               apr_hashfunc_t hash_func)
{
    RMM_OFF_T(rmm_hash_t) ht_offset = rmm_hash_make(rmm);
    if (ht_offset == RMM_OFF_NULL) {
    	return RMM_OFF_NULL;
    }
    APR_RMM_ADDR_GET(rmm_hash_t, rmm, ht_offset)->hash_func=hash_func;
    return ht_offset;
}


/*
 * Hash iteration functions.
 */
APR_DECLARE(RMM_OFF_T(rmm_hash_index_t)) rmm_hash_next(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_index_t)hi)
{
	rmm_hash_index_t *hi_physical = APR_RMM_ADDR_GET(rmm_hash_index_t, rmm, hi);
	rmm_hash_t *ht_physical = APR_RMM_ADDR_GET(rmm_hash_t, rmm, hi_physical->ht);
	RMM_OFF_T(rmm_hash_entry_t) *array_physical = APR_RMM_ADDR_GET(RMM_OFF_T(rmm_hash_entry_t), rmm, ht_physical->array);
    hi_physical->this = hi_physical->next;
    
    while (hi_physical->this == RMM_OFF_NULL) {
        if (hi_physical->index > ht_physical->max)
            return RMM_OFF_NULL;

        hi_physical->this = array_physical[hi_physical->index++];
    }
    hi_physical->next = APR_RMM_ADDR_GET(rmm_hash_entry_t, rmm, hi_physical->this)->next;
    return hi;
}

APR_DECLARE(RMM_OFF_T(rmm_hash_index_t)) rmm_hash_first(int allocate_hi, apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_t) ht)
{
    RMM_OFF_T(rmm_hash_index_t) hi;
    rmm_hash_index_t *hi_physical;
    if (allocate_hi) {
        hi = apr_rmm_calloc(rmm, sizeof(rmm_hash_index_t));
        hi_physical = APR_RMM_ADDR_GET(rmm_hash_index_t, rmm, hi);
    }
    else {    	
        hi_physical = &(APR_RMM_ADDR_GET(rmm_hash_t, rmm, ht)->iterator);
        hi = apr_rmm_offset_get(rmm, hi_physical);
    }
    hi_physical->ht = ht;
    hi_physical->index = 0;
    hi_physical->this = RMM_OFF_NULL;
    hi_physical->next = RMM_OFF_NULL;
    return rmm_hash_next(rmm, hi);
}

APR_DECLARE(void) rmm_hash_this(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_index_t) hi,
								apr_rmm_off_t *key,
                                apr_ssize_t *klen,
                                apr_rmm_off_t *val)
{
	rmm_hash_index_t *hi_physical = APR_RMM_ADDR_GET(rmm_hash_index_t, rmm, hi);
	rmm_hash_entry_t *this_physical = APR_RMM_ADDR_GET(rmm_hash_entry_t, rmm, hi_physical->this);
    if (key)  *key  = this_physical->key;
    if (klen) *klen = this_physical->klen;
    if (val)  *val  = this_physical->val;
}

/*
 * Expanding a hash table
 */
static void expand_array(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_t) ht_offset)
{
	RMM_OFF_T(rmm_hash_index_t) hi_offset;
	rmm_hash_index_t *hi_physical;
    RMM_OFF_T(rmm_hash_entry_t_ptr) new_array_offset;
    RMM_OFF_T(rmm_hash_entry_t) *new_array_physical;
    unsigned int new_max;
    rmm_hash_t *ht_physical = APR_RMM_ADDR_GET(rmm_hash_t, rmm, ht_offset);
    new_max = ht_physical->max * 2 + 1;
    new_array_offset = alloc_array(rmm, new_max);
    if (new_array_offset == RMM_OFF_NULL) {
    	return; // Can't allocate memory to expand the array, keep using the old one
    }
    new_array_physical = APR_RMM_ADDR_GET(RMM_OFF_T(rmm_hash_entry_t), rmm, new_array_offset);
    for (hi_offset = rmm_hash_first(0, rmm, ht_offset); hi_offset; hi_offset = rmm_hash_next(rmm, hi_offset)) {
    	hi_physical = APR_RMM_ADDR_GET(rmm_hash_index_t, rmm, hi_offset);
    	rmm_hash_entry_t *this_physical = APR_RMM_ADDR_GET(rmm_hash_entry_t, rmm, hi_physical->this);
        unsigned int i = this_physical->hash & new_max;
        this_physical->next = new_array_physical[i];
        new_array_physical[i] = hi_physical->this;
    }
    apr_rmm_free(rmm, ht_physical->array);
    ht_physical->array = new_array_offset;
    ht_physical->max = new_max;
}


/*
 * This is where we keep the details of the hash function and control
 * the maximum collision rate.
 *
 * If val is non-NULL it creates and initializes a new hash entry if
 * there isn't already one there; it returns an updatable pointer so
 * that hash entries can be removed.
 */
static RMM_OFF_T(rmm_hash_entry_t) *find_entry(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_t) ht_offset,
                                     const void *key_physical,
                                     apr_ssize_t klen,
                                     apr_rmm_off_t val_offset)
{
    RMM_OFF_T(rmm_hash_entry_t) *he_offset_p, he_offset;
    rmm_hash_entry_t *he_physical;
    unsigned int hash;

    rmm_hash_t *ht_physical = APR_RMM_ADDR_GET(rmm_hash_t, rmm, ht_offset);
    hash = ht_physical->hash_func(key_physical, &klen);
    RMM_OFF_T(rmm_hash_entry_t) *array_physical= APR_RMM_ADDR_GET(RMM_OFF_T(rmm_hash_entry_t), rmm, ht_physical->array);

    /* scan linked list */
    for (he_offset_p = &array_physical[hash & ht_physical->max], he_offset = *he_offset_p;
         he_offset != RMM_OFF_NULL; 
         he_offset_p = &he_physical->next, he_offset = *he_offset_p) {
    	he_physical = APR_RMM_ADDR_GET(rmm_hash_entry_t, rmm, he_offset);
        if (he_physical->hash == hash
            && he_physical->klen == klen
            && memcmp(APR_RMM_ADDR_GET(void, rmm, he_physical->key), key_physical, klen) == 0)
            break;
    }
    if (he_offset || val_offset == RMM_OFF_NULL)
        return he_offset_p;

    /* add a new entry for non-NULL values */
    if ((he_offset = ht_physical->free) != RMM_OFF_NULL)
        ht_physical->free = APR_RMM_ADDR_GET(rmm_hash_entry_t, rmm, he_offset)->next;
    else
        he_offset = apr_rmm_calloc(rmm, sizeof(*he_physical));
    he_physical = APR_RMM_ADDR_GET(rmm_hash_entry_t, rmm, he_offset);
    he_physical->next = RMM_OFF_NULL;
    he_physical->hash = hash;
    he_physical->key  = apr_rmm_offset_get(rmm, (void *)key_physical);
    he_physical->klen = klen;
    he_physical->val  = val_offset;
    *he_offset_p = he_offset;
    ht_physical->count++;
    return he_offset_p;
}

/**
 * TODO: Migrate from apr_hash (pool-based) structure to rmm_hash (rmm-based) structure
 */
#if 0
APR_DECLARE(apr_hash_t *) apr_hash_copy(apr_pool_t *pool,
                                        const apr_hash_t *orig)
{
    apr_hash_t *ht;
    apr_hash_entry_t *new_vals;
    unsigned int i, j;

    ht = apr_palloc(pool, sizeof(apr_hash_t) +
                    sizeof(*ht->array) * (orig->max + 1) +
                    sizeof(apr_hash_entry_t) * orig->count);
    ht->pool = pool;
    ht->free = NULL;
    ht->count = orig->count;
    ht->max = orig->max;
    ht->hash_func = orig->hash_func;
    ht->array = (apr_hash_entry_t **)((char *)ht + sizeof(apr_hash_t));

    new_vals = (apr_hash_entry_t *)((char *)(ht) + sizeof(apr_hash_t) +
                                    sizeof(*ht->array) * (orig->max + 1));
    j = 0;
    for (i = 0; i <= ht->max; i++) {
        apr_hash_entry_t **new_entry = &(ht->array[i]);
        apr_hash_entry_t *orig_entry = orig->array[i];
        while (orig_entry) {
            *new_entry = &new_vals[j++];
            (*new_entry)->hash = orig_entry->hash;
            (*new_entry)->key = orig_entry->key;
            (*new_entry)->klen = orig_entry->klen;
            (*new_entry)->val = orig_entry->val;
            new_entry = &((*new_entry)->next);
            orig_entry = orig_entry->next;
        }
        *new_entry = NULL;
    }
    return ht;
}
#endif

APR_DECLARE(apr_rmm_off_t) rmm_hash_get(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_t) ht,
                                 const void *key,
                                 apr_ssize_t klen)
{
    RMM_OFF_T(rmm_hash_entry_t) he_offset;
    he_offset = *find_entry(rmm, ht, key, klen, RMM_OFF_NULL);
    if (he_offset)
        return APR_RMM_ADDR_GET(rmm_hash_entry_t, rmm, he_offset)->val;
    else
        return RMM_OFF_NULL;
}

APR_DECLARE(apr_rmm_off_t) rmm_hash_set(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_t) ht,
                               apr_rmm_off_t key,
                               apr_ssize_t klen,
                               apr_rmm_off_t val)
{
    RMM_OFF_T(rmm_hash_entry_t) *he_offset_p;
    apr_rmm_off_t oldval = RMM_OFF_NULL;
    he_offset_p = find_entry(rmm, ht, apr_rmm_addr_get(rmm, key), klen, val);
    if (*he_offset_p) {
        RMM_OFF_T(rmm_hash_entry_t) old = *he_offset_p;
        rmm_hash_entry_t *old_physical = APR_RMM_ADDR_GET(rmm_hash_entry_t, rmm, old);
    	oldval = old_physical->val;
        rmm_hash_t *ht_physical = APR_RMM_ADDR_GET(rmm_hash_t, rmm, ht);
        if (!val) {
            /* delete entry */
            *he_offset_p = old_physical->next;
            old_physical->next = ht_physical->free;
            ht_physical->free = old;
            --ht_physical->count;
        }
        else {
            /* replace entry */
            old_physical->val = val;
            /* check that the collision rate doesn't become too high */
            if (ht_physical->count > ht_physical->max) {
            	// It's too high. Try to re-hash into a larger array
                expand_array(rmm, ht);
            }
        }
    }
    /* else key not present and val==NULL */
    return oldval;
}

APR_DECLARE(unsigned int) rmm_hash_count(apr_rmm_t *rmm, RMM_OFF_T(rmm_hash_t) ht)
{
    return APR_RMM_ADDR_GET(rmm_hash_t, rmm, ht)->count;
}

APR_DECLARE(void) rmm_hash_clear(apr_rmm_t *rmm, int free_keys, int free_values, RMM_OFF_T(rmm_hash_t) ht)
{
    RMM_OFF_T(rmm_hash_index_t) hi_offset;
    for (hi_offset = rmm_hash_first(0, rmm, ht); hi_offset; hi_offset = rmm_hash_next(rmm, hi_offset)) {
    	rmm_hash_entry_t *this_physical = APR_RMM_ADDR_GET(rmm_hash_entry_t, rmm, APR_RMM_ADDR_GET(rmm_hash_index_t, rmm, hi_offset)->this);
        apr_rmm_off_t key = this_physical->key;
        apr_rmm_off_t val = this_physical->val;
    	rmm_hash_set(rmm, ht, this_physical->key, this_physical->klen, RMM_OFF_NULL);
    	if (free_keys) {
    		apr_rmm_free(rmm, key);
    	}
    	if (free_values) {
    		apr_rmm_free(rmm, val);
    	}
    }
}

/**
 * TODO: Migrate from apr_hash (pool-based) structure to rmm_hash (rmm-based) structure
 */
#if 0
APR_DECLARE(apr_hash_t*) apr_hash_overlay(apr_pool_t *p,
                                          const apr_hash_t *overlay,
                                          const apr_hash_t *base)
{
    return apr_hash_merge(p, overlay, base, NULL, NULL);
}
#endif

/**
 * TODO: Migrate from apr_hash (pool-based) structure to rmm_hash (rmm-based) structure
 */
#if 0
APR_DECLARE(apr_hash_t *) apr_hash_merge(apr_pool_t *p,
                                         const apr_hash_t *overlay,
                                         const apr_hash_t *base,
                                         void * (*merger)(apr_pool_t *p,
                                                     const void *key,
                                                     apr_ssize_t klen,
                                                     const void *h1_val,
                                                     const void *h2_val,
                                                     const void *data),
                                         const void *data)
{
    apr_hash_t *res;
    apr_hash_entry_t *new_vals = NULL;
    apr_hash_entry_t *iter;
    apr_hash_entry_t *ent;
    unsigned int i,j,k;

#if APR_POOL_DEBUG
    /* we don't copy keys and values, so it's necessary that
     * overlay->a.pool and base->a.pool have a life span at least
     * as long as p
     */
    if (!apr_pool_is_ancestor(overlay->pool, p)) {
        fprintf(stderr,
                "apr_hash_merge: overlay's pool is not an ancestor of p\n");
        abort();
    }
    if (!apr_pool_is_ancestor(base->pool, p)) {
        fprintf(stderr,
                "apr_hash_merge: base's pool is not an ancestor of p\n");
        abort();
    }
#endif

    res = apr_palloc(p, sizeof(apr_hash_t));
    res->pool = p;
    res->free = NULL;
    res->hash_func = base->hash_func;
    res->count = base->count;
    res->max = (overlay->max > base->max) ? overlay->max : base->max;
    if (base->count + overlay->count > res->max) {
        res->max = res->max * 2 + 1;
    }
    res->array = alloc_array(res, res->max);
    if (base->count + overlay->count) {
        new_vals = apr_palloc(p, sizeof(apr_hash_entry_t) *
                              (base->count + overlay->count));
    }
    j = 0;
    for (k = 0; k <= base->max; k++) {
        for (iter = base->array[k]; iter; iter = iter->next) {
            i = iter->hash & res->max;
            new_vals[j].klen = iter->klen;
            new_vals[j].key = iter->key;
            new_vals[j].val = iter->val;
            new_vals[j].hash = iter->hash;
            new_vals[j].next = res->array[i];
            res->array[i] = &new_vals[j];
            j++;
        }
    }

    for (k = 0; k <= overlay->max; k++) {
        for (iter = overlay->array[k]; iter; iter = iter->next) {
            i = iter->hash & res->max;
            for (ent = res->array[i]; ent; ent = ent->next) {
                if ((ent->klen == iter->klen) &&
                    (memcmp(ent->key, iter->key, iter->klen) == 0)) {
                    if (merger) {
                        ent->val = (*merger)(p, iter->key, iter->klen,
                                             iter->val, ent->val, data);
                    }
                    else {
                        ent->val = iter->val;
                    }
                    break;
                }
            }
            if (!ent) {
                new_vals[j].klen = iter->klen;
                new_vals[j].key = iter->key;
                new_vals[j].val = iter->val;
                new_vals[j].hash = iter->hash;
                new_vals[j].next = res->array[i];
                res->array[i] = &new_vals[j];
                res->count++;
                j++;
            }
        }
    }
    return res;
}
#endif

/* This is basically the following...
 * for every element in hash table {
 *    comp elemeny.key, element.value
 * }
 *
 * Like with apr_table_do, the comp callback is called for each and every
 * element of the hash table.
 */
/**
 * TODO: Migrate from apr_hash (pool-based) structure to rmm_hash (rmm-based) structure
 */
#if 0
APR_DECLARE(int) apr_hash_do(apr_hash_do_callback_fn_t *comp,
                             void *rec, const apr_hash_t *ht)
{
    apr_hash_index_t  hix;
    apr_hash_index_t *hi;
    int rv, dorv  = 1;

    hix.ht    = (apr_hash_t *)ht;
    hix.index = 0;
    hix.this  = NULL;
    hix.next  = NULL;

    if ((hi = apr_hash_next(&hix))) {
        /* Scan the entire table */
        do {
            rv = (*comp)(rec, hi->this->key, hi->this->klen, hi->this->val);
        } while (rv && (hi = apr_hash_next(hi)));

        if (rv == 0) {
            dorv = 0;
        }
    }
    return dorv;
}
#endif

/**
 * TODO: Migrate from apr_hash (pool-based) structure to rmm_hash (rmm-based) structure
 */
#if 0
APR_POOL_IMPLEMENT_ACCESSOR(hash)
#endif
