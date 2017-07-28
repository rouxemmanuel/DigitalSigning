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
	<#if aliasList??>
	{
		"aliasList": "${aliasList}"
	}
	</#if>
</#if>
</#escape>