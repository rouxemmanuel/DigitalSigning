(function()
{
	/**
	 * YUI Library aliases
	 */
	 var Dom = YAHOO.util.Dom,
	 	Event = YAHOO.util.Event;

	 /**
	  * Alfresco Slingshot aliases
	  */
	  var $html = Alfresco.util.encodeHTML,
	      $combine = Alfresco.util.combinePaths;

	/**
	 * Sign a document.
	 *
	 * @method onActionSign
	 * @param record
	 *            {object} record to be actioned
	 */
	YAHOO.Bubbling.fire("registerAction",
	{
		actionName: "onActionSign",
		fn: function DL_onActionSign(record)
		{
		 var jsNode = record.jsNode,
		    nodeRef = record.nodeRef,
		    displayName = record.displayName,
		    actionUrl = Alfresco.constants.PROXY_URI + "/api/digitalSigning/sign",
		    maxLabelSize=0;

		 var resultRefs =[];
		
		  var currentId = this.id;
		
		 this.modules.sign = new Alfresco.module.SimpleDialog(this.id + "-sign").setOptions(
		 {
		    width: "50em",
		    templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/digitalSigning/sign?displayName=" + encodeURIComponent(displayName) + "&nodeRef=" + encodeURIComponent(nodeRef),
		    actionUrl: actionUrl,
		    doSetupFormsValidation:
            {
               fn: function dlA_doSetupFormsValidation(p_form)
               {
			 	p_form.addValidation(this.id + "-sign-password", Alfresco.forms.validation.mandatory, null, "change");
				
			 	//p_form.addValidation(this.id + "-sign-filterPathView", function(field, args, event, form, silent, message)
                //{
				//	if (Dom.get(currentId + "-sign-pathNodeRef").value == "") {
				//		return false;
			 	//	} else {
			 	//		return true;
			 	//	}
                //}, null, "blur", this.msg("message.validation.position"));
				
				p_form.addValidation(this.id + "-sign-position", function(field, args, event, form, silent, message)
                {
					return true;
                }, null, "change", this.msg("message.validation.position"));
				
				p_form.addValidation(this.id + "-sign-field", function(field, args, event, form, silent, message)
                {
					return true;
                }, null, "keyup", this.msg("message.validation.position"));
				
				p_form.addValidation(this.id + "-sign-locationX", function(field, args, event, form, silent, message)
                {
					if (Dom.get(currentId + "-sign-position").value == "custom" && Dom.get(currentId + "-sign-field").value == "" && Dom.get(currentId + "-sign-locationX").value == "") {			 			
						return false;
					} else {
						return true;
					}
                }, null, "keyup", this.msg("message.validation.position"));
				
			 	p_form.addValidation(this.id + "-sign-locationY", function(field, args, event, form, silent, message)
                {
					if (Dom.get(currentId + "-sign-position").value == "custom" && Dom.get(currentId + "-sign-field").value == "" && Dom.get(currentId + "-sign-locationY").value == "") {			 			
						return false;
					} else {
						return true;
					}
                }, null, "keyup", this.msg("message.validation.position"));
				
			 	p_form.setShowSubmitStateDynamically(true, false);
				
				this.modules.sign.widgets.pathNodeRef = Dom.get(this.id + "-sign-pathNodeRef"); // NodeRef
				this.modules.sign.widgets.pathView = Dom.get(this.id + "-sign-filterPathView");
				this.modules.sign.widgets.selectFilterPathButton = Alfresco.util.createYUIButton(this.modules.sign, "selectFilterPath-button", function getLatestDoc_onSelectFilterPath(e)
				{
				  if (!this.widgets.filterPathDialog)
				  {
					this.widgets.filterPathDialog = new Alfresco.module.DoclibGlobalFolder(this.id + "-selectFilterPath");
					var allowedViewModes =
					[
					 Alfresco.module.DoclibGlobalFolder.VIEW_MODE_SITE, 
					 Alfresco.module.DoclibGlobalFolder.VIEW_MODE_RECENT_SITES,
					 Alfresco.module.DoclibGlobalFolder.VIEW_MODE_FAVOURITE_SITES,
					 Alfresco.module.DoclibGlobalFolder.VIEW_MODE_REPOSITORY, 
					 Alfresco.module.DoclibGlobalFolder.VIEW_MODE_USERHOME,
					 Alfresco.module.DoclibGlobalFolder.VIEW_MODE_SHARED
					];
					this.widgets.filterPathDialog.setOptions(
					{
					  allowedViewModes: allowedViewModes,
					  siteId: this.options.siteId,
					  //containerId: this.options.containerId,
					  title: this.msg("message.sign.select.destination"),
					  nodeRef: "alfresco://company/home"
					});
					YAHOO.Bubbling.on("folderSelected", function (layer, args) {
					  var obj = args[1];
					  if (obj !== null) {
						this.widgets.pathView.innerHTML = obj.selectedFolder.path;
						this.widgets.pathNodeRef.value = obj.selectedFolder.nodeRef;
					  }
					}, this);
				  }
				  this.widgets.filterPathDialog.setOptions({
				  	pathNodeRef: this.widgets.pathNodeRef.value ? new Alfresco.util.NodeRef(this.widgets.pathNodeRef.value) : null
				  });
				  
				  // Show the dialog
				  this.widgets.filterPathDialog.showDialog();
				});
               },
               scope: this
            },
		    onSuccess:
		    {
		       fn: function dlA_onActionDepotCasier_success(response)
		       {
		    	   var responseJSON = Alfresco.util.parseJSON(response.serverResponse.responseText);
		    	   if (responseJSON.result == "success") {
		    		   Alfresco.util.PopupManager.displayMessage({
			    		   text: this.msg("message.sign.success"),
			    		   displayTime: 10
			    	   });
		    	   } else {
		    		   Alfresco.util.PopupManager.displayMessage({
			    		   text: this.msg("message.sign.error", responseJSON.error),
			    		   displayTime: 10
			    	   });
		    	   }
		       },
		       scope: this
		    },
		    onFailure:
		    {
		       fn: function dlA_onActionDepotCasier_failure(response)
		       {
		    	   var responseJSON = Alfresco.util.parseJSON(response.serverResponse.responseText);
		    	   Alfresco.util.PopupManager.displayMessage({
		    		   text: this.msg("message.sign.error", responseJSON.error),
		    		   displayTime: 10
		    	   });
		       },
		       scope: this
		    }
		 });
		 this.modules.sign.show();
		}
	});
})();