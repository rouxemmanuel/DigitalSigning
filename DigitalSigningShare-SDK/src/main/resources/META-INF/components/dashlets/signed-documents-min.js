/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Dashboard SignedDocuments component.
 *
 * @namespace Alfresco
 * @class Alfresco.dashlet.SignedDocuments
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Selector = YAHOO.util.Selector;

   /**
    * Dashboard SignedDocuments constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.dashlet.MyDocuments} The new component instance
    * @constructor
    */
   Alfresco.dashlet.SignedDocuments = function SignedDocuments_constructor(htmlId)
   {
      return Alfresco.dashlet.SignedDocuments.superclass.constructor.call(this, htmlId);
   };

   YAHOO.extend(Alfresco.dashlet.SignedDocuments, Alfresco.component.SimpleDocList,
   {
      PREFERENCES_SIGNEDDOCUMENTS_DASHLET_FILTER_PERSON: "",
      PREFERENCES_SIGNEDDOCUMENTS_DASHLET_FILTER_DATE: "",
      PREFERENCES_SIGNEDDOCUMENTS_DASHLET_VIEW: "",
      /**
       * Fired by YUI when parent element is available for scripting
       * @method onReady
       */
      onReady: function SignedDocuments_onReady()
      {
         /**
          * Preferences
          */
         var PREFERENCES_SIGNEDDOCUMENTS_DASHLET = this.services.preferences.getDashletId(this, "signeddocuments");
            this.PREFERENCES_SIGNEDDOCUMENTS_DASHLET_FILTER_PERSON = PREFERENCES_SIGNEDDOCUMENTS_DASHLET + ".filterPerson";
            this.PREFERENCES_SIGNEDDOCUMENTS_DASHLET_FILTER_DATE = PREFERENCES_SIGNEDDOCUMENTS_DASHLET + ".filterDate";
            this.PREFERENCES_SIGNEDDOCUMENTS_DASHLET_VIEW = PREFERENCES_SIGNEDDOCUMENTS_DASHLET + ".simpleView";

         // Create Dropdown filter
         this.widgets.user = Alfresco.util.createYUIButton(this, "user", this.onFilterUserChange,
         {
            type: "menu",
            menu: "user-menu",
            lazyloadmenu: false
         });
         this.widgets.range = Alfresco.util.createYUIButton(this, "range", this.onFilterRangeChange,
         {
            type: "menu",
            menu: "range-menu",
            lazyloadmenu: false
         });
         
         // Select the preferred filter in the ui
         //var filterPerson = this.options.filterPerson;
         //var filterDate = this.options.filterDate;
         //filterPerson = Alfresco.util.arrayContains(this.options.validFiltersPerson, filterPerson) ? filterPerson : this.options.validFiltersPerson[0].type;
         //filterDate = Alfresco.util.arrayContains(this.options.validFiltersDate, filterDate) ? filterDate : this.options.validFiltersDate[0].type;
         
         this.widgets.range.set("label", this.msg("filter.28"));
         this.widgets.range.value = "28";
         this.widgets.user.set("label", this.msg("filter.all"));
         this.widgets.user.value = "all";
         this.widgets.siteId = this.options.siteId;
         
         // Detailed/Simple List button
         this.widgets.simpleDetailed = new YAHOO.widget.ButtonGroup(this.id + "-simpleDetailed");
         if (this.widgets.simpleDetailed !== null)
         {
            this.widgets.simpleDetailed.check(this.options.simpleView ? 0 : 1);
            this.widgets.simpleDetailed.on("checkedButtonChange", this.onSimpleDetailed, this.widgets.simpleDetailed, this);
         }

         // Display the toolbar now that we have selected the filter
         Dom.removeClass(Selector.query(".toolbar div", this.id, true), "hidden");

         // DataTable can now be rendered
         Alfresco.dashlet.SignedDocuments.superclass.onReady.apply(this, arguments);
      },
      
      /**
       * Generate base webscript url.
       * Can be overridden.
       *
       * @method getWebscriptUrl
       */
      getWebscriptUrl: function SimpleDocList_getWebscriptUrl()
      {
         return Alfresco.constants.PROXY_URI + "api/digitalSigning/searchDocuments?max=50";
      },
      
      
      /**
       * Detail custom datacell formatter
       *
       * @method renderCellDetail
       * @param elCell {object}
       * @param oRecord {object}
       * @param oColumn {object}
       * @param oData {object|string}
       */
      renderCellDetail: function SimpleDocList_renderCellDetail(elCell, oRecord, oColumn, oData)
      {
         var record = oRecord.getData(),
            desc = "";

         if (record.isInfo)
         {
            desc += '<div class="empty"><h3>' + record.title + '</h3>';
            desc += '<span>' + record.description + '</span></div>';
         }
         else
         {
            var id = this.id + '-metadata-' + oRecord.getId(),
               version = "",
               description = '<span class="faded">' + this.msg("details.description.none") + '</span>',
               dateLine = "",
               locn = record.location,
               nodeRef = new Alfresco.util.NodeRef(record.nodeRef),
               docDetailsUrl = Alfresco.constants.URL_PAGECONTEXT + (locn.site ? "site/" + locn.site + '/' : "") + "document-details?nodeRef=" + nodeRef.toString();

            // Description non-blank?
            if (record.description && record.description !== "")
            {
               description = Alfresco.util.activateLinks(Alfresco.util.encodeHTML(record.description));
            }

            // Version display
            if (record.version && record.version !== "")
            {
               version = '<span class="document-version">' + Alfresco.util.encodeHTML(record.version) + '</span>';
            }
            
            // Date line
            var dateI18N = "modified", dateProperty = record.modifiedOn;
            if (record.custom && record.custom.isWorkingCopy)
            {
               dateI18N = "editing-started";
            }
            else if (record.modifiedOn === record.createdOn)
            {
               dateI18N = "created";
               dateProperty = record.createdOn;
            }
            
            if (locn.site)
            {
               dateLine = this.msg("details." + dateI18N + "-in-site", Alfresco.util.relativeTime(dateProperty), Alfresco.util.siteDashboardLink(locn.site, locn.siteTitle, 'class="site-link theme-color-1" id="' + id + '"'));
            }
            else
            {
               dateLine = this.msg("details." + dateI18N + "-by", Alfresco.util.relativeTime(dateProperty), Alfresco.util.userProfileLink(record.modifiedByUser, record.modifiedBy, 'class="theme-color-1"'));
            }
            
            dateLine += "<br />" + this.msg("details.signed-by", Alfresco.util.relativeTime(record.signatureDate), Alfresco.util.userProfileLink(record.signedBy, record.signedBy, 'class="theme-color-1"'));

            if (this.options.simpleView)
            {
               /**
                * Simple View
                */
               desc += '<h3 class="filename simple-view"><a class="theme-color-1" href="' + docDetailsUrl + '">' + Alfresco.util.encodeHTML(record.displayName) + '</a></h3>';
               desc += '<div class="detail"><span class="item-simple">' + dateLine + '</span></div>';
            }
            else
            {
               /**
                * Detailed View
                */
               desc += '<h3 class="filename"><a class="theme-color-1" href="' + docDetailsUrl + '">' + Alfresco.util.encodeHTML(record.displayName) + '</a>' + version + '</h3>';

               desc += '<div class="detail">';
               desc +=    '<span class="item">' + dateLine + '</span>';
               if (this.options.showFileSize)
               {
                  desc +=    '<span class="item">' + Alfresco.util.formatFileSize(record.size) + '</span>';
               }
               desc += '</div>';
               desc += '<div class="detail"><span class="item">' + description + '</span></div>';
            }
            
            // Metadata tooltip
            this.metadataTooltips.push(id);
         }

         elCell.innerHTML = desc;
      },

      /**
       * Calculate webscript parameters
       *
       * @method getParameters
       * @override
       */
      getParameters: function SignedDocuments_getParameters()
      {
         return "filterPerson=" + this.widgets.user.value + "&filterDate=" + this.widgets.range.value + "&siteId=" + this.widgets.siteId;
      },

      /**
       * Filter User Change menu handler
       *
       * @method onFilterUserChange
       * @param p_sType {string} The event
       * @param p_aArgs {array}
       */
      onFilterUserChange: function SignedDocuments_onFilterUserChange(p_sType, p_aArgs)
      {
         var menuItem = p_aArgs[1];
         if (menuItem)
         {
            this.widgets.user.set("label", menuItem.cfg.getProperty("text"));
            this.widgets.user.value = menuItem.value;

            this.services.preferences.set(this.PREFERENCES_SIGNEDDOCUMENTS_DASHLET_FILTER_PERSON, this.widgets.user.value);

            this.reloadDataTable();
         }
      },
      
      /**
       * Filter Range Change menu handler
       *
       * @method onFilterRangeChange
       * @param p_sType {string} The event
       * @param p_aArgs {array}
       */
      onFilterRangeChange: function SignedDocuments_onFilterRangeChange(p_sType, p_aArgs)
      {
         var menuItem = p_aArgs[1];
         if (menuItem)
         {
            this.widgets.range.set("label", menuItem.cfg.getProperty("text"));
            this.widgets.range.value = menuItem.value;

            this.services.preferences.set(this.PREFERENCES_SIGNEDDOCUMENTS_DASHLET_FILTER_DATE, this.widgets.range.value);

            this.reloadDataTable();
         }
      },

      /**
       * Show/Hide detailed list buttongroup click handler
       *
       * @method onSimpleDetailed
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onSimpleDetailed: function SignedDocuments_onSimpleDetailed(e, p_obj)
      {
         this.options.simpleView = e.newValue.index === 0;
         this.services.preferences.set(this.PREFERENCES_SIGNEDDOCUMENTS_DASHLET_VIEW, this.options.simpleView);
         if (e)
         {
            Event.preventDefault(e);
         }

         this.reloadDataTable();
      }
   });
})();
