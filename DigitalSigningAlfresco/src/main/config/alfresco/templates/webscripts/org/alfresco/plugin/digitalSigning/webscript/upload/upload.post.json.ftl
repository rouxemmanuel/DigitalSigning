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
				"alias":"${signingKey.properties["dgtsgn:keyAlias"]}",
				"subject":"${signingKey.properties["dgtsgn:keySubject"]}",
				"type":"${signingKey.properties["dgtsgn:keyType"]}",
				"algorithm":"${signingKey.properties["dgtsgn:keyAlgorithm"]}",
				"firstDayValidity":"${signingKey.properties["dgtsgn:keyFirstValidity"]?string("dd-MM-yyyy HH:mm:ss")}",
				"lastDayValidity":"${signingKey.properties["dgtsgn:keyLastValidity"]?string("dd-MM-yyyy HH:mm:ss")}",
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