<#assign el=args.htmlid?html>
<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/dashlets/signature.css" group="dashlets"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/components/dashlets/signature.js" group="dashlets"/>
   <@script type="text/javascript" src="${url.context}/res/modules/simple-dialog.js" group="dashlets"/>
</@>

<@markup id="widgets">
   <@inlineScript group="dashlets">
      var editSignatureDashletEvent = new YAHOO.util.CustomEvent("onDashletConfigure");
   </@>
   <@createWidgets group="dashlets"/>
   <@inlineScript group="dashlets">
      editSignatureDashletEvent.subscribe(signature.onConfigSignatureClick, signature, true);
   </@>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <div class="dashlet signature">
         <div class="title" id="${el}-title">${msg("label.header")}</div>
         <div id="${el}-signatures" class="body scrollablePanel yui-dt" style="height: 228px;">
			<div class="yui-dt-mask" style="display: none;"></div>
			<table summary="" id="yui-signatureInfos" style="border:0; width: 100%;">
				<tbody tabindex="0" class="yui-dt-data" style="">
					<tr class="yui-dt-rec yui-dt-first yui-dt-even" id="yui-errors" <#if errorNumber??>style=""<#else>style="display:none;"</#if> >
						<td class="yui-dt14-col-icon yui-dt-col-icon yui-dt-first" id="yui-errorImage" style="width: 52px;border-right: 0px">
							<div class="yui-dt-liner" id="yui-errorImageKey" <#if errorNumber??>style="width: 52px;"<#else>style="width: 52px; display:none;"</#if>>
								<img src="${url.context}/res/components/images/help-key-error-bw-32.png" id="yui-errorKeyImage">
							</div>
						</td>
						<td class="yui-dt14-col-detail yui-dt-col-detail" id="yui-errorMessage" style="border-right: 0px;">
							<div class="yui-dt-liner" id="yui-errorMessageText" <#if errorNumber??>style=""<#else>style="display:none;"</#if>>
								<div class="empty" id="yui-errorMessageText2">
									<h3 id="yui-errorMessageTextH3">
									<#if errorNumber?? && errorNumber == "1">
										${msg("signature.noKey")}
									</#if>
									<#if errorNumber?? && errorNumber == "2">
										${msg("signature.error")}
									</#if>
									<#if errorNumber?? && errorNumber == "3">
										${msg("signature.noAlias")}
									</#if>
									</h3>
									<span id="yui-technicalErrorText">
										<#if errorNumber?? && errorNumber == "2">
											<#if errorMessage??>${errorMessage}</#if>
										</#if>
									</span>
								</div>
							</div>
						</td>
					</tr>
					
     				<tr id="yui-expireTr" <#if keyInfos?? && ((keyInfos.hasExpired?? && keyInfos.hasExpired == true) || keyInfos.expire??)>style=""<#else>style="display:none;"</#if> >
     					<td colspan="2" class="yui-dt-empty" style="text-align: center; border-right: 0px;">
     						<div class="yui-dt-liner" id="yui-expireMessageText">
     							<img src="${url.context}/res/components/images/warning-sign-key-16.png" />
     							<#if keyInfos??>
     								<#if keyInfos.hasExpired??>
     									<#if keyInfos.hasExpired>
     										${msg("signature.warning.end")}<br />
     									</#if>
     								</#if>
     								<#if keyInfos.expire??>
     									${msg("signature.warning.day", keyInfos.expire)}<br />
     								</#if>
     							</#if>
     						</div>
     					</td>
     				</tr>
					<tr class="yui-dt-rec yui-dt-first yui-dt-even" id="yui-keyInfosTr" <#if keyInfos??>style=""<#else>style="display:none;"</#if> >
						<td class="yui-dt14-col-icon yui-dt-col-icon yui-dt-first" id="yui-keyInfosTd" style="width: 52px;border-right: 0px">
							<div class="yui-dt-liner" id="yui-keyInfosImageDiv" style="width: 52px;">
								<#if keyInfos??><img src="${url.context}/res/components/images/help-key-bw-32.png" id="yui-keyInfosImage"></#if>
							</div>
						</td>
						<td class="yui-dt14-col-detail yui-dt-col-detail" id="yui-keyInfosTextTd" style="border-right: 0px;">
							<div class="yui-dt-liner" id="yui-keyInfosTextDiv">
								<div class="empty" id="yui-keyInfosTextContent">
									<span id="yui-keyInfosTypeLabel" <#if keyInfos??>style=""<#else>style="display:none;"</#if> ><b>${msg("signature.type")}</b></span>
									<span id="yui-keyInfosType"><#if keyInfos??>${keyInfos.type}<br /></#if></span>
									<span id="yui-keyInfosAliasLabel" <#if keyInfos??>style=""<#else>style="display:none;"</#if> ><b>${msg("signature.alias")}</b></span>
									<span id="yui-keyInfosAlias"><#if keyInfos??>${keyInfos.alias}<br /></#if></span>
									<span id="yui-keyInfosSubjectLabel" <#if keyInfos??>style=""<#else>style="display:none;"</#if> ><b>${msg("signature.subject")}</b></span>
									<span id="yui-keyInfosSubject"><#if keyInfos??>${keyInfos.subject}<br /></#if></span>
									<span id="yui-keyInfosAlgorithmLabel" <#if keyInfos??>style=""<#else>style="display:none;"</#if> ><b>${msg("signature.algorithm")}</b></span>
									<span id="yui-keyInfosAlgorithm"><#if keyInfos??>${keyInfos.algorithm}<br /></#if></span>
									<span id="yui-keyInfosFirstDayLabel" <#if keyInfos??>style=""<#else>style="display:none;"</#if> ><b>${msg("signature.firstDayValidity")}</b></span>
									<span id="yui-keyInfosFirstDay"><#if keyInfos??>${keyInfos.firstDayValidity}<br /></#if></span>
									<span id="yui-keyInfosLastDayLabel" <#if keyInfos??>style=""<#else>style="display:none;"</#if> ><b>${msg("signature.lastDayValidity")}</b></span>
									<span id="yui-keyInfosLastDay"><#if keyInfos??>${keyInfos.lastDayValidity}<br /></#if></span>
									<span id="yui-keyInfosAlertLabel" <#if keyInfos??>style=""<#else>style="display:none;"</#if> ><b>${msg("signature.alert")}</b></span>
									<span id="yui-keyInfosAlert"><#if keyInfos??>${keyInfos.alert}</#if></span>
								</div>
							</div>
						</td>
					</tr>
					<tr class="yui-dt-rec yui-dt-last yui-dt-even" id="yui-imageInfosTr" <#if hasImage?? && hasImage>style=""<#else>style="display:none;"</#if> >
						<td class="yui-dt14-col-icon yui-dt-col-icon yui-dt-first" style="width: 52px;border-right: 0px">
							<div class="yui-dt-liner" style="width: 52px;" id="yui-imageInfosImageDiv">
								<#if hasImage?? && hasImage><img src="${url.context}/res/components/images/help-signing-bw-32.png" id="yui-imageInfosIcon"></#if>
							</div>
						</td>
						<td class="yui-dt14-col-detail yui-dt-col-detail" id="yui-imageInfosTd" style="border-right: 0px;">
							<div class="yui-dt-liner" id="yui-imageInfosDiv">
								<span id="yui-imageInfosImage"><#if hasImage?? && hasImage><img src="${url.context}/proxy/alfresco/api/digitalSigning/image" alt="${msg("signature.image.alt")}" title="${msg("signature.image.alt")}" border="0" width="200" /></#if></span>
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		 </div>
      </div>
   </@>
</@>