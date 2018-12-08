<#escape x as jsonUtils.encodeJSONString(x)>
{
	"result":"${result}"
	<#if error??>
	,"error":"${error}"
	</#if>
}
</#escape>