try {
	var jsonObject = jsonUtils.toObject(requestbody.content);

	var document = jsonObject.document;
	var destination = jsonObject.pathNodeRef;
	var keyPassword = jsonObject.password;
	var reason = jsonObject.reason;
	var location = jsonObject.location;
	var contact = jsonObject.contact;
	var position = jsonObject.position;
	var field = jsonObject.field;
	var page = jsonObject.page;
	var locationX = jsonObject.locationX;
	var locationY = jsonObject.locationY;
	var marginX = jsonObject.marginX;
	var marginY = jsonObject.marginY;
	var height = jsonObject.height;
	var width = jsonObject.width;
	var pageNumber = jsonObject.pageNumber;
	var depth = "over";
	
	var parameters = new Object();
	parameters.keyPassword=keyPassword;
	parameters.document=document;
	parameters.destination=destination;
	parameters.reason=reason;
	parameters.location=location;
	parameters.contact=contact;
	parameters.position=position;
	parameters.field=field;
	parameters.page=page;
	parameters.locationX=locationX;
	parameters.locationY=locationY;
	parameters.marginX=marginX;
	parameters.marginY=marginY;
	parameters.height=height;
	parameters.width=width;
	parameters.depth=depth;
	parameters.pageNumber=pageNumber;
	
	digitalSigning.sign(parameters);
	model.result = "success";
} catch (e) {
	model.result = "error";
	model.error = (e.javaException == null ? e.rhinoException.message : e.javaException.message);
}