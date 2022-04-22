<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ page import="org.wso2.carbon.consent.mgt.ui.client.ConsentManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException" %>
<%@ page import="org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException" %>
<%@ page import="org.wso2.carbon.consent.mgt.core.model.Purpose" %>
<%@ page
        import="static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_IS_ASSOCIATED" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="org.json.JSONArray" %>
<%! public static final String LOGGED_USER = "logged-user";
    public static final String PURPOSE_NAME = "purposeName";
%>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle
        basename="org.wso2.carbon.consent.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="consent.purposes"
                       resourceBundle="org.wso2.carbon.consents.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    
    <div id="middle">
        <div id="workArea">
            
            <%
                String httpMethod = request.getMethod();
                if (!"post".equalsIgnoreCase(httpMethod)) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }
                
                String forwardTo = null;
                String BUNDLE = "org.wso2.carbon.consent.mgt.ui.i18n.Resources";
                ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
                
                String purposeName = request.getParameter(PURPOSE_NAME);
    
                String callback = request.getParameter("callback");
                String purposeGroup = request.getParameter("purposeGroup");
                String purposeGroupType = request.getParameter("purposeGroupType");
                String listPurposePage = "list-purposes.jsp";
                String purposeIdList = request.getParameter("purposeIdList");
                String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    
                if (StringUtils.isNotEmpty(callback) && StringUtils.isNotEmpty(purposeGroup) &&
                        StringUtils.isNotEmpty(purposeGroupType) && callback.startsWith("/")) {
                    listPurposePage = listPurposePage + "?purposeGroup=" + purposeGroup + "&purposeGroupType=" +
                            purposeGroupType + "&callback=" + URLEncoder.encode(callback,
                            StandardCharsets.UTF_8.name());
                }
                
                try {
                    String currentUser = (String) session.getAttribute(LOGGED_USER);
                    ConsentManagementServiceClient serviceClient = new ConsentManagementServiceClient(currentUser);

                    JSONArray purposeIdListParsed = new JSONArray(purposeIdList);

                    boolean hasPurposeWithMandatoryEmailInRemainingList = false;
                    boolean hasPurposeWithMandatoryEmailInDeletedPurpose = false;

                    if (purposeIdListParsed.length() > 1) {

                        for (int i = 0; i < purposeIdListParsed.length(); i++) {
                            Purpose retrievedPurpose = serviceClient.getPurpose(Integer.parseInt(purposeIdListParsed.get(i).toString()));
                            if (!retrievedPurpose.getName().equals(purposeName)) {
                                if (!hasPurposeWithMandatoryEmailInRemainingList) {
                                    hasPurposeWithMandatoryEmailInRemainingList =
                                            retrievedPurpose.getPurposePIICategories().stream().anyMatch(purposePIICategory ->
                                                    purposePIICategory.getName().equals(EMAIL_CLAIM_URI) &&
                                                            purposePIICategory.getMandatory());
                                }
                            } else {
                                hasPurposeWithMandatoryEmailInDeletedPurpose = retrievedPurpose.getPurposePIICategories().stream().anyMatch(
                                        purposePIICategory -> purposePIICategory.getName().equals(EMAIL_CLAIM_URI) &&
                                                purposePIICategory.getMandatory());
                            }
                        }
                    }

                    serviceClient.deletePurposeByName(purposeName, purposeGroup, purposeGroupType);
                    if (hasPurposeWithMandatoryEmailInDeletedPurpose && !hasPurposeWithMandatoryEmailInRemainingList) {
                        String message = MessageFormat.format(resourceBundle.getString("missing.mandatory.email.pii.category.warning.delete.purpose"),
                                purposeName);
                        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.WARNING, request);
                    }
                    forwardTo = listPurposePage;
                } catch (ConsentManagementException e) {
                    String message = resourceBundle.getString("error.while.delete.purpose");
                    if (ERROR_CODE_PURPOSE_IS_ASSOCIATED.getCode().equals(e.getErrorCode())) {
                        message = resourceBundle.getString("error.while.delete.purpose.used");
                    }
                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
                    forwardTo = listPurposePage;
                } catch (Exception e) {
                    String message = resourceBundle.getString("error.while.delete.purpose");
                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
                    forwardTo = listPurposePage;
                }
            %>
    
            <script type="text/javascript">
                function forward() {
                    location.href = "<%=Encode.forJavaScript(forwardTo)%>";
                }

                forward();
            </script>

        </div>
    </div>
</fmt:bundle>