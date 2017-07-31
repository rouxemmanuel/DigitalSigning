/**
 * 
 */
package org.alfresco.plugin.digitalSigning.scheduledAction;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.alfresco.model.ContentModel;
import org.alfresco.plugin.digitalSigning.model.SigningConstants;
import org.alfresco.plugin.digitalSigning.model.SigningModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

/**
 * Certificate alert process class.
 * 
 * @author Emmanuel ROUX
 */
public class CertificateAlert {
	
	/**
	 * Logger.
	 */
	private static Log logger = LogFactory.getLog(CertificateAlert.class);
	
	/**
	 * Message ressource bundle.
	 */
	private static final String BUNDLE_FILE_NAME = "alfresco.module.digitalSigning.messages.alert-messages";

	/**
	 * RetryingTransactionHelper bean.
	 */
	private RetryingTransactionHelper retryingTransactionHelper;
	
	/**
	 * NodeService bean.
	 */
	private NodeService nodeService;
	
	/**
	 * PersonService bean.
	 */
	private PersonService personService;
	
	/**
	 * ActionService bean.
	 */
	private ActionService actionService;
	
	/**
	 * TemplateService bean.
	 */
	private TemplateService templateService;
	
	/**
	 * TenantService bean.
	 */
	private TenantService tenantService;
	
	/**
	 * PreferenceService bean.
	 */
	private PreferenceService preferenceService;
	
	/**
	 * From email address.
	 */
	private String fromEmail;
	
	/**
	 * Process execution method.
	 */
	public void execute() {

		if (logger.isDebugEnabled()) {
			logger.debug("Start certificate alert process.");
		}
		
		final RetryingTransactionCallback<Object> processCallBack = new RetryingTransactionCallback<Object>() {
			public Object execute() throws Throwable {
				checkCertificateAlert();
				return null;
			}
		};

		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
			public Object doWork() throws Exception {
				// Exécution du traitement au sein d'une transaction
				// Alfresco
				retryingTransactionHelper.doInTransaction(processCallBack, false, false);
						return null;
				}
			}, AuthenticationUtil.getSystemUserName());
		
		if (logger.isDebugEnabled()) {
			logger.debug("End certificate alert process.");
		}
	}

	/**
	 * Check certificate expiration for all users.
	 */
	private void checkCertificateAlert() {
		final PagingRequest pagingRequest = new PagingRequest(Integer.MAX_VALUE, null);
		final PagingResults<PersonInfo> personInfos = personService.getPeople(null, null, null, pagingRequest);
		if (personInfos != null) {
			final List<PersonInfo> listPersonInfos = personInfos.getPage();
			if (listPersonInfos != null && listPersonInfos.size() > 0) {
		        for (PersonInfo personInfo : listPersonInfos) {
		            final NodeRef userNodeRef = personInfo.getNodeRef();
		            final NodeRef keyNodeRef = getKeyFileForUser(userNodeRef);
		            
		            if (keyNodeRef != null) {
			            final Date lastValidityKey = (Date) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYLASTVALIDITY);
			            final String alertFrequenceKey = (String) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYALERT);
			            final Boolean alertAlreadySendKey = (Boolean) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYHASALERT);
			            
			            if (alertAlreadySendKey != null && alertAlreadySendKey == false && alertFrequenceKey != null && alertFrequenceKey.compareTo("0") != 0) {
			            	
			            	final int alertFrequenceKeyInt = Integer.valueOf(alertFrequenceKey);
			            	
			            	final Calendar keyCalendar = Calendar.getInstance();
			            	keyCalendar.setTime(lastValidityKey);
			            	keyCalendar.add(Calendar.MONTH, -alertFrequenceKeyInt);
			            	
			            	final Calendar nowCalendar = Calendar.getInstance();
			            	
			            	// If alert date is before current date
			            	if (keyCalendar.compareTo(nowCalendar) < 0) {
			            		sendMail(userNodeRef, lastValidityKey);
			            		
			            		nodeService.setProperty(keyNodeRef, SigningModel.PROP_KEYHASALERT, true);
			            	}
			            }
		            }
		        }
			}
		}
	}
	
	/**
	 * Send alert mail to a user.
	 * 
	 * @param userNodeRef user NodeRef
	 */
	private void sendMail(final NodeRef userNodeRef, final Date expirationDate) {
		final Action emailAction = actionService.createAction(MailActionExecuter.NAME);
		
		//final Locale locale = getLocaleForUser((String) nodeService.getProperty(userNodeRef, ContentModel.PROP_USERNAME));
		final Locale locale = (Locale) nodeService.getProperty(userNodeRef, ContentModel.PROP_LOCALE);
		
		final Map<String, Object> model = new HashMap<String, Object>();
	    model.put("expiration", expirationDate);
	    String text = null;
	    try {
	    	text = templateService.processTemplate("freemarker", "alfresco/module/digitalSigning/templates/keyAlert_" + locale.toString() + ".ftl", (Serializable) model, locale);
	    } catch (TemplateException e) {
	    	text = templateService.processTemplate("freemarker", "alfresco/module/digitalSigning/templates/keyAlert.ftl", (Serializable) model, locale);
	    }
	    if (text != null) {
			emailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, text);
			emailAction.setParameterValue(MailActionExecuter.PARAM_FROM , fromEmail);
			emailAction.setParameterValue(MailActionExecuter.PARAM_TO , nodeService.getProperty(userNodeRef, ContentModel.PROP_EMAIL));
			
			final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_FILE_NAME, locale);
			final String mailSubject = bundle.getString("mail.subject");
			
			emailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT , mailSubject);
	
			emailAction.setExecuteAsynchronously(true);
			actionService.executeAction(emailAction, null);
	    }
	}
	
	/**
     * Gets the specified user's preferred locale, if available.
     * 
     * @param user the username of the user whose locale is sought.
     * @return the preferred locale for that user, if available, else <tt>null</tt>. The result would be <tt>null</tt>
     *         e.g. if the user does not exist in the system.
     */
    private Locale getLocaleForUser(final String user) {
        Locale locale = null;
        String localeString = null;
        
        // get primary tenant for the specified user.
        //
        // This can have one of (at least) 3 values currently:
        // 1. In single-tenant (community/enterprise) this will be the empty string.
        // 2. In the cloud, for a username such as this: joe.soap@acme.com:
        //    2A. If the acme.com tenant exists in the system, the primary domain is "acme.com"
        //    2B. Id the acme.xom tenant does not exist in the system, the primary domain is null.
        String domain = tenantService.getPrimaryDomain(user);
        
        if (domain != null) { 
            // If the domain is not null, then the user exists in the system and we may get a preferred locale.
            localeString = TenantUtil.runAsSystemTenant(new TenantRunAsWork<String>() {
                public String doWork() throws Exception {
                    return (String) preferenceService.getPreference(user, "locale");
                }
            }, domain);
        } else {
            // If the domain is null, then the beahviour here varies depending on whether it's a single tenant or multi-tenant cloud.
            if (personExists(user)) {
                localeString = AuthenticationUtil.runAsSystem(new RunAsWork<String>() {
                    public String doWork() throws Exception  {
                        return (String) preferenceService.getPreference(user, "locale");
                    };
                }); 
            }
            // else leave it as null - there's no tenant, no user for that username, so we can't get a preferred locale.
        }
        
        if (localeString != null) {
            locale = StringUtils.parseLocaleString(localeString);
        }

        return locale;
    }
    
    public boolean personExists(final String user) {
        boolean exists = false;
        String domain = tenantService.getPrimaryDomain(user); // get primary tenant 
        if (domain != null) { 
            exists = TenantUtil.runAsTenant(new TenantRunAsWork<Boolean>()
            {
                public Boolean doWork() throws Exception
                {
                    return personService.personExists(user);
                }
            }, domain);
        } else {
            exists = personService.personExists(user);
        }
        return exists;
    }
	
	/**
	 * Get key file for a user.
	 * 
	 * @param userNodeRef user NodeRef
	 * @return the key file NodeRef
	 */
	private NodeRef getKeyFileForUser(final NodeRef userNodeRef) {
		final NodeRef userHomeFolder = (NodeRef) nodeService.getProperty(userNodeRef, ContentModel.PROP_HOMEFOLDER);
		if (userHomeFolder != null) {
			final NodeRef signingFolderNodeRef = nodeService.getChildByName(userHomeFolder, ContentModel.ASSOC_CONTAINS, SigningConstants.KEY_FOLDER);
			if (signingFolderNodeRef != null) {
				final List<ChildAssociationRef> children = nodeService.getChildAssocs(signingFolderNodeRef);
				if (children != null && children.size() > 0) {
					final Iterator<ChildAssociationRef> itChildren = children.iterator();
					while (itChildren.hasNext()) {
						final ChildAssociationRef childAssoc = itChildren.next();
						final NodeRef child = childAssoc.getChildRef();
						if (nodeService.hasAspect(child, SigningModel.ASPECT_KEY)) {
							return child;
						}
					}
					return null;
				} else {
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	

	/**
	 * @param retryingTransactionHelper the retryingTransactionHelper to set
	 */
	public final void setRetryingTransactionHelper(
			final RetryingTransactionHelper retryingTransactionHelper) {
		this.retryingTransactionHelper = retryingTransactionHelper;
	}
	
	/**
	 * @param nodeService the nodeService to set
	 */
	public final void setNodeService(final NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * @param personService the personService to set
	 */
	public final void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * @param actionService the actionService to set
	 */
	public final void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	/**
	 * @param fromEmail the fromEmail to set
	 */
	public final void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	/**
	 * @param templateService the templateService to set
	 */
	public final void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}

	/**
	 * @param tenantService the tenantService to set
	 */
	public final void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	/**
	 * @param preferenceService the preferenceService to set
	 */
	public final void setPreferenceService(PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}
}
