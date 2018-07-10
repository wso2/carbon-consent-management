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
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.consent.mgt.ui.client.ClaimMetadataAdminClient" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimPropertyDTO" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.LocalClaimDTO" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="static org.wso2.carbon.consent.mgt.ui.constant.ClaimMgtUIConstants.CLAIM_URI" %>
<%@ page import="static org.wso2.carbon.consent.mgt.ui.constant.ClaimMgtUIConstants.DESCRIPTION" %>
<%@ page import="static org.wso2.carbon.consent.mgt.ui.constant.ClaimMgtUIConstants.DISPLAY_NAME" %>
<%@ page import="org.owasp.encoder.Encode" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    ClaimMetadataAdminClient client = null;
    String BUNDLE = "org.wso2.carbon.consent.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    List<LocalClaimDTO> claims = new ArrayList<LocalClaimDTO>();
    try {
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext)
                config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        
        client = new ClaimMetadataAdminClient(cookie, serverURL, configContext);
        LocalClaimDTO[] localClaims = client.getLocalClaims();
        if (localClaims != null) {
            claims.addAll(Arrays.asList(localClaims));
        }
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("error.while.loading.claim.info"),
                e.getMessage());
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
    }
%>

<fmt:bundle basename="org.wso2.carbon.consent.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="add.consent"
                       resourceBundle="org.wso2.carbon.consent.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <style>
        #claimAddTable tbody tr td{
            border: 1px solid #cccccc!important;
        }
    </style>
    <script type="text/javascript">
        function doFinish() {
            document.dataForm.action = "add-finish-purpose.jsp";
            if (doValidation() === true) {
                document.dataForm.submit();
            }
        }

        function doValidation() {
            var reason = "";
            reason = validateEmpty("purposeName");
            if (reason != "") {
                CARBON.showWarningDialog("Purpose name cannot be empty");
                return false;
            }
            
            reason = validateEmpty("group");
            
            if (reason != "") {
                CARBON.showWarningDialog("Purpose group cannot be empty");
                return false;
            }

            reason = validateEmpty("groupType");
            
            if (reason != "") {
                CARBON.showWarningDialog("Purpose group type cannot be empty");
                return false;
            }

            return true
        }

        function doCancel() {
            location.href = 'list-purposes.jsp?region=region1&item=list_consent_menu';
        }

        var deleteClaimRows = [];
        function deleteClaimRow(obj) {
            if (jQuery(obj).parent().prev().children()[0].value != '') {
                deleteClaimRows.push(jQuery(obj).parent().prev().children()[0].value);
            }
            jQuery(obj).parent().parent().remove();
            if ($(jQuery('#claimAddTable tr')).length == 1) {
                $(jQuery('#claimAddTable')).toggle();
            }
        }
        
        var claimRowId = -1;
        jQuery(document).ready(function () {
            jQuery('#claimAddLink').click(function () {
                claimRowId++;
                var option = '<option value="">---Select Claim URI ---</option>';

                <% for(int i =0 ; i< claims.size() ; i++){%>
                option += "<option value='" + '<%=Encode.forHtmlAttribute(getLocalClaims(claims.get(i)))%>' + "'>" +
                    "<%=Encode.forHtmlAttribute(claims.get(i).getLocalClaimURI())%>" + '</option>';
                <%}%>
                $("#claimrow_id_count").val(claimRowId + 1);
                var newrow = jQuery('<tr><td><select class="claimrow_wso2" name="claimrow_name_wso2_' + claimRowId + '">' + option + '</select></td> ' +
                    '<td><a onclick="deleteClaimRow(this)" class="icon-link" ' +
                    'style="background-image: url(images/delete.gif)">' +
                    'Delete' +
                    '</a></td></tr>');
                jQuery('.claimrow', newrow).blur(function () {
                    claimURIDropdownPopulator();
                });
                jQuery('#claimAddTable').append(newrow);
                if ($(jQuery('#claimAddTable tr')).length == 2) {
                    $(jQuery('#claimAddTable')).toggle();
                }
            })
        });
    </script>
    
    <div id="middle">
        <h2><fmt:message key="add.new.purpose"/></h2>
        
        <div id="workArea">
            <form method="post" action="add-finish-purpose.jsp" name="dataForm" onsubmit="return doValidation();">
                
                <table class="styledLeft" id="purposeAdd" width="60%">
                    <thead>
                    <tr>
                        <th><fmt:message key="enter.purpose.details"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <table class="normal" id="mainTable" style="width: 100%;">
                                <tr>
                                    <td><fmt:message key="purpose.name"/><font color="red">*</font>
                                    </td>
                                    <td><input type="text" name="purposeName" id="purposeName"
                                               value=""
                                               style="width:150px"/></td>
                                </tr>
    
                                <tr id="descripiton">
                                    <td><fmt:message key="description"/></td>
                                    <td>
                                        <textarea type="text" name="purpose.description" id="purpose.description"
                                                  style="width:300px"></textarea>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Group<font color="red">*</font></td>
                                    <td><input type="text" name="group" id="group"
                                                value=""
                                                style="width:150px"/></td>
                                </tr>
                                <tr>
                                    <td>Group Type<font color="red">*</font>
                                    </td>
                                    <td><input type="text" name="groupType" id="groupType"
                                                value=""
                                                style="width:150px"/></td>
                                </tr>
                                <tr>
                                    <td>Mandatory</td>
                                    <td><input type="checkbox" id="isPurposeMandatory" name="isPurposeMandatory"/></td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med labelField customClaim"><fmt:message key='pii.categories'/>:</td>
                                    <td class="customClaim">
                                        <a id="claimAddLink" class="icon-link"
                                           style="margin-left:0;background-image:url(images/add.gif);"><fmt:message
                                                key='add.pii.cat'/></a>
                                        
                                        <div style="clear:both"></div>
                                        <div class="sectionHelp">
                                            <fmt:message key='piiCat.help'/>
                                        </div>
                                        <table class="styledLeft" id="claimAddTable" style="display:none">
                                            <thead>
                                            <tr>
                                                <th><fmt:message key='wso2.pii.cat'/></th>
                                                <th><fmt:message key='actions'/></th>
                                            </tr>
                                            </thead>
                                        </table>
                                    </td>
                                </tr>
    
                                <tr>
                                    <td>
                                        <input type="hidden" id="claimrow_id_count" name="claimrow_name_count" value="0">
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" class="button" value="<fmt:message key="finish"/>"
                                   onclick="doFinish();"/>
                            <input type="button" class="button" value="<fmt:message key="cancel"/>"
                                   onclick="doCancel();"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
                
            </form>
        </div>
        <p>&nbsp;</p>
    </div>
</fmt:bundle>
<%!
    private String getLocalClaims(LocalClaimDTO localClaimDTO) {
    
        ClaimPropertyDTO[] claimProperties = localClaimDTO.getClaimProperties();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CLAIM_URI, localClaimDTO.getLocalClaimURI());
        if (claimProperties != null) {
            for (ClaimPropertyDTO claimPropertyDTO : claimProperties) {
                if (DESCRIPTION.equalsIgnoreCase(claimPropertyDTO.getPropertyName())) {
                    jsonObject.put(DESCRIPTION, claimPropertyDTO.getPropertyValue());
                } else if (DISPLAY_NAME.equalsIgnoreCase(claimPropertyDTO.getPropertyName())) {
                    jsonObject.put(DISPLAY_NAME, claimPropertyDTO.getPropertyValue());
                }
            }
        }
        return jsonObject.toString();
    }
%>