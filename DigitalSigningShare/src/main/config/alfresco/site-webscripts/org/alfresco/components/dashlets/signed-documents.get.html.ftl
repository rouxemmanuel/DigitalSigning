<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/dashlets/signed-documents.css" group="dashlets"  />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/components/dashlets/signed-documents.js" group="dashlets"/>
</@>

<@markup id="widgets">
   <@createWidgets group="dashlets"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#assign id = args.htmlid>
      <#assign prefFilterPerson = preferences.filterPerson!"all">
      <#assign prefFilterDate = preferences.filterDate!"28">
      <#assign prefSimpleView = preferences.simpleView!true>
      <div class="dashlet signed-documents">
         <div class="title">${msg("header")}</div>
         <div class="toolbar flat-button">
            <div class="hidden">
               <span class="align-left yui-button yui-menu-button" id="${id}-user">
                  <span class="first-child">
                     <button type="button" tabindex="0"></button>
                  </span>
               </span>
               <select id="${id}-user-menu">
               <#list filterTypes as filter>
                  <option value="${filter.type?html}">${msg("filter." + filter.label)}</option>
               </#list>
               </select>
               <span class="align-left yui-button yui-menu-button" id="${id}-range">
                  <span class="first-child">
                     <button type="button" tabindex="0"></button>
                  </span>
               </span>
               <select id="${id}-range-menu">
               <#list filterRanges as filter>
                  <option value="${filter.type?html}">${msg("filter." + filter.label)}</option>
               </#list>
               </select>
               <div id="${id}-simpleDetailed" class="align-right simple-detailed yui-buttongroup inline">
                  <span class="yui-button yui-radio-button simple-view<#if prefSimpleView> yui-button-checked yui-radio-button-checked</#if>">
                     <span class="first-child">
                        <button type="button" tabindex="0" title="${msg("button.view.simple")}"></button>
                     </span>
                  </span>
                  <span class="yui-button yui-radio-button detailed-view<#if !prefSimpleView> yui-button-checked yui-radio-button-checked</#if>">
                     <span class="first-child">
                        <button type="button" tabindex="0" title="${msg("button.view.detailed")}"></button>
                     </span>
                  </span>
               </div>
               <div class="clear"></div>
            </div>
         </div>
         <div class="body scrollableList" <#if args.height??>style="height: ${args.height}px;"</#if>>
            <div id="${id}-documents"></div>
         </div>
      </div>
   </@>
</@>