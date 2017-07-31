<#escape x as jsonUtils.encodeJSONString(x)>
{
	"results":
   	[
	<#list results as result>
		{
			"name":"${result.name}",
			"signatureCoversWholeDocument":"${result.signatureCoversWholeDocument?string("true","false")}",
			"revision":"${result.revision}",
			"totalRevision":"${result.totalRevision}",
			"isSignValid":"${result.isSignValid?string("true","false")}",
			"failReason":"${result.failReason}",
			"signReason":"${result.signReason}",
			"signLocation":"${result.signLocation}",
			"signDate":"${result.signDate?datetime}",
			"signName":"${result.signName}",
			"signVersion":"${result.signVersion}",
			"signInformationVersion":"${result.signInformationVersion}",
			"signSubject":"${result.signSubject}",
			"isDocumentModified":"${result.isDocumentModified?string("true","false")}"
		}
	<#if result_has_next>,</#if>
	</#list>
	]
}
</#escape>