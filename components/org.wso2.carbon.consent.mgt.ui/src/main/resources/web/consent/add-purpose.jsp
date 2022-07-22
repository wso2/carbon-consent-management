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
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="org.wso2.carbon.consent.mgt.core.model.Purpose" %>
<%@ page import="org.wso2.carbon.consent.mgt.ui.client.ConsentManagementServiceClient" %>
<%@ page import="org.json.JSONArray" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    ClaimMetadataAdminClient client = null;
    String BUNDLE = "org.wso2.carbon.consent.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    List<LocalClaimDTO> claims = new ArrayList<LocalClaimDTO>();
    String purposeGroup = request.getParameter("purposeGroup");
    String purposeGroupType = request.getParameter("purposeGroupType");
    boolean isPurposeGroupPresent = false;
    boolean isPurposeGroupTypePresent = false;
    String callback = request.getParameter("callback");
    String addFinishPurposePage = "add-finish-purpose.jsp";
    String purposeIdList = request.getParameter("purposeIdList");
    boolean hasPurposeWithMandatoryEmailInList = false;
    String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    
    if (StringUtils.isNotEmpty(callback)) {
        if (!callback.startsWith("/")) {
            callback = "";
        } else {
            addFinishPurposePage = addFinishPurposePage + "?callback=" + URLEncoder.encode(callback,
                    StandardCharsets.UTF_8.name());
        }
    }
    if (StringUtils.isNotEmpty(purposeGroup)) {
        isPurposeGroupPresent = true;
        addFinishPurposePage = addFinishPurposePage + "&purposeGroup=" + purposeGroup;
    }
    if (StringUtils.isNotEmpty(purposeGroupType)) {
        isPurposeGroupTypePresent = true;
        addFinishPurposePage = addFinishPurposePage + "&purposeGroupType=" + purposeGroupType;
    }
    
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

        String currentUser = (String) session.getAttribute("logged-user");
        ConsentManagementServiceClient serviceClient = new ConsentManagementServiceClient(currentUser);

        JSONArray purposeIdListParsed = new JSONArray(purposeIdList);
        for (int i = 0; i < purposeIdListParsed.length(); i++) {
            Purpose retrievedPurpose = serviceClient.getPurpose(purposeIdListParsed.getInt(i));
            hasPurposeWithMandatoryEmailInList =
                    retrievedPurpose.getPurposePIICategories().stream().anyMatch(purposePIICategory ->
                            purposePIICategory.getName().equals(EMAIL_CLAIM_URI) &&
                                    purposePIICategory.getMandatory());

            if (hasPurposeWithMandatoryEmailInList) {
                break;
            }
        }
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("error.while.loading.claim.info"),
                e.getMessage());
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
    }
%>

<fmt:bundle basename="org.wso2.carbon.consent.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="add.new.purpose"
                       resourceBundle="org.wso2.carbon.consent.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <style>
        #claimAddTable tbody tr td{
            border: 1px solid #cccccc!important;
        }
    </style>
    <script type="text/javascript">
        function doFinish() {
            document.dataForm.action = "<%=Encode.forJavaScript(addFinishPurposePage)%>";
            if (doValidation() === true) {
                if (!doValidationForMandatoryEmailPIICategory() && !<%=hasPurposeWithMandatoryEmailInList%>) {
                    CARBON.showWarningDialog("<%=resourceBundle.getString("missing.mandatory.email.pii.category.warning.add.purpose")%>",
                        doSubmit, doSubmit);
                } else {
                    doSubmit();
                }
            }
            function doSubmit() {
                doEncode();
                document.dataForm.submit();
            }
        }

        function doEncode() {
            var claimRawCount = $('#claimrow_id_count').val();
            for (let i = 0; i < claimRawCount; i++) {
                var claimValue = $("[name=claimrow_name_wso2_" + i + "] option:selected").val();
                $("[name=claimrow_name_wso2_" + i + "] option:selected").val(btoa(claimValue));
            }
        }

        function doValidationForMandatoryEmailPIICategory() {
            var count = document.getElementsByName("claimrow_name_count")[0];
            for (let i = 0; i < count.value; i++) {
                var claim = document.getElementsByName("claimrow_name_wso2_" + i)
                var claimInfo = JSON.parse(claim[0].value);
                var claimURL = claimInfo.ClaimURI;
                var isMandatory =  document.getElementsByName("claimrow_mandatory_"+ i);
                if(claimURL === "<%=EMAIL_CLAIM_URI%>" && isMandatory[0].checked) {
                    return true;
                }
            }
            return false;
        }

        function validateNonEmptyPIICategories() {
            var count = document.getElementsByName("claimrow_name_count")[0];
            for (let i = 0; i < count.value; i++) {
                var claim = document.getElementsByName("claimrow_name_wso2_" + i);
                if (claim && claim[0] && !claim[0].value){
                    return false;
                }
            }
            return true;
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

            if (!validateNonEmptyPIICategories()) {
                CARBON.showWarningDialog("Claim URI must be selected for all added PII categories");
                return false;
            }

            return true
        }

        function doCancel() {
            <% if(StringUtils.isNotEmpty(callback)) {%>
            location.href = '<%=Encode.forJavaScript(callback)%>';
            <%} else {%>
            location.href = 'list-purposes.jsp?region=region1&item=list_consent_menu';
            <%}%>
        }

        function deleteClaimRow(obj) {
            var currentDeletingRowName = jQuery(obj).parent().prev().prev().children()[0].name;
            var currentDeletingRowId = extractClaimRowIdFromName(currentDeletingRowName);

            for (let i = currentDeletingRowId + 1; i < claimRowId + 1; i++) {
                $("[name=claimrow_name_wso2_" + i + "]").attr('name', 'claimrow_name_wso2_' + (i - 1));
                $("[name=claimrow_mandatory_" + i + "]").attr('name', 'claimrow_mandatory_' + (i - 1));
            }

            claimRowId--;
            $("#claimrow_id_count").val(claimRowId + 1);

            jQuery(obj).parent().parent().remove();
            if ($(jQuery('#claimAddTable tr')).length == 1) {
                $(jQuery('#claimAddTable')).toggle();
            }
        }

        function extractClaimRowIdFromName(rowName) {
            nameArr = rowName.split("_");
            return Number(nameArr[nameArr.length - 1]);
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
                    '<td><input type="checkbox" name="claimrow_mandatory_'+ claimRowId + '">' +
                    '</td><td><a onclick="deleteClaimRow(this)" class="icon-link" ' +
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
        <% if (StringUtils.isNotEmpty(purposeGroup)) {%>
        <h2><%=Encode.forHtmlContent(MessageFormat.format(resourceBundle.getString("add.new.purpose.for"),
                StringUtils.isEmpty(purposeGroup) ? "" : purposeGroup))%>
        </h2>
        <%} else {%>
        <h2><%=Encode.forHtmlContent(resourceBundle.getString("add.new.purpose"))%>
        </h2>
        <%}%>
        
        <div id="workArea">
            <form method="post" action="<%=Encode.forHtmlAttribute(addFinishPurposePage)%>" name="dataForm" onsubmit="return
            doValidation();">
                
                <table class="styledLeft" id="purposeAdd" width="60%" >
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
                                    <td style="vertical-align: top!important;"><fmt:message key="purpose.name"/><font color="red">*</font>
                                    </td>
                                    <td><input type="text" name="purposeName" id="purposeName"
                                               value=""
                                               style="width:300px"/>
                                        <div class="sectionHelp" style="margin-top: 5px;">
                                            <fmt:message key='purpose.name.help'/>
                                        </div>
                                    </td>
                                </tr>
    
                                <tr id="descripiton">
                                    <td style="vertical-align: top!important;"><fmt:message key="description"/></td>
                                    <td>
                                        <textarea type="text" name="purpose.description" id="purpose.description"
                                                  style="width:300px"></textarea>
                                        <div class="sectionHelp" style="margin-top: 5px;">
                                            <fmt:message key='purpose.desc.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <tr <%if (isPurposeGroupPresent) {%> style="display:none" <%}%>>
                                    <td style="vertical-align: top!important;"><fmt:message key="purpose.flow"/><%if(!isPurposeGroupPresent) {%><font color="red">*</font> <%}%></td>
                                    <td><% if (isPurposeGroupPresent) {%>
                                        <input type="text" name="group" id="group" readOnly="true"
                                               value="<%=Encode.forHtmlAttribute(purposeGroup)%>" style="width:300px;
                                               border-style: none;"/>
                                        <%} else { %>
                                        <input type="text" name="group" id="group"
                                               value="" style="width:300px"/>
                                        <%}%>
                                        <div class="sectionHelp" style="margin-top: 5px;">
                                            <fmt:message key='associated.flow.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <tr<%if (isPurposeGroupTypePresent) {%> style="display:none" <%}%>>
                                    <td style="vertical-align: top!important;"><fmt:message key="purpose.group.type"/><%if(!isPurposeGroupTypePresent){%>
                                        <font color="red">*</font> <%}%>
                                    </td>
                                    <td><% if (isPurposeGroupTypePresent) {%>
                                        <input type="text" name="groupType" id="groupType" readOnly="true"
                                               value="<%=Encode.forHtmlAttribute(purposeGroupType)%>" style="width:300px ;
                                               border-style: none"/>
                                        <%} else { %>
                                        <input type="text" name="groupType" id="groupType"
                                               value="" style="width:150px"/>
                                        <%}%>
                                        <div class="sectionHelp" style="margin-top: 5px;">
                                            <fmt:message key='group.type.help'/>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med labelField customClaim" style="vertical-align:top!important;">
                                    <fmt:message key='pii.categories'/>:</td>
                                    <td class="customClaim">
                                        <a id="claimAddLink" class="icon-link"
                                           style="margin-left:0;margin-top:2px;background-image:url(images/add.gif);"><fmt:message
                                                key='add.pii.cat'/></a>
                                        
                                        <div style="clear:both"></div>
                                        <div class="sectionHelp">
                                            <fmt:message key='piiCat.help'/>
                                        </div>
                                        <table class="styledLeft" id="claimAddTable" style="display:none">
                                            <thead>
                                            <tr>
                                                <th><fmt:message key='wso2.pii.cat'/></th>
                                                <th><fmt:message key='mandatory'/></th>
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