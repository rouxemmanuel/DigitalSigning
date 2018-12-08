model.nodeRef = args.nodeRef;
if (args.multiple == "false") {
	model.displayName = args.displayName;
}


if (args.displayName.indexOf(".pdf", args.displayName.length - ".pdf".length) !== -1) {
	model.pdf = true;
	model.xml = false;
}else if (args.displayName.indexOf(".xml", args.displayName.length - ".xml".length) !== -1) {
	model.pdf = false;
	model.xml = true;
} else {
	model.pdf = false;
	model.xml = true;
}