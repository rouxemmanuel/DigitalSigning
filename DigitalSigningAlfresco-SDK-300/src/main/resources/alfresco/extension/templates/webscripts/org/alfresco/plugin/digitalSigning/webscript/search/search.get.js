<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/evaluator.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

var items = [], item;

var filterPerson = args["filterPerson"];
var filterDate = args["filterDate"];
var siteId = args["siteId"];

var query = 'ASPECT:"dgtsgn:signed"';

// Filter on person
if (filterPerson != null) {
	if (filterPerson ==  "mine") {
		query += ' AND @dgtsgn\\:signedby:"' + person.properties.userName + '"';
	} else if (filterPerson ==  "others") {
		query += ' AND NOT @dgtsgn\\:signedby:"' + person.properties.userName + '"';
	}
}

var currentDateWithoutTime = new Date();
currentDateWithoutTime.setHours(23,59,0,0);
var isoCurrentDateWithoutTime = utils.toISO8601(currentDateWithoutTime);

// Filter on date
if (filterDate != null) {
	if (filterDate ==  "today") {
		var yesterdayWithoutTime = new Date(currentDateWithoutTime);
		yesterdayWithoutTime.setDate(currentDateWithoutTime.getDate()-1);
		var isoYesterdayWithoutTime = utils.toISO8601(yesterdayWithoutTime);
		
		query += ' AND @dgtsgn\\:signaturedate:[' + isoYesterdayWithoutTime + ' TO ' + isoCurrentDateWithoutTime + ']';
	} else if (filterDate ==  "7") {
		var sevenDayBeforeWithoutTime = new Date(currentDateWithoutTime);
		sevenDayBeforeWithoutTime.setDate(currentDateWithoutTime.getDate()-7);
		var isoSevenDayBeforeWithoutTime = utils.toISO8601(sevenDayBeforeWithoutTime);
		
		query += ' AND @dgtsgn\\:signaturedate:[' + isoSevenDayBeforeWithoutTime + ' TO ' + isoCurrentDateWithoutTime + ']';
	} if (filterDate ==  "14") {
		var forteenDayBeforeWithoutTime = new Date(currentDateWithoutTime);
		forteenDayBeforeWithoutTime.setDate(currentDateWithoutTime.getDate()-14);
		var isoForteenDayBeforeWithoutTime = utils.toISO8601(forteenDayBeforeWithoutTime);
		
		query += ' AND @dgtsgn\\:signaturedate:[' + isoForteenDayBeforeWithoutTime + ' TO ' + isoCurrentDateWithoutTime + ']';
	} if (filterDate ==  "28") {
		var twentyeightDayBeforeWithoutTime = new Date(currentDateWithoutTime);
		twentyeightDayBeforeWithoutTime.setDate(currentDateWithoutTime.getDate()-28);
		var isoTwentyeightDayBeforeWithoutTime = utils.toISO8601(twentyeightDayBeforeWithoutTime);
		
		query += ' AND @dgtsgn\\:signaturedate:[' + isoTwentyeightDayBeforeWithoutTime + ' TO ' + isoCurrentDateWithoutTime + ']';
	}
}

logger.log(query);

//Filter on site and store site information in model
if (siteId != null && siteId != "") {
	query += ' AND PATH:"/app:company_home/st:sites//cm:' + siteId + '/cm:documentLibrary//*"';
	var siteNode = siteService.getSite(siteId);
	model.siteTitle = siteNode.title;
	model.siteId = siteId;
}

var nodes = search.luceneSearch(query);

for each (node in nodes) {
   // Get evaluated properties.
   item = Evaluator.run(node);
   
   if (item !== null) {
	   locationNode = item.isLink ? item.linkedNode : item.node;
	   // Ensure we have Read permissions on the destination on the link object
	   if (!locationNode.hasPermission("Read")) break;
	   location = Common.getLocation(locationNode, null);
	   location.parent = {};
	   if (node.parent != null && node.parent.isContainer && node.parent.hasPermission("Read")) {
         location.parent.nodeRef = String(node.parent.nodeRef.toString());  
	   }
      
	   // Resolved location
	   item.location = location;
      
	   items.push(item);
   }
}

// Array Remove - By John Resig (MIT Licensed)
var fnArrayRemove = function fnArrayRemove(array, from, to) {
  var rest = array.slice((to || from) + 1 || array.length);
  array.length = from < 0 ? array.length + from : from;
  return array.push.apply(array, rest);
};

/**
 * De-duplicate orignals for any existing working copies.
 * This can't be done in evaluator.lib.js as it has no knowledge of the current filter or UI operation.
 * Note: This may result in pages containing less than the configured amount of items (50 by default).
*/
for each (item in items) {
   if (item.customObj && item.customObj.isWorkingCopy) {
      var workingCopyOriginal = String(item.customObj.workingCopyOriginal);
      for (var i = 0, ii = items.length; i < ii; i++) {
         if (String(items[i].node.nodeRef) == workingCopyOriginal) {
            fnArrayRemove(items, i);
            break;
         }
      }
   }
}

model.count = nodes.length;
model.nodes = items;