(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;
	  
      var signatureConfigDialog = null;
	  
	  var signatureElmtId = null;
	  
	  var signatureInstance = null;
	  
	  var waitDialog = null;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;
   
   Alfresco.dashlet.Signature = function Signature_constructor(htmlId)
   {
      Alfresco.dashlet.Signature.superclass.constructor.call(this, "Alfresco.dashlet.Signature", htmlId);

      // Initialise prototype properties
      signatureConfigDialog = null;
	  signatureElmtId = htmlId;
	  waitDialog = null;
	  
      return this;
   };
   
   submitCallBackAlias = function signature_submitCallBackAlias(errorNumber, errorMessage, aliasList)
   {
	    // Hide waiting dialog
	    if (waitDialog)
	    {
	    	waitDialog.destroy();
	    	waitDialog = null;
	    }
	   
		if (errorNumber != null) {
			Dom.get("yui-errors").style.display = "";
			Dom.get("yui-errorImageKey").style.display = "block";
			Dom.get("yui-errorMessageText").style.display = "block";
			Dom.get("yui-expireTr").style.display = "none";
			Dom.get("yui-keyInfosTr").style.display = "none";
			Dom.get("yui-imageInfosTr").style.display = "none";
			if (errorNumber == "1") {
			   Dom.get("yui-errorMessageTextH3").innerHTML = signatureInstance.msg("signature.noKey");
			} else if (errorNumber == "2") {
				Dom.get("yui-errorMessageTextH3").innerHTML = signatureInstance.msg("signature.error");
				if (errorMessage != null) {
					Dom.get("yui-technicalErrorText").innerHTML = errorMessage;
				}
			}
			signatureConfigDialog.hide();
		} else {
			signatureConfigDialog.hide();
			
			// Display Dialog to choose the certifcate alias
			var actionUrl = Alfresco.constants.PROXY_URI + "api/digitalSigning/chooseAlias.html";

            signatureConfigDialog = new Alfresco.module.SimpleDialog(this.id + "-configDialog").setOptions(
            {
               width: "50em",
               templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/digitalSigning/signature/configAlias?aliasList=" + aliasList,
			   actionUrl: actionUrl,
			   clearForm: true,
               onSuccess:
               {
                  fn: function Signature_onConfigSignature_callback(response)
                  {
                	  alert(response.serverResponse.responseText);
                  },
                  scope: this
               },
               doBeforeAjaxRequest:
			   {
				  fn: function Signature_doBeforeAjaxRequest(p_config, p_obj)
				  {
					// Check 
					return true;
				  },
                  scope: this
				},
               doSetupFormsValidation:
               {
                  fn: function Signature_doSetupForm_callback(form)
                  {
                	  form.addValidation(signatureConfigDialog.id + "-signaturePassword", Alfresco.forms.validation.mandatory, null, "keyup");
                	  document.getElementById(signatureConfigDialog.id + "-cancel").onclick = function(){
                		  Alfresco.util.Ajax.jsonRequest({
                              method: "POST",
                              url: Alfresco.constants.PROXY_URI + "api/digitalSigning/delete",
                              dataObj: "",
                              successCallback: {
                                 fn: function() {
                                	Dom.get("yui-errors").style.display = "";
                         			Dom.get("yui-errorImageKey").style.display = "block";
                         			Dom.get("yui-errorMessageText").style.display = "block";
                         			Dom.get("yui-expireTr").style.display = "none";
                         			Dom.get("yui-keyInfosTr").style.display = "none";
                         			Dom.get("yui-imageInfosTr").style.display = "none";
                         			Dom.get("yui-errorMessageTextH3").innerHTML = signatureInstance.msg("signature.noKey");
                                 },
                                 scope: this
                              },
                              failureCallback:
                              {
                            	  fn: function() {
                            		Dom.get("yui-errors").style.display = "";
                           			Dom.get("yui-errorImageKey").style.display = "block";
                           			Dom.get("yui-errorMessageText").style.display = "block";
                           			Dom.get("yui-expireTr").style.display = "none";
                           			Dom.get("yui-keyInfosTr").style.display = "none";
                           			Dom.get("yui-imageInfosTr").style.display = "none";
                           			Dom.get("yui-errorMessageTextH3").innerHTML = signatureInstance.msg("signature.noKey");
                                   },
                                   scope: this
                              }
                           });
                		  
                	  };
                  },
                  scope: this
               },
               doBeforeFormSubmit :
   	           {
   	            fn: function(form, obj)
   	            {
   	            	waitDialog = Alfresco.util.PopupManager.displayMessage({
   		                text : signatureInstance.msg("upload.signature.processing.alias"),
   		                spanClass : "wait",
   		                displayTime : 0
   		             });
   	            },
   	            scope: this
   	         }
            });
	        signatureConfigDialog.show();
		}
   }
   
   submitCallBack = function signature_submitCallBack(errorNumber, errorMessage, alias, subject, type, algorithm, firstValidity, lastValidity, hasExpired, expire, hasImage, alert)
   {
	    // Hide waiting dialog
	    if (waitDialog)
	    {
	    	waitDialog.destroy();
	    	waitDialog = null;
	    }
	   
		if (errorNumber != null) {
			Dom.get("yui-errors").style.display = "";
			Dom.get("yui-errorImageKey").style.display = "block";
			Dom.get("yui-errorMessageText").style.display = "block";
			Dom.get("yui-expireTr").style.display = "none";
			Dom.get("yui-keyInfosTr").style.display = "none";
			Dom.get("yui-imageInfosTr").style.display = "none";
			if (errorNumber == "1") {
			   Dom.get("yui-errorMessageTextH3").innerHTML = signatureInstance.msg("signature.noKey");
			} else if (errorNumber == "2") {
				Dom.get("yui-errorMessageTextH3").innerHTML = signatureInstance.msg("signature.error");
				if (errorMessage != null) {
					Dom.get("yui-technicalErrorText").innerHTML = errorMessage;
				}
			}
		} else {
			Dom.get("yui-errors").style.display = "none";
			Dom.get("yui-errorImageKey").style.display = "none";
			Dom.get("yui-errorMessageText").style.display = "none";
			
			if (alias != null && subject != null && type != null && algorithm != null && firstValidity != null && lastValidity != null && alert != null) {
				Dom.get("yui-keyInfosTr").style.display = "";
				Dom.get("yui-keyInfosTypeLabel").style.display = "";
				Dom.get("yui-keyInfosType").innerHTML = type + "<br />";
				Dom.get("yui-keyInfosAliasLabel").style.display = "";
				Dom.get("yui-keyInfosAlias").innerHTML = alias + "<br />";
				Dom.get("yui-keyInfosSubjectLabel").style.display = "";
				Dom.get("yui-keyInfosSubject").innerHTML = subject + "<br />";
				Dom.get("yui-keyInfosAlgorithmLabel").style.display = "";
				Dom.get("yui-keyInfosAlgorithm").innerHTML = algorithm + "<br />";
				Dom.get("yui-keyInfosFirstDayLabel").style.display = "";
				Dom.get("yui-keyInfosFirstDay").innerHTML = firstValidity + "<br />";
				Dom.get("yui-keyInfosLastDayLabel").style.display = "";
				Dom.get("yui-keyInfosLastDay").innerHTML = lastValidity + "<br />";
				Dom.get("yui-keyInfosAlertLabel").style.display = "";
				Dom.get("yui-keyInfosAlert").innerHTML = alert;
				
				Dom.get("yui-keyInfosImageDiv").innerHTML = "<img src='" + Alfresco.constants.URL_CONTEXT + "res/components/images/help-key-bw-32.png' id='yui-keyInfosImage'>";
			}
			if (hasExpired || expire != null) {
				Dom.get("yui-expireTr").style.display = "";
				if (hasExpired) {
					Dom.get("yui-expireMessageText").innerHTML = signatureInstance.msg("signature.warning.end");
				} else if (expire != null) {
					Dom.get("yui-expireMessageText").innerHTML = signatureInstance.msg("signature.warning.day", expire);
				}
			} else {
				Dom.get("yui-expireTr").style.display = "none";
			}
			if (hasImage != null && hasImage == true) {
				Dom.get("yui-imageInfosTr").style.display = "";
				//Dom.get("yui-imageInfosImage").innerHTML = "";
				var imgDiv = document.getElementById("yui-imageInfosImage");
				if (imgDiv.hasChildNodes()) {
					imgDiv.removeChild(imgDiv.childNodes[0]);
				}
				var oImg=document.createElement("img");
				var altImageMsg = signatureInstance.msg("signature.image.alt");
				var srcImage = Alfresco.constants.PROXY_URI + "api/digitalSigning/image?cache=" + new Date().getTime();
				oImg.setAttribute('src', srcImage);
				oImg.setAttribute('alt', altImageMsg);
				oImg.setAttribute('width', '200px');
				oImg.setAttribute('border', '0');
				Dom.get("yui-imageInfosImage").appendChild(oImg);
				Dom.get("yui-imageInfosImageDiv").innerHTML = "<img src='" + Alfresco.constants.URL_CONTEXT + "res/components/images/help-signing-bw-32.png' id='yui-imageInfosIcon'>";
			} else {
				Dom.get("yui-imageInfosTr").style.display = "none";
				Dom.get("yui-imageInfosImage").innerHTML = "";
				Dom.get("yui-imageInfosImageDiv").innerHTML = "";
			}
		}
		signatureConfigDialog.hide();
   }

   YAHOO.extend(Alfresco.dashlet.Signature, Alfresco.component.Base,
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
         /**
          * ComponentId used for saving configuration
          * @property componentId
          * @type string
          */
         componentId: ""
      },

      /**
       * Fired by YUI when parent element is available for scripting.
       * Initialises components, including YUI widgets.
       *
       * @method onReady
       */
      onReady: function Signature_onReady()
      {
         /**
          * Save reference to iframe wrapper so we can hide and show it depending
          * on how well the browser handles flash movies.
          */
         this.widgets.content = Dom.get(this.id + "-signatures");
		 
		 signatureInstance = this;
      },

      /**
       * Event listener for configuration link click.
       *
       * @method onConfigSignatureClick
       * @param e {object} HTML event
       */
      onConfigSignatureClick: function Signature_onConfigSignatureClick(e)
      {
         Event.stopEvent(e);
         signatureConfigDialog = null;
         
         var actionUrl = Alfresco.constants.PROXY_URI + "api/digitalSigning/upload.html";

         if (!signatureConfigDialog)
         {
            signatureConfigDialog = new Alfresco.module.SimpleDialog(this.id + "-configDialog").setOptions(
            {
               width: "50em",
               templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/digitalSigning/signature/config",
			   actionUrl: actionUrl,
			   clearForm: true,
               onSuccess:
               {
                  fn: function Signature_onConfigSignature_callback(response)
                  {
                	  alert(response.serverResponse.responseText);
                	  
                	  //var div = Dom.get(this.id + "-iframeWrapper");
	                  //div.innerHTML = response.serverResponse.responseText + '<div class="resize-mask"></div>';
	                    
                  },
                  scope: this
               },
			   doBeforeAjaxRequest:
			   {
				  fn: function Signature_doBeforeAjaxRequest(p_config, p_obj)
				  {
					

					// Check 

					return true;
				  },
                  scope: this
				},
               doSetupFormsValidation:
               {
                  fn: function Signature_doSetupForm_callback(form)
                  {
                	  form.addValidation(signatureConfigDialog.id + "-signatureKey", Alfresco.forms.validation.mandatory, null, "keyup");
                	  form.addValidation(signatureConfigDialog.id + "-signaturePassword", Alfresco.forms.validation.mandatory, null, "keyup");
                	  //form.addValidation(signatureConfigDialog.id + "-signatureAlias", Alfresco.forms.validation.mandatory, null, "keyup");
                  },
                  scope: this
               },
               doBeforeFormSubmit :
   	           {
   	            fn: function(form, obj)
   	            {
   	            	waitDialog = Alfresco.util.PopupManager.displayMessage({
   		                text : signatureInstance.msg("upload.signature.processing"),
   		                spanClass : "wait",
   		                displayTime : 0
   		             });
   	            },
   	            scope: this
   	         }
            });
         }
         signatureConfigDialog.show();
      },
	  
	  displaySignatureMsg: function Signature_displaySignatureMsg(key)
      {
         return this.msg(key);
      },
   });
})();