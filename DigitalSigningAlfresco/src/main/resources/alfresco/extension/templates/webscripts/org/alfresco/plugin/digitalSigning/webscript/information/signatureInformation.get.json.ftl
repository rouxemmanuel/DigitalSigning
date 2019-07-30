<#escape x as jsonUtils.encodeJSONString(x)>
<#if errorNumber??>
{
	"errorNumber":"${errorNumber}"
	<#if errorMessage??>
	,
	"errorMessage":"${errorMessage}"
	</#if>
}
<#else>
	<#if keyInfos??>
	{
		"key":
			{
				"alias":"${keyInfos.alias}",
				"subject":"${keyInfos.subject}",
				"type":"${keyInfos.type}",
				"algorithm":"${keyInfos.algorithm}",
				"firstDayValidity":"${keyInfos.firstDayValidity?string("dd-MM-yyyy HH:mm:ss")}",
				"lastDayValidity":"${keyInfos.lastDayValidity?string("dd-MM-yyyy HH:mm:ss")}",
				"alert":"${keyInfos.alert}",
				"hasExpired":${keyInfos.hasExpired?string("true","false")}
				<#if keyInfos.expire??>
				,"expire":"${keyInfos.expire}"
				</#if>
			},
		"hasImage": ${hasImage?string("true","false")}
	}
	</#if>
</#if>
</#escape>