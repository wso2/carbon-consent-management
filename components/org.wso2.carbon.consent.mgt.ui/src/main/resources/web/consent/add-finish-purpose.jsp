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
<%@page import="org.wso2.carbon.consent.mgt.ui.client.ConsentManagementServiceClient" %>
<%@page import="org.wso2.carbon.consent.mgt.ui.dto.PiiCategoryDTO" %>
<%@page import="org.wso2.carbon.consent.mgt.ui.dto.PurposeRequestDTO" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
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
    
    try {
        String currentUser = (String) session.getAttribute("logged-user");
        ConsentManagementServiceClient serviceClient = new ConsentManagementServiceClient(currentUser);
        PurposeRequestDTO purposeRequestDTO = new PurposeRequestDTO();
        List<PiiCategoryDTO> categories = new ArrayList<PiiCategoryDTO>();
        name = request.getParameter("purposeName");
        String description = request.getParameter("purpose.description");
        int categoryCount = Integer.parseInt(request.getParameter("claimrow_name_count"));
        for (int i = 0; i < categoryCount; i++) {
            String claim = request.getParameter("claimrow_name_wso2_" + i);
            if (StringUtils.isNotBlank(claim)) {
                categories.add(new PiiCategoryDTO(claim));
            }
        }
        purposeRequestDTO.setPurpose(name);
    
        purposeRequestDTO.setDescription(description == null ? "" : description);
        purposeRequestDTO.setPiiCategories(categories);
        serviceClient.addPurpose(purposeRequestDTO);
        
        String message = MessageFormat.format(resourceBundle.getString("purpose.add.success"), name);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
        forwardTo = "list-purposes.jsp";
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("purpose.cannot.add"), name);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "list-purposes.jsp";
    }
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }

    forward();
</script>
