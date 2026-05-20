/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.consent.mgt.core.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Cache for the list of {@link org.wso2.carbon.consent.mgt.core.model.PurposeVersion} objects
 * belonging to a purpose, keyed by purpose UUID via {@link PurposeCacheKey}.
 */
public class PurposeVersionListCache extends BaseCache<PurposeCacheKey, PurposeVersionListCacheEntry> {

    private static final Log log = LogFactory.getLog(PurposeVersionListCache.class);
    private static final String CACHE_NAME = "ConsentPurposeVersionListCache";

    private static final PurposeVersionListCache INSTANCE = new PurposeVersionListCache();

    private PurposeVersionListCache() {

        super(CACHE_NAME);
    }

    public static PurposeVersionListCache getInstance() {

        return INSTANCE;
    }

    @Override
    public void addToCache(PurposeCacheKey key, PurposeVersionListCacheEntry entry, String tenantDomain) {

        try {
            super.addToCache(key, entry, tenantDomain);
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Cache infrastructure not available, skipping addToCache for PurposeVersionListCache.", e);
            }
        }
    }

    @Override
    public PurposeVersionListCacheEntry getValueFromCache(PurposeCacheKey key, String tenantDomain) {

        try {
            return super.getValueFromCache(key, tenantDomain);
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Cache infrastructure not available, skipping getValueFromCache for PurposeVersionListCache.",
                        e);
            }
            return null;
        }
    }

    @Override
    public void clearCacheEntry(PurposeCacheKey key, String tenantDomain) {

        try {
            super.clearCacheEntry(key, tenantDomain);
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Cache infrastructure not available, skipping clearCacheEntry for PurposeVersionListCache.",
                        e);
            }
        }
    }

    @Override
    public void clear(String tenantDomain) {

        try {
            super.clear(tenantDomain);
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Cache infrastructure not available, skipping clear for PurposeVersionListCache.", e);
            }
        }
    }
}
