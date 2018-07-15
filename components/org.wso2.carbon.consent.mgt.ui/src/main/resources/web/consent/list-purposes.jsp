<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page import="org.owasp.encoder.Encode" %>
<%@page import="org.wso2.carbon.consent.mgt.core.model.Purpose" %>
<%@ page import="org.wso2.carbon.consent.mgt.ui.client.ConsentManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.consent.mgt.core.constant.ConsentConstants" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%! private static final String DEFAULT = "DEFAULT";
    private static final String LOGGED_USER = "logged-user";
%>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle
        basename="org.wso2.carbon.consent.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="consent.mgt"
                       resourceBundle="org.wso2.identity.consents.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <div id="middle">
        <%
            String callback = request.getParameter("callback");
            String purposeGroup = request.getParameter("purposeGroup");
            String purposeGroupType = request.getParameter("purposeGroupType");
            String addPurposeLocation = "add-purpose.jsp?";
            boolean callbackPresent = false;
            if (StringUtils.isNotEmpty(callback)) {
                if (!callback.startsWith("/")) {
                    callback = "";
                } else {
                    callbackPresent = true;
                }
            }
    
            if (StringUtils.isNotEmpty(purposeGroup) && StringUtils.isNotEmpty(purposeGroupType) && callbackPresent) {
                addPurposeLocation = addPurposeLocation + "purposeGroup=" + purposeGroup + "&purposeGroupType=" +
                        purposeGroupType + "&callback=" + callback;
            }
        %>
        <h2><fmt:message key='title.list.purposes'/></h2>
        <div id="workArea">
            
            <script type="text/javascript">

                function removeItem(pName) {
                    function doDelete() {
                        var purposeName = pName;
                        $.ajax({
                            type: 'POST',
                            url: 'remove-purpose-finish.jsp',
                            headers: {
                                Accept: "text/html"
                            },
                            data: 'purposeName=' + purposeName,
                            async: false,
                            success: function (responseText, status) {
                                if (status == "success") {
                                    location.assign("list-purposes.jsp");
                                }
                            }
                        });
                    }

                    CARBON.showConfirmationDialog('Are you sure you want to delete "' + pName +
                        '" Purpose information?',
                        doDelete, null);
                }

                function doFinish() {
                    location.href = "<%=Encode.forJavaScript(callback)%>";
                }
                function addPurpose() {
                    location.href = "<%=Encode.forJavaScript(addPurposeLocation)%>";
                }
            </script>
            
            <%
                Purpose[] purposes = null;
                
                String BUNDLE = "org.wso2.carbon.consent.mgt.ui.i18n.Resources";
                ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
                
                try {
                    String currentUser = (String) session.getAttribute(LOGGED_USER);
                    ConsentManagementServiceClient serviceClient = new ConsentManagementServiceClient(currentUser);
                    purposes = serviceClient.listPurposes();
                } catch (Exception e) {
                    String message = resourceBundle.getString("error.while.listing.purpose");
                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
                }
            %>
            <table class="styledLeft" width="100%" id="ServiceProviders">
                            <thead>
                            <tr style="white-space: nowrap">
                                <th class="leftCol-med"><fmt:message
                                        key="field.consent.id"/></th>
                                <th class="leftCol-big"><fmt:message
                                        key="consent.mgt.description"/></th>
                                <th class="leftCol-med"><fmt:message
                                        key="consent.mgt.group"/></th>
                                <th class="leftCol-med"><fmt:message
                                        key="consent.mgt.group.type"/></th>
                                <th style="width: 30%"><fmt:message
                                        key="consent.action"/></th>
                            </tr>
                            </thead>
                            <%
                                if (purposes != null && purposes.length > 0) {
                            %>
                            <tbody>
                            <%
                                for (Purpose purpose : purposes) {
                                    if (purpose != null) {
                            %>
                            <tr>
                                <td><%=Encode.forHtml(purpose.getName())%>
                                </td>
                                <td><%=purpose.getDescription() != null ? Encode.forHtml(purpose.getDescription()) : ""%>
                                </td>
                                <td><%=purpose.getGroup() != null ? Encode.forHtml(purpose.getGroup()) : ""%>
                                </td>
                                <td><%=purpose.getGroupType() != null ? Encode.forHtml(purpose.getGroupType()) : ""%>
                                </td>
                                <%
                                    if (DEFAULT.equals(purpose.getName())) {
                                %>
                                <td style="width: 100px; white-space: nowrap;">
                                </td>
                                <%
                                } else {
                                %>
    
                                <td style="width: 100px; white-space: nowrap;"><a
                                        title="View PII Categories"
                                        href="view-pii-category.jsp?purposeId=<%=Encode.forUriComponent(String.valueOf(purpose.getId()))%>&purposeName=<%=Encode.forUriComponent(purpose.getName())%>"
                                        class="icon-link"
                                        style="background-image: url(../admin/images/edit.gif)"><fmt:message
                                        key='view.pii.cat'/></a>
                                    <%
                                        if (CarbonUIUtil.isUserAuthorized(request, ConsentConstants.PERMISSION_CONSENT_MGT_DELETE)) {
                                    %>
                                    <a title="Delete Purpose"
                                       onclick="removeItem('<%=Encode.forJavaScriptAttribute(purpose.getName())%>');return
                                               false;" href="#"
                                       class="icon-link"
                                       style="background-image: url(../admin/images/delete.gif)"><fmt:message
                                            key='delete'/>
                                    </a>
                                    <%
                                        }
                                    %>
                                </td>
                                <%
            
                                    }
    
                                %>
                            </tr>
                            <%
                                    }
                                }
                            %>
                            <%if (callbackPresent) {%>
                            <tr>
                                <td colspan="5" class="buttonRow">
                                    <input type="button" class="button" value="<fmt:message key="finish"/>"
                                           onclick="doFinish();"/>
                                    <input type="button" class="button" value="<fmt:message key="add.new.purpose"/>"
                                           onclick="addPurpose();"/>
                                </td>
                            </tr>
                            <%}%>
                            
                            </tbody>
                            <% } else { %>
                            <tbody>
                            <tr>
                                <td colspan="3"><i><fmt:message key='no.purpose.registered'/></i></td>
                            </tr>
                            </tbody>
                            <% } %>
                        </table>

        </div>
    </div>
</fmt:bundle>