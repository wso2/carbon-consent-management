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
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.consent.mgt.core.model.PurposePIICategory" %>
<%@ page import="org.wso2.carbon.consent.mgt.core.model.Purpose" %>
<%@ page import="org.wso2.carbon.consent.mgt.ui.client.ConsentManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%! private static final String LOGGED_USER = "logged-user";
    private static final String PURPOSE_ID = "purposeId";
    private static final String PURPOSE_NAME = "purposeName";
%>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String BUNDLE = "org.wso2.carbon.consent.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    int purposeId = Integer.parseInt(request.getParameter(PURPOSE_ID));
    String purposeName = request.getParameter(PURPOSE_NAME);

    Purpose purpose = null;
    List<PurposePIICategory> piiCategories = new ArrayList<PurposePIICategory>();

    try {
        String currentUser = (String) session.getAttribute(LOGGED_USER);
        ConsentManagementServiceClient serviceClient = new ConsentManagementServiceClient(currentUser);
        purpose = serviceClient.getPurpose(purposeId);
        piiCategories = purpose.getPurposePIICategories();
    } catch (Exception e) {
        String message = resourceBundle.getString("error.while.reading.pii.info") + " : " + e.getMessage();
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
    }
%>

<fmt:bundle
        basename="org.wso2.carbon.consent.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="consent.mgt.pii.categories"
                       resourceBundle="org.wso2.carbon.consent.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="title.list.pii.categories"/> <%=Encode.forHtml(purposeName)%></h2>
        <div id="workArea">

            <script type="text/javascript">
                function doCancel() {
                    location.href = '<%=Encode.forJavaScriptBlock("list-purposes.jsp?region=region1&item=list_consent_menu")%>';
                }
            </script>

            <table class="styledLeft" width="100%" id="piiCategories">
                            <thead>
                            <tr style="white-space: nowrap">
                                <th class="leftCol-med"><fmt:message key="field.pii.consent.id"/></th>
                                <th class="leftCol-big"><fmt:message key="consent.mgt.displayname"/></th>
                                <th class="leftCol-big"><fmt:message key="consent.mgt.description"/></th>
                            </tr>
                            </thead>
                            <%
                                if (piiCategories.size() > 0) {
                            %>
                            <tbody>
                            <%
                                for (PurposePIICategory piiCategory : piiCategories) {
                            %>
                            <tr>
                                <td><%=Encode.forHtml(piiCategory.getName())%>
                                </td>
                                <td><%=piiCategory.getDisplayName() != null ? Encode.forHtml(piiCategory.getDisplayName()) : ""%>
                                </td>
                                <td><%=piiCategory.getDescription() != null ? Encode.forHtml(piiCategory.getDescription()) : ""%>
                                </td>
                            </tr>
                            <%
                                }
                            %>
                            </tbody>
                            <% } else { %>
                            <tbody>
                            <tr>
                                <td colspan="3"><i><fmt:message key="no.pii.cat.registered"/></i></td>
                            </tr>
                            </tbody>
                            <% } %>
        </table>
            <table class="styledLeft noBorders" style="margin-top: 10px">
                <tbody>
                    <tr>
                        <td class="buttonRow">
                            <input class="button" type="button" value="<fmt:message key="back"/>" onclick="doCancel()"/>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>