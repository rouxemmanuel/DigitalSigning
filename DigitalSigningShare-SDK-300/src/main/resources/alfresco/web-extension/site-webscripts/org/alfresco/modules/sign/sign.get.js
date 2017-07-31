model.nodeRef = args.nodeRef;
if (args.multiple == "false") {
	model.displayName = args.displayName;
}

if (args.displayName.indexOf(".xml", args.displayName.length - ".xml".length) !== -1) {
	model.xml = true;
} else {
	model.xml = false;
}