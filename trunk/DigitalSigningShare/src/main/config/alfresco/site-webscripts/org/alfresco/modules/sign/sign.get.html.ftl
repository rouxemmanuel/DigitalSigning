<#assign el=args.htmlid?html>
<div id="${el}-dialog" class="depot-casier">
   <div id="${el}-dialogTitle" class="hd">${msg("title", displayName)}</div>
   <div class="bd">
     <form id="${el}-form" action="" method="post">
	 	<input type="hidden" name="document" id="${el}-document" value="${nodeRef}" />
	 	<input type="hidden" name="pathNodeRef" id="${el}-pathNodeRef" />
		<div class="yui-gd">
            <div class="yui-u first"><label for="${el}-destination">${msg("label.destination")}:</label></div>
            <div class="yui-u">
               <button type="button" name="-" id="${el}-selectFilterPath-button">${msg("label.browse")}</button> * 
               <br /><span id="${el}-filterPathView"></span>
            </div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-password">${msg("label.password")}:</label></div>
            <div class="yui-u"><input id="${el}-password" type="password" name="password" value="" /> * </div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-reason">${msg("label.reason")}:</label></div>
            <div class="yui-u"><input id="${el}-reason" type="text" name="reason" value="" /></div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-location">${msg("label.location")}:</label></div>
            <div class="yui-u"><input id="${el}-location" type="text" name="location" value="${user.location}" /></div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-contact">${msg("label.contact")}:</label></div>
            <div class="yui-u"><input id="${el}-contact" type="text" name="contact" value="${user.firstName} ${user.lastName}" /></div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-position">${msg("label.position")}:</label></div>
            <div class="yui-u">
            	<select name="position" id="${el}-position">
            		<option value="custom">${msg("label.custom")}</option>
            		<option value="topleft">${msg("label.topleft")}</option>
            		<option value="topright">${msg("label.topright")}</option>
            		<option value="center">${msg("label.center")}</option>
            		<option value="bottomleft">${msg("label.bottomleft")}</option>
            		<option value="bottomright">${msg("label.bottomright")}</option>
            	</select>
            </div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-page">${msg("label.page")}:</label></div>
            <div class="yui-u">
            	<select name="page" id="${el}-page">
            		<option value="first">${msg("label.first")}</option>
            		<option value="all">${msg("label.all")}</option>
            		<option value="last">${msg("label.last")}</option>
            		<option value="odd">${msg("label.odd")}</option>
            		<option value="even">${msg("label.even")}</option>
            	</select>
            </div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-field">${msg("label.field")}:</label></div>
            <div class="yui-u"><input id="${el}-field" type="text" name="field" value="" /></div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-locationX">${msg("label.locationX")}:</label></div>
            <div class="yui-u"><input id="${el}-locationX" type="text" name="locationX" value="" /></div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-locationY">${msg("label.locationY")}:</label></div>
            <div class="yui-u"><input id="${el}-locationY" type="text" name="locationY" value="" /></div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-marginX">${msg("label.marginX")}:</label></div>
            <div class="yui-u"><input id="${el}-marginX" type="text" name="marginX" value="" /></div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-marginY">${msg("label.marginY")}:</label></div>
            <div class="yui-u"><input id="${el}-marginY" type="text" name="marginY" value="" /></div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-height">${msg("label.height")}:</label></div>
            <div class="yui-u"><input id="${el}-height" type="text" name="height" value="" /></div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-width">${msg("label.width")}:</label></div>
            <div class="yui-u"><input id="${el}-width" type="text" name="width" value="" /></div>
         </div>
	 	<div class="bdft">
	    	<input type="submit" id="${el}-ok" value="${msg("button.ok")}" tabindex="0" />
	    	<input type="button" id="${el}-cancel" value="${msg("button.cancel")}" tabindex="0" />
	 	</div>
     </form>
   </div>
</div>