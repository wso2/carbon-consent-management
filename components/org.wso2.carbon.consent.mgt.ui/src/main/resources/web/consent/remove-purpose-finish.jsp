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
<%! public static final String LOGGED_USER = "logged-user";
    public static final String PURPOSE_NAME = "purposeName";
%>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle
        basename="org.wso2.carbon.consent.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="consent.mgt"
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
                
                try {
                    String currentUser = (String) session.getAttribute(LOGGED_USER);
                    ConsentManagementServiceClient serviceClient = new ConsentManagementServiceClient(currentUser);
                    serviceClient.deletePurposeByName(purposeName);
                    forwardTo = "list-purposes.jsp";
                } catch (Exception e) {
                    String message = resourceBundle.getString("error.while.delete.purpose");
                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
                    forwardTo = "list-purposes.jsp";
                }
            %>
            
            <script type="text/javascript">
                function forward() {
                    location.href = "<%=forwardTo%>";
                }

                forward();
            </script>
        
        </div>
    </div>
</fmt:bundle>