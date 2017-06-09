<#assign el=args.htmlid?html>
<div id="${el}-configDialog">
   <div class="hd">${msg("label.title")}</div>
   <div class="bd">
      <form id="${el}-form" action="" method="POST" enctype="multipart/form-data">
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-signatureKey">${msg("label.key")}</label></div>
            <div class="yui-u"><input id="${el}-signatureKey" type="file" name="key" tabindex="0" />&nbsp;*</div>
           
           	<div class="yui-u first"><label for="${el}-signatureType">${msg("label.type")}</label></div>
            <div class="yui-u"><select id="${el}-signatureType" name="keyType" tabindex="1"><option value="X.509">X.509</option></select>&nbsp;*</div>
           
            <div class="yui-u first"><label for="${el}-signaturePassword">${msg("label.password")}</label></div>
            <div class="yui-u"><input id="${el}-signaturePassword" type="password" name="password" tabindex="2" />&nbsp;*</div>
            <!--
            <div class="yui-u first"><label for="${el}-signatureAlias">${msg("label.alias")}</label></div>
            <div class="yui-u"><input id="${el}-signatureAlias" type="text" name="alias" tabindex="3" />&nbsp;*</div>
            -->
            <div class="yui-u first"><label for="${el}-signatureImage">${msg("label.image")}</label></div>
            <div class="yui-u"><input id="${el}-signatureImage" type="file" name="image" tabindex="4" />&nbsp;</div>
            
            <div class="yui-u first"><label for="${el}-signatureAlert">${msg("label.alert")}</label></div>
            <div class="yui-u"><select id="${el}-signatureAlert" name="alert"><option value="0">${msg("label.option.none")}</option><option value="1">${msg("label.option.1")}</option><option value="3">${msg("label.option.3")}</option><option value="6">${msg("label.option.6")}</option></select>&nbsp;</div>
         </div>
         <div class="bdft">
            <input type="submit" id="${el}-ok" value="${msg("button.ok")}" tabindex="0" />
            <input type="button" id="${el}-cancel" value="${msg("button.cancel")}" tabindex="0" />
         </div>
      </form>
   </div>
</div>