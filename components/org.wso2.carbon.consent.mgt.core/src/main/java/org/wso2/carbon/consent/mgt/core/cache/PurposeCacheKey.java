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
 * Cache key for a consent purpose, identified by its UUID.
 * Used by {@link PurposeCache}.
 */
public class PurposeCacheKey extends CacheKey {

    private static final long serialVersionUID = 4812736401948302751L;

    private final String purposeUuid;

    public PurposeCacheKey(String purposeUuid) {

        this.purposeUuid = purposeUuid;
    }

    public String getPurposeUuid() {

        return purposeUuid;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof PurposeCacheKey)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PurposeCacheKey that = (PurposeCacheKey) o;
        return Objects.equals(purposeUuid, that.purposeUuid);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), purposeUuid);
    }
}
