/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.consent.mgt.core.dao;

import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.ConsentAuthorization;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.consent.mgt.core.model.ReceiptUpdateInput;

import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Perform CRUD operations for {@link Receipt}.
 *
 * @since 1.0.0
 */
public interface ReceiptDAO {

    /**
     * Returns the priority of the DAO.
     *
     * @return priority of the DAO.
     */
    int getPriority();

    /**
     * Add a {@link Receipt}.
     *
     * @param receiptInput {@link Receipt} to insert.
     * @throws ConsentManagementException If error occurs while adding the {@link Receipt}.
     */
    void addReceipt(ReceiptInput receiptInput) throws ConsentManagementException;

    /**
     * Retrieve {@link Receipt} by receipt ID.
     *
     * @param receiptId ID of the {@link Receipt} to retrieve.
     * @return Receipt for the given ID.
     * @throws ConsentManagementException If error occurs while retrieving {@link Receipt}.
     */
    Receipt getReceipt(String receiptId) throws ConsentManagementException;

    /**
     * Revoke a {@link Receipt} by ID.
     *
     * @param receiptId ID of the {@link Receipt}.
     * @throws ConsentManagementException If error occurs while revoking the {@link Receipt}.
     */
    void revokeReceipt(String receiptId) throws ConsentManagementException;

    /**
     * Delete a {@link Receipt} by ID.
     *
     * @param receiptID
     * @throws ConsentManagementException
     */
    void deleteReceipt(String receiptID) throws ConsentManagementException;

    /**
     * Delete all {@link Receipt} of a given tenant id.
     *
     * @param tenantId Id of the tenant
     * @throws ConsentManagementException
     */
    default void deleteReceiptsByTenantId(int tenantId) throws ConsentManagementException {

    }

    /**
     *  Search {@link Receipt} items for a given criteria.
     *
     * @param limit Maximum number of results expected.
     * @param offset Result offset.
     * @param piiPrincipalId Identifier of the principal subject.
     * @param spTenantId Tenant domain of the service.
     * @param service Service name.
     * @param state State of the {@link Receipt}.
     * @param principalTenantId Tenant ID of the principal.
     * @return A list of {@link ReceiptListResponse}
     * @throws ConsentManagementException If error occurs while searching {@link Receipt} items.
     */
    List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId, int spTenantId,
                                             String service, String state, int principalTenantId)
            throws ConsentManagementException;

    /**
     * Check whether a receipt exists with the given criteria.
     * @param receiptId Consent Receipt Id.
     * @param piiPrincipalId PII Principal Id.
     * @param tenantId PII Principal tenant Id.
     * @return true if a receiept exists.
     * @throws ConsentManagementException If error occurs while checking {@link ConsentManagementException}
     */
    boolean isReceiptExist(String receiptId, String piiPrincipalId, int tenantId) throws ConsentManagementException;

    default void insertConsentAuthorization(ConsentAuthorization authorization)
            throws ConsentManagementException {
    }

    default void addReceiptWithAuthorizations(ReceiptInput receiptInput, List<ConsentAuthorization> authorizations)
            throws ConsentManagementException {

    }

    default List<ConsentAuthorization> getConsentAuthorizations(String consentReceiptId)
            throws ConsentManagementException {

        return Collections.emptyList();
    }

    default ConsentAuthorization getConsentAuthorizationByUser(String consentReceiptId, String userId)
            throws ConsentManagementException {

        return null;
    }

    default void updateConsentAuthorization(String consentReceiptId, String userId, String status, long updatedTime)
            throws ConsentManagementException {
    }

    default void updateReceiptState(String consentReceiptId, String state)
            throws ConsentManagementException {
    }

    default String getReceiptState(String consentReceiptId)
            throws ConsentManagementException {

        return null;
    }

    default Timestamp getReceiptExpiryTime(String consentReceiptId)
            throws ConsentManagementException {

        return null;
    }

    /**
     * Applies a receipt update and recomputes the receipt state atomically within a single transaction.
     * <p>
     * The authorization changes, the re-read of the resulting authorization set, and the persisted state
     * update all happen inside one transaction guarded by a lock on the receipt row, so concurrent callers
     * targeting the same receipt are serialized. This prevents a stale state being written from a snapshot
     * read taken before another writer committed (lost update).
     * <p>
     * The receipt state is recomputed only when {@link ReceiptUpdateInput#getAuthorizations()} is non-null,
     * using {@code statusCalculator} so the caller's status-derivation logic is reused unchanged.
     *
     * @param updateInput      Fields to update.
     * @param statusCalculator Function deriving the receipt state from the post-update authorization set.
     * @throws ConsentManagementException if the update fails.
     */
    default void updateConsent(ReceiptUpdateInput updateInput,
                               Function<List<ConsentAuthorization>, String> statusCalculator)
            throws ConsentManagementException {
    }

    /**
     * Lists receipts using cursor-based pagination (V2 API).
     * Cursor and property filter nodes are supplied as {@link ExpressionNode} instances
     * produced by {@link org.wso2.carbon.consent.mgt.core.util.FilterQueriesUtil#getExpressionNodes}.
     */
    default List<Receipt> listReceipts(String subjectId, String serviceId, String state,
                                       String purposeId, String purposeVersionId,
                                       int limit, int tenantId,
                                       List<ExpressionNode> expressionNodes)
            throws ConsentManagementException {

        return Collections.emptyList();
    }

    /**
     * Retrieve {@link Receipt} by receipt ID using extended schema (requires UUID/version columns).
     * Falls back to {@link #getReceipt(String)} for implementations that do not override.
     */
    default Receipt getReceiptWithExtendedSchema(String receiptId) throws ConsentManagementException {

        return getReceipt(receiptId);
    }
}
