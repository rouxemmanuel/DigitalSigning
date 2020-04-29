var keyPassword = args["password"];
var noderef = args["noderef"];

if (keyPassword == null) {
	status.setCode(status.STATUS_BAD_REQUEST, "password parameter is not present");
    return;
}
if (noderef == null) {
	status.setCode(status.STATUS_BAD_REQUEST, "noderef parameter is not present");
    return;
}

var parameters = new Object();
parameters.keyPassword=keyPassword;
parameters.document=noderef;

var results = digitalSigning.verify(parameters);
model.results = results;