<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function runEvaluator(evaluator)
{
   return eval(evaluator);
}


function getFilters(filterType)
{
   var myConfig = new XML(config.script),
       filters = [];

   for each (var xmlFilter in myConfig[filterType].filter)
   {
      filters.push(
      {
         type: xmlFilter.@type.toString(),
         label: xmlFilter.@label.toString()
      });
   }

   return filters;
}

/* Max Items */
function getMaxItems()
{
   var myConfig = new XML(config.script),
      maxItems = myConfig["max-items"];

   if (maxItems)
   {
      maxItems = myConfig["max-items"].toString();
   }
   return parseInt(maxItems && maxItems.length > 0 ? maxItems : 50, 10);
}


model.filterRanges = getFilters("filter-range");
model.filterTypes = getFilters("filter-type");
model.maxItems = getMaxItems();
var siteId = (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "";
var regionId = args['region-id'];
model.preferences = AlfrescoUtil.getPreferences("org.alfresco.share.signeddocuments.dashlet." + regionId);

function main()
{
   // Widget instantiation metadata...
   model.prefFilterPerson = model.preferences.filterPerson;
   if (model.prefFilterPerson == null)
   {
      model.prefFilterPerson = "all";
   }
   
   model.prefFilterDate = model.preferences.filterDate;
   if (model.prefFilterDate == null)
   {
	   model.prefFilterDate = "28";
   }

   model.prefSimpleView = model.preferences.simpleView;
   if (model.prefSimpleView == null)
   {
      model.prefSimpleView = true;
   }

   var signedDocs = {
      id : "SignedDocuments",
      name : "Alfresco.dashlet.SignedDocuments",
      options : {
         filterPerson : model.prefFilterPerson,
         filterDate : model.prefFilterDate,
         maxItems : parseInt(model.maxItems),
         simpleView : model.prefSimpleView,
         validFiltersPerson : model.filterTypes,
         validFiltersDate : model.filterRanges,
         regionId : regionId,
         siteId : siteId
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
   model.widgets = [signedDocs, dashletResizer, dashletTitleBarActions];
}

main();