<#escape x as jsonUtils.encodeJSONString(x)>
<#if errorNumber??>
{
	"errorNumber":"${errorNumber}"
	<#if errorMessage??>
	,
	"errorMessage":"${errorMessage}"
	</#if>
}
</#if>
</#escape>