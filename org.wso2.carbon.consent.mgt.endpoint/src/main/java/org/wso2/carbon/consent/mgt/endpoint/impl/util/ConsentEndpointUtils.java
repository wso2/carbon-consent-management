package org.wso2.carbon.consent.mgt.endpoint.impl.util;

import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeRequestDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class ConsentEndpointUtils {

    public static ConsentManager getConsentManager() {
        return (ConsentManager) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(ConsentManager.class, null);
    }

    public static Purpose getPurposeRequest(PurposeRequestDTO purposeRequestDTO) {
        return new Purpose(purposeRequestDTO.getPurpose(), purposeRequestDTO.getDescription());
    }

    public static PurposeListResponseDTO getPurposeListResponse(Purpose purposeResponse) {
        PurposeListResponseDTO purposeListResponseDTO = new PurposeListResponseDTO();
        purposeListResponseDTO.setPurposeId(String.valueOf(purposeResponse.getId()));
        purposeListResponseDTO.setPurpose(purposeResponse.getName());
        purposeListResponseDTO.setDiscripiton(purposeResponse.getDescription());
        return purposeListResponseDTO;
    }
}
