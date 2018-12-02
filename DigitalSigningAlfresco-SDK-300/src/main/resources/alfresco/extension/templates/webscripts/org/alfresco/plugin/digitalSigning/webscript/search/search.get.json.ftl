<#macro dateFormat date=""><#if date?is_date>${xmldate(date)}</#if></#macro>

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"totalRecords": ${count},
   	"startIndex": 0,
   	"items":
   	[
	<#list nodes as item>
  		{
			"nodeRef": "${item.node.nodeRef}",
			"nodeType": "${shortQName(item.node.type)}",
			"type": "${item.type}",
			"mimetype": "${item.node.mimetype!""}",
			"isFolder": <#if item.linkedNode??>${item.linkedNode.isContainer?string}<#else>${item.node.isContainer?string}</#if>,
			"isLink": ${(item.isLink!false)?string},
			"fileName": "<#if item.linkedNode??>${item.linkedNode.name}<#else>${item.node.name}</#if>",
			"displayName": "<#if item.linkedNode??>${item.linkedNode.name}<#elseif item.node.hasAspect("{http://www.alfresco.org/model/content/1.0}workingcopy")>${item.node.name?replace(workingCopyLabel, "")}<#else>${item.node.name}</#if>",
			"status": "",
			"title": "${item.node.properties.title!""}",
			"description": "${item.node.properties.description!""}",
			"author": "${item.node.properties.author!""}",
			"createdOn": "<@dateFormat item.node.properties.created />",
			"createdBy": <#if item.createdBy??>"${item.createdBy.displayName}"<#else>""</#if>,
			"createdByUser": <#if item.createdBy??>"${item.createdBy.userName}"<#else>""</#if>,
			"modifiedOn": "<@dateFormat item.node.properties.modified />",
			"modifiedBy": <#if item.modifiedBy??>"${item.modifiedBy.displayName}"<#else>""</#if>,
			"modifiedByUser": <#if item.modifiedBy??>"${item.modifiedBy.userName}"<#else>""</#if>,
			"lockedBy": <#if item.lockedBy??>"${item.lockedBy.displayName}"<#else>""</#if>,
			"lockedByUser": <#if item.lockedBy??>"${item.lockedBy.userName}"<#else>""</#if>,
			"size": "${item.node.size?c}",
			"version": <#if item.node.properties["cm:versionLabel"]?exists>"${item.node.properties["cm:versionLabel"]}"<#else>"1.0"</#if>,
			"contentUrl": "api/node/content/${item.node.storeType}/${item.node.storeId}/${item.node.id}/${item.node.name?url}",
			"webdavUrl": "${item.node.webdavUrl}",
			"actionSet": "${item.actionSet}",
			<#if item.activeWorkflows??>"activeWorkflows": "<#list item.activeWorkflows as aw>${aw}<#if aw_has_next>,</#if></#list>",</#if>
			<#if item.isFavourite??>"isFavourite": ${item.isFavourite?string},</#if>
			"signatureDate": "<@dateFormat item.node.properties["dgtsgn:signaturedate"] />",
			"signedBy": "${item.node.properties["dgtsgn:signedby"]}",
			"signatureReason": <#if item.node.properties["dgtsgn:reason"]?exists>"${item.node.properties["dgtsgn:reason"]}"<#else>""</#if>,
			"signatureLocation": <#if item.node.properties["dgtsgn:location"]?exists>"${item.node.properties["dgtsgn:location"]}"<#else>""</#if>,
			"location":
			{
  				"repositoryId": "${(item.node.properties["trx:repositoryId"])!(server.id)}",
      			"site": "${item.location.site!""}",
      			"siteTitle": "${item.location.siteTitle!""}",
      			"container": "${item.location.container!""}",
      			"path": "${item.location.path!""}",
      			"file": "${item.location.file!""}",
      			"parent":
      			{
      			<#if item.location.parent??>
         			<#if item.location.parent.nodeRef??>
         				"nodeRef": "${item.location.parent.nodeRef!""}"
         			</#if>
      			</#if>
      			}
			}
  		}
  	<#if item_has_next>,</#if>
	</#list>
	]
}
</#escape>