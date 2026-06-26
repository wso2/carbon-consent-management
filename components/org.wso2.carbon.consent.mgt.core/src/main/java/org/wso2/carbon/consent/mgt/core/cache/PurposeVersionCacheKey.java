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

import org.wso2.carbon.identity.core.cache.CacheKey;

import java.util.Objects;

/**
 * Cache key for a specific {@link org.wso2.carbon.consent.mgt.core.model.PurposeVersion},
 * identified by the parent purpose UUID and the version UUID.
 */
public class PurposeVersionCacheKey extends CacheKey {

    private static final long serialVersionUID = -3047265801734920183L;

    private final String purposeUuid;
    private final String versionUuid;

    public PurposeVersionCacheKey(String purposeUuid, String versionUuid) {

        this.purposeUuid = purposeUuid;
        this.versionUuid = versionUuid;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof PurposeVersionCacheKey)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PurposeVersionCacheKey that = (PurposeVersionCacheKey) o;
        return Objects.equals(purposeUuid, that.purposeUuid)
                && Objects.equals(versionUuid, that.versionUuid);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), purposeUuid, versionUuid);
    }
}
