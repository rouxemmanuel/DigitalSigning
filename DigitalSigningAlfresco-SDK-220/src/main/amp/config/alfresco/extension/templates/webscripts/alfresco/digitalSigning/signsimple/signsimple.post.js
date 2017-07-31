try {
	var jsonObject = jsonUtils.toObject(requestbody.content);

	var document = jsonObject.document;
	var destination = jsonObject.pathNodeRef;
	var keyPassword = jsonObject.password;
	var reason = jsonObject.reason;
	var location = jsonObject.location;
	var contact = jsonObject.contact;	
	var field = jsonObject.field;
	
	var parameters = new Object();
	parameters.keyPassword=keyPassword;
	parameters.document=document;
	parameters.destination=destination;
	parameters.reason=reason;
	parameters.location=location;
	parameters.contact=contact;
	parameters.field=field;
	
	if (jsonObject.image != null) {
		parameters.image=jsonObject.image;
	}
	if (jsonObject.detachedSignature != null) {
		parameters.detachedSignature=jsonObject.detachedSignature;
	}	
	if (jsonObject.transformInPdfA != null) {
		parameters.transformInPdfA=jsonObject.transformInPdfA;
	}
		
	digitalSigning.signSimple(parameters);
	model.result = "success";
} catch (e) {
	model.result = "error";
	model.error = (e.javaException == null ? e.rhinoException.message : e.javaException.message);
}