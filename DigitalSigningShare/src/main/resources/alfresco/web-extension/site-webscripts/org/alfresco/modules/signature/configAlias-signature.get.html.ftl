<#assign el=args.htmlid?html>
<div id="${el}-configDialog">
   <div class="hd">${msg("label.title")}</div>
   <div class="bd">
      <form id="${el}-form" action="" method="POST" enctype="multipart/form-data">
         <div class="yui-gd">
         	<div class="yui-u first"><label for="${el}-signaturePassword">${msg("label.password")}</label></div>
            <div class="yui-u"><input id="${el}-signaturePassword" type="password" name="password" tabindex="2" />&nbsp;*</div>
            <div class="yui-u first"><label for="${el}-signatureAlias">${msg("label.alias")}</label></div>
            <div class="yui-u">
            	<select id="${el}-signatureAlias" name="alias" style="width: auto;">
            		<#list aliasList as alias>
    					<option value="${alias}">${alias}</option>
					</#list>
				</select>
            </div>
         </div>
         <div class="bdft">
            <input type="submit" id="${el}-ok" value="${msg("button.ok")}" tabindex="0" />
            <input type="button" id="${el}-cancel" value="${msg("button.cancel")}" tabindex="0" />
         </div>
      </form>
   </div>
</div>