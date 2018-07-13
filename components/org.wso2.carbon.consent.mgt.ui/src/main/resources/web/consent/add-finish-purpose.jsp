<%--
  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

   WSO2 Inc. licenses this file to you under the Apache License,
   Version 2.0 (the "License"); you may not use this file except
   in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="org.apache.commons.lang.StringUtils" %>
<%@page import="org.json.JSONObject" %>
<%@page import="org.wso2.carbon.consent.mgt.ui.client.ConsentManagementServiceClient" %>
<%@page import="org.wso2.carbon.consent.mgt.ui.dto.PiiCategoryDTO" %>
<%@page import="org.wso2.carbon.consent.mgt.ui.dto.PurposeRequestDTO" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="static org.wso2.carbon.consent.mgt.ui.constant.ClaimMgtUIConstants.CLAIM_URI" %>
<%@ page import="static org.wso2.carbon.consent.mgt.ui.constant.ClaimMgtUIConstants.DISPLAY_NAME" %>
<%@ page import="static org.wso2.carbon.consent.mgt.ui.constant.ClaimMgtUIConstants.DESCRIPTION" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.consent.mgt.core.constant.ConsentConstants" %>
<%@ page import="org.owasp.encoder.Encode" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    
    String name = null;
    String BUNDLE = "org.wso2.carbon.consent.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String forwardTo = null;
    String defaultConsentGroup = "DEFAULT";
    String defaultConsentGroupType = "SP";
    String PURPOSE_GROUP = "purposeGroup";
    String PURPOSE_GROUP_TYPE = "purposeGroupType";
    String CALLBACK = "callback";
    String purposeGroup = request.getParameter(PURPOSE_GROUP);
    String purposeGroupType = request.getParameter(PURPOSE_GROUP_TYPE);
    String callback = request.getParameter(CALLBACK);
    String listPurposesPage = "list-purposes.jsp?";
    String addPurposesPage = "add-purpose.jsp?";
    
    if (StringUtils.isNotEmpty(purposeGroup)) {
        listPurposesPage = listPurposesPage + PURPOSE_GROUP + "=" + purposeGroup;
        addPurposesPage = addPurposesPage + PURPOSE_GROUP + "=" + purposeGroup;
    }
    if (StringUtils.isNotEmpty(purposeGroupType)) {
        listPurposesPage = listPurposesPage + "&" + PURPOSE_GROUP_TYPE + "=" + purposeGroupType;
        addPurposesPage = addPurposesPage + "&" + PURPOSE_GROUP_TYPE + "=" + purposeGroupType;
    }
    if (StringUtils.isNotEmpty(callback)) {
        if (!callback.startsWith("/")) {
            callback = "";
        } else {
            listPurposesPage = listPurposesPage + "&" + CALLBACK + "=" + callback;
            addPurposesPage = addPurposesPage + "&" + CALLBACK + "=" + callback;
        }
    }
    
    try {
        String currentUser = (String) session.getAttribute("logged-user");
        ConsentManagementServiceClient serviceClient = new ConsentManagementServiceClient(currentUser);
        PurposeRequestDTO purposeRequestDTO = new PurposeRequestDTO();
        List<PiiCategoryDTO> categories = new ArrayList<PiiCategoryDTO>();
        name = request.getParameter("purposeName");
        String description = request.getParameter("purpose.description");
        String group = request.getParameter("group");
        String groupType = request.getParameter("groupType");
        int categoryCount = Integer.parseInt(request.getParameter("claimrow_name_count"));
        for (int i = 0; i < categoryCount; i++) {
            String claimInfo = request.getParameter("claimrow_name_wso2_" + i);
            if (StringUtils.isNotBlank(claimInfo)) {
                JSONObject jsonObject = new JSONObject(claimInfo);
                String piiCatName = null;
                String displayName = null;
                String piiCatDescription = null;
            
                if (jsonObject.get(CLAIM_URI) != null && jsonObject.get(CLAIM_URI) instanceof String) {
                    piiCatName = (String) jsonObject.get(CLAIM_URI);
                }
            
                if (jsonObject.get(DISPLAY_NAME) != null && jsonObject.get(DISPLAY_NAME) instanceof String) {
                    displayName = (String) jsonObject.get(DISPLAY_NAME);
                }
            
                if (jsonObject.get(DESCRIPTION) != null && jsonObject.get(DESCRIPTION) instanceof String) {
                    piiCatDescription = (String) jsonObject.get(DESCRIPTION);
                }
                if (StringUtils.isNotBlank(piiCatName)) {
                    categories.add(new PiiCategoryDTO(piiCatName, displayName, piiCatDescription));
                }
            }
        }
        purposeRequestDTO.setPurpose(name);
    
        purposeRequestDTO.setDescription(description == null ? "" : description);
        purposeRequestDTO.setGroup(group);
        purposeRequestDTO.setGroupType(groupType);
        purposeRequestDTO.setMandatory(false);
        purposeRequestDTO.setPiiCategories(categories);
        serviceClient.addPurpose(purposeRequestDTO);
        
        String message = MessageFormat.format(resourceBundle.getString("purpose.add.success"), name);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("purpose.cannot.add"), name);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
    }

    if (CarbonUIUtil.isUserAuthorized(request, ConsentConstants.PERMISSION_CONSENT_MGT_LIST)) {
        forwardTo = listPurposesPage;
    } else {
        forwardTo = addPurposesPage;
    }
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=Encode.forJavaScript(forwardTo)%>";
    }

    forward();
</script>
