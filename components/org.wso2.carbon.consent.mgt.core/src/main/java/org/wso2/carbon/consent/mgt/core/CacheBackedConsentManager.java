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

package org.wso2.carbon.consent.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.cache.PurposeCache;
import org.wso2.carbon.consent.mgt.core.cache.PurposeCacheEntry;
import org.wso2.carbon.consent.mgt.core.cache.PurposeCacheKey;
import org.wso2.carbon.consent.mgt.core.cache.PurposeVersionCache;
import org.wso2.carbon.consent.mgt.core.cache.PurposeVersionCacheEntry;
import org.wso2.carbon.consent.mgt.core.cache.PurposeVersionCacheKey;
import org.wso2.carbon.consent.mgt.core.cache.PurposeVersionListCache;
import org.wso2.carbon.consent.mgt.core.cache.PurposeVersionListCacheEntry;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.AddReceiptResponse;
import org.wso2.carbon.consent.mgt.core.model.ConsentAuthorization;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.util.List;

import static org.wso2.carbon.consent.mgt.core.util.ConsentUtils.getTenantDomainFromCarbonContext;

/**
 * Decorator that wraps a {@link ConsentManager} delegate and adds a caching layer.
 * <p>
 * Cache READ methods: check cache first, delegate on miss, populate cache with result.
 * Cache INVALIDATION methods: call delegate first, then clear relevant cache entries.
 * All other methods are forwarded directly to the delegate.
 * <p>
 * Cached data:
 * <ul>
 *   <li>{@link org.wso2.carbon.consent.mgt.core.cache.PurposeCache} — individual purposes by UUID</li>
 *   <li>{@link PurposeVersionCache} — individual purpose versions by (purposeUuid, versionUuid)</li>
 *   <li>{@link PurposeVersionListCache} — full version list per purpose by purposeUuid</li>
 * </ul>
 */
public class CacheBackedConsentManager implements ConsentManager {

    private static final Log LOG = LogFactory.getLog(CacheBackedConsentManager.class);

    private final ConsentManager delegate;
    private final PurposeCache purposeCache = PurposeCache.getInstance();
    private final PurposeVersionCache purposeVersionCache = PurposeVersionCache.getInstance();
    private final PurposeVersionListCache purposeVersionListCache = PurposeVersionListCache.getInstance();

    public CacheBackedConsentManager(ConsentManager delegate) {

        this.delegate = delegate;
    }

    @Override
    public List<PurposeVersion> listPurposeVersions(String purposeUuid) throws ConsentManagementException {

        String tenantDomain = getTenantDomainFromCarbonContext();
        PurposeCacheKey key = new PurposeCacheKey(purposeUuid);
        PurposeVersionListCacheEntry cached = purposeVersionListCache.getValueFromCache(key, tenantDomain);
        if (cached != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for PurposeVersionList of purpose " + purposeUuid);
            }
            return cached.getVersions();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for PurposeVersionList of purpose " + purposeUuid
                    + ". Fetching entry from DB");
        }
        List<PurposeVersion> versions = delegate.listPurposeVersions(purposeUuid);
        if (versions != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry fetched from DB for PurposeVersionList of purpose " + purposeUuid
                        + ". Updating cache");
            }
            purposeVersionListCache.addToCache(key, new PurposeVersionListCacheEntry(versions), tenantDomain);
        }
        return versions;
    }

    @Override
    public PurposeVersion getPurposeVersion(String purposeUuid, String versionUuid) throws ConsentManagementException {

        String tenantDomain = getTenantDomainFromCarbonContext();
        PurposeVersionCacheKey key = new PurposeVersionCacheKey(purposeUuid, versionUuid);
        PurposeVersionCacheEntry cached = purposeVersionCache.getValueFromCache(key, tenantDomain);
        if (cached != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for PurposeVersion " + versionUuid + " of purpose " + purposeUuid);
            }
            return cached.getVersion();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for PurposeVersion " + versionUuid + " of purpose " + purposeUuid
                    + ". Fetching entry from DB");
        }
        PurposeVersion version = delegate.getPurposeVersion(purposeUuid, versionUuid);
        if (version != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry fetched from DB for PurposeVersion " + versionUuid + " of purpose " + purposeUuid
                        + ". Updating cache");
            }
            purposeVersionCache.addToCache(key, new PurposeVersionCacheEntry(version), tenantDomain);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for PurposeVersion " + versionUuid + " of purpose " + purposeUuid
                        + " not found in cache or DB");
            }
        }
        return version;
    }

    @Override
    public Purpose addPurpose(Purpose purpose) throws ConsentManagementException {

        return delegate.addPurpose(purpose);
    }

    @Override
    public Purpose addPurposeWithUuid(Purpose purpose) throws ConsentManagementException {

        return delegate.addPurposeWithUuid(purpose);
    }

    @Override
    public void deletePurpose(int purposeId) throws ConsentManagementException {

        delegate.deletePurpose(purposeId);
    }

    @Override
    public void deletePurpose(String purposeUuid) throws ConsentManagementException {

        delegate.deletePurpose(purposeUuid);
        String tenantDomain = getTenantDomainFromCarbonContext();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing entry for Purpose " + purposeUuid + " of tenantDomain:" + tenantDomain
                    + " from cache.");
        }
        PurposeCacheKey key = new PurposeCacheKey(purposeUuid);
        purposeCache.clearCacheEntry(key, tenantDomain);
        purposeVersionListCache.clearCacheEntry(key, tenantDomain);
        purposeVersionCache.clear(tenantDomain);
    }

    @Override
    public void deletePurposes(int tenantId) throws ConsentManagementException {

        delegate.deletePurposes(tenantId);
        String tenantDomain = getTenantDomainFromCarbonContext();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing all Purpose, PurposeVersion and PurposeVersionList cache entries of tenantDomain:"
                    + tenantDomain + " from cache.");
        }
        purposeCache.clear(tenantDomain);
        purposeVersionListCache.clear(tenantDomain);
        purposeVersionCache.clear(tenantDomain);
    }

    @Override
    public PurposeVersion addPurposeVersion(String purposeUuid, PurposeVersion purposeVersion, boolean setAsLatest)
            throws ConsentManagementException {

        PurposeVersion result = delegate.addPurposeVersion(purposeUuid, purposeVersion, setAsLatest);
        String tenantDomain = getTenantDomainFromCarbonContext();
        PurposeCacheKey key = new PurposeCacheKey(purposeUuid);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing entry for PurposeVersionList of purpose " + purposeUuid + " of tenantDomain:"
                    + tenantDomain + " from cache.");
        }
        purposeVersionListCache.clearCacheEntry(key, tenantDomain);
        if (setAsLatest) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Removing entry for Purpose " + purposeUuid + " of tenantDomain:" + tenantDomain
                        + " from cache after setting latest version.");
            }
            purposeCache.clearCacheEntry(key, tenantDomain);
        }
        return result;
    }

    @Override
    public void deletePurposeVersion(String purposeUuid, String versionUuid) throws ConsentManagementException {

        delegate.deletePurposeVersion(purposeUuid, versionUuid);
        String tenantDomain = getTenantDomainFromCarbonContext();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing entry for PurposeVersion " + versionUuid + " of purpose " + purposeUuid
                    + " of tenantDomain:" + tenantDomain + " from cache.");
        }
        purposeVersionCache.clearCacheEntry(new PurposeVersionCacheKey(purposeUuid, versionUuid), tenantDomain);
        purposeVersionListCache.clearCacheEntry(new PurposeCacheKey(purposeUuid), tenantDomain);
    }

    @Override
    public void setLatestPurposeVersion(String purposeUUID, String versionLabel) throws ConsentManagementException {

        delegate.setLatestPurposeVersion(purposeUUID, versionLabel);
        String tenantDomain = getTenantDomainFromCarbonContext();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing entry for Purpose " + purposeUUID + " of tenantDomain:" + tenantDomain
                    + " from cache after updating latest version.");
        }
        purposeCache.clearCacheEntry(new PurposeCacheKey(purposeUUID), tenantDomain);
    }

    @Override
    public List<Receipt> listReceipts(String subjectId, String serviceId, String state,
                                      String purposeId, String purposeVersionId,
                                      String after, String before, int limit)
            throws ConsentManagementException {

        return delegate.listReceipts(subjectId, serviceId, state, purposeId, purposeVersionId, after, before, limit);
    }

    @Override
    public AddReceiptResponse addConsent(ReceiptInput receiptInput) throws ConsentManagementException {

        return delegate.addConsent(receiptInput);
    }

    @Override
    public void revokeReceipt(String receiptId) throws ConsentManagementException {

        delegate.revokeReceipt(receiptId);
    }

    @Override
    public void deleteReceipt(String receiptId) throws ConsentManagementException {

        delegate.deleteReceipt(receiptId);
    }

    @Override
    public void deleteReceipts(int tenantId) throws ConsentManagementException {

        delegate.deleteReceipts(tenantId);
    }

    @Override
    public Purpose getPurpose(int purposeId) throws ConsentManagementException {

        return delegate.getPurpose(purposeId);
    }

    @Override
    public Purpose getPurposeByName(String name, String group, String groupType) throws ConsentManagementException {

        return delegate.getPurposeByName(name, group, groupType);
    }

    @Override
    public Purpose getPurposeByUuid(String uuid) throws ConsentManagementException {

        String tenantDomain = getTenantDomainFromCarbonContext();
        PurposeCacheKey key = new PurposeCacheKey(uuid);
        PurposeCacheEntry cached = purposeCache.getValueFromCache(key, tenantDomain);
        if (cached != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for Purpose " + uuid);
            }
            return cached.getPurpose();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for Purpose " + uuid + ". Fetching entry from DB");
        }
        Purpose purpose = delegate.getPurposeByUuid(uuid);
        if (purpose != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry fetched from DB for Purpose " + uuid + ". Updating cache");
            }
            purposeCache.addToCache(key, new PurposeCacheEntry(purpose), tenantDomain);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for Purpose " + uuid + " not found in cache or DB");
            }
        }
        return purpose;
    }

    @Override
    public List<Purpose> listPurposes(int limit, int offset) throws ConsentManagementException {

        return delegate.listPurposes(limit, offset);
    }

    @Override
    public List<Purpose> listPurposes(String group, String groupType, int limit, int offset)
            throws ConsentManagementException {

        return delegate.listPurposes(group, groupType, limit, offset);
    }

    @Override
    public List<Purpose> listPurposes(List<ExpressionNode> expressionNodes, int limit)
            throws ConsentManagementException {

        return delegate.listPurposes(expressionNodes, limit);
    }

    @Override
    public boolean isPurposeExists(String name, String group, String groupType) throws ConsentManagementException {

        return delegate.isPurposeExists(name, group, groupType);
    }

    @Override
    public PurposeCategory addPurposeCategory(PurposeCategory purposeCategory) throws ConsentManagementException {

        return delegate.addPurposeCategory(purposeCategory);
    }

    @Override
    public PurposeCategory getPurposeCategory(int purposeCategoryId) throws ConsentManagementException {

        return delegate.getPurposeCategory(purposeCategoryId);
    }

    @Override
    public PurposeCategory getPurposeCategoryByName(String name) throws ConsentManagementException {

        return delegate.getPurposeCategoryByName(name);
    }

    @Override
    public List<PurposeCategory> listPurposeCategories(int limit, int offset) throws ConsentManagementException {

        return delegate.listPurposeCategories(limit, offset);
    }

    @Override
    public void deletePurposeCategory(int purposeCategoryId) throws ConsentManagementException {

        delegate.deletePurposeCategory(purposeCategoryId);
    }

    @Override
    public void deletePurposeCategories(int tenantId) throws ConsentManagementException {

        delegate.deletePurposeCategories(tenantId);
    }

    @Override
    public boolean isPurposeCategoryExists(String name) throws ConsentManagementException {

        return delegate.isPurposeCategoryExists(name);
    }

    @Override
    public PIICategory addPIICategory(PIICategory piiCategory) throws ConsentManagementException {

        return delegate.addPIICategory(piiCategory);
    }

    @Override
    public PIICategory addPIICategoryWithUuid(PIICategory piiCategory) throws ConsentManagementException {

        return delegate.addPIICategoryWithUuid(piiCategory);
    }

    @Override
    public PIICategory getPIICategoryByName(String name) throws ConsentManagementException {

        return delegate.getPIICategoryByName(name);
    }

    @Override
    public PIICategory getPIICategory(int piiCategoryId) throws ConsentManagementException {

        return delegate.getPIICategory(piiCategoryId);
    }

    @Override
    public List<PIICategory> listPIICategories(int limit, int offset) throws ConsentManagementException {

        return delegate.listPIICategories(limit, offset);
    }

    @Override
    public List<PIICategory> listPIICategories(List<ExpressionNode> expressionNodes, int limit)
            throws ConsentManagementException {

        return delegate.listPIICategories(expressionNodes, limit);
    }

    @Override
    public void deletePIICategory(int piiCategoryId) throws ConsentManagementException {

        delegate.deletePIICategory(piiCategoryId);
    }

    @Override
    public void deletePIICategory(String uuid) throws ConsentManagementException {

        delegate.deletePIICategory(uuid);
    }

    @Override
    public PIICategory getPIICategoryByUuid(String uuid) throws ConsentManagementException {

        return delegate.getPIICategoryByUuid(uuid);
    }

    @Override
    public void deletePIICategories(int tenantId) throws ConsentManagementException {

        delegate.deletePIICategories(tenantId);
    }

    @Override
    public boolean isPIICategoryExists(String name) throws ConsentManagementException {

        return delegate.isPIICategoryExists(name);
    }

    @Override
    public Receipt getReceipt(String receiptId) throws ConsentManagementException {

        return delegate.getReceipt(receiptId);
    }

    @Override
    public Receipt getReceiptWithExtendedSchema(String receiptId) throws ConsentManagementException {

        return delegate.getReceiptWithExtendedSchema(receiptId);
    }

    @Override
    public List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId,
                                                    String spTenantDomain, String service, String state)
            throws ConsentManagementException {

        return delegate.searchReceipts(limit, offset, piiPrincipalId, spTenantDomain, service, state);
    }

    @Override
    public List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId,
                                                    String spTenantDomain, String service, String state,
                                                    String principalTenantDomain)
            throws ConsentManagementException {

        return delegate.searchReceipts(limit, offset, piiPrincipalId, spTenantDomain, service, state,
                principalTenantDomain);
    }

    @Override
    public boolean isReceiptExist(String receiptId, String tenantAwareUsername, int tenantId)
            throws ConsentManagementException {

        return delegate.isReceiptExist(receiptId, tenantAwareUsername, tenantId);
    }

    @Override
    public void authorizeConsent(String consentId, String userId, String authStatus)
            throws ConsentManagementException {

        delegate.authorizeConsent(consentId, userId, authStatus);
    }

    @Override
    public List<ConsentAuthorization> getConsentAuthorizations(String consentId)
            throws ConsentManagementException {

        return delegate.getConsentAuthorizations(consentId);
    }

    @Override
    public String validateConsentStatus(String consentId) throws ConsentManagementException {

        return delegate.validateConsentStatus(consentId);
    }
}
