/**
 * 
 */
package org.alfresco.plugin.digitalSigning.scheduledAction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Certificate Alert Job class.
 * 
 * @author Emmanuel ROUX
 */
public class CertificateAlertJob implements Job {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap jobData = context.getJobDetail().getJobDataMap();
		// extract the content cleaner to use
		Object certificateAlertObj = jobData.get("certificateAlert");
		if (certificateAlertObj == null
				|| !(certificateAlertObj instanceof CertificateAlert)) {
			throw new AlfrescoRuntimeException(
					"CertificateAlertJob data must contain valid 'certificateAlert' reference");
		}
		CertificateAlert certificateAlert = (CertificateAlert) certificateAlertObj;
		certificateAlert.execute();
	}

}
