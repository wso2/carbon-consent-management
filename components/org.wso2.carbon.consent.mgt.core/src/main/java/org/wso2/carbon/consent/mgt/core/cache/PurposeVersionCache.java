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
 * Cache for individual {@link org.wso2.carbon.consent.mgt.core.model.PurposeVersion} objects,
 * keyed by (purposeUuid, versionUuid).
 */
public class PurposeVersionCache extends BaseCache<PurposeVersionCacheKey, PurposeVersionCacheEntry> {

    private static final Log log = LogFactory.getLog(PurposeVersionCache.class);
    private static final String CACHE_NAME = "ConsentPurposeVersionCache";

    private static final PurposeVersionCache INSTANCE = new PurposeVersionCache();

    private PurposeVersionCache() {

        super(CACHE_NAME);
    }

    public static PurposeVersionCache getInstance() {

        return INSTANCE;
    }

    @Override
    public void addToCache(PurposeVersionCacheKey key, PurposeVersionCacheEntry entry, String tenantDomain) {

        try {
            super.addToCache(key, entry, tenantDomain);
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Cache infrastructure not available, skipping addToCache for PurposeVersionCache.", e);
            }
        }
    }

    @Override
    public PurposeVersionCacheEntry getValueFromCache(PurposeVersionCacheKey key, String tenantDomain) {

        try {
            return super.getValueFromCache(key, tenantDomain);
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Cache infrastructure not available, skipping getValueFromCache for PurposeVersionCache.", e);
            }
            return null;
        }
    }

    @Override
    public void clearCacheEntry(PurposeVersionCacheKey key, String tenantDomain) {

        try {
            super.clearCacheEntry(key, tenantDomain);
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Cache infrastructure not available, skipping clearCacheEntry for PurposeVersionCache.", e);
            }
        }
    }

    @Override
    public void clear(String tenantDomain) {

        try {
            super.clear(tenantDomain);
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Cache infrastructure not available, skipping clear for PurposeVersionCache.", e);
            }
        }
    }
}
