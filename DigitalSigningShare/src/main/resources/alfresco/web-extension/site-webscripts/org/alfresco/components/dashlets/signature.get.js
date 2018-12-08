function main()
{
	var json = remote.call("/api/digitalSigning/signatureInformation");

    if (json.status == 200) {
         var obj = eval('(' + json + ')');
         if (obj) {
        	if (obj.key != null) {
        		model.keyInfos = obj.key;
        	}
        	if (obj.hasImage != null) {
        		model.hasImage = obj.hasImage;
        	}
            if (obj.errorNumber != null) {
            	model.errorNumber = obj.errorNumber;
            }
            if (obj.errorMessage != null) {
            	model.errorMessage = obj.errorMessage;
            }
         }
    }
   
    // Widget instantiation metadata...
    var signature = {
      id : "Signature", 
      name : "Alfresco.dashlet.Signature",
      assignTo : "signature",
      options : {
         componentId : instance.object.id
      }
    };

    var dashletResizer = {
      id : "DashletResizer", 
      name : "Alfresco.widget.DashletResizer",
      initArgs : ["\"" + args.htmlid + "\"", "\"" + instance.object.id + "\""],
      useMessages: false
    };
   
    var dashletTitleBarActions = {
      id : "DashletTitleBarActions", 
      name : "Alfresco.widget.DashletTitleBarActions",
      useMessages : false,
      options : {
          actions: [
             {
            	 cssClass: "edit",
        	     eventOnClick: { 
        	    	 	_alfValue : "editSignatureDashletEvent", 
        	    	 	_alfType: "REFERENCE"}, 
        	     tooltip: msg.get("dashlet.signature.edit.tooltip")
             },
             {
            	 cssClass: "help",
       		  bubbleOnClick:
       		  {
       			  message: msg.get("dashlet.help")
       		  },
       		  tooltip: msg.get("dashlet.help.tooltip")
             }
          ]
       }
    };
    model.widgets = [signature, dashletResizer, dashletTitleBarActions];
}

main();