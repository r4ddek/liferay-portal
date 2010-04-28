/**
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portlet.calendar.action;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.CalendarUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.struts.PortletAction;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.calendar.ImportEventsException;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.calendar.service.CalEventServiceUtil;

import java.io.File;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * <a href="ImportEventsAction.java.html"><b><i>View Source</i></b></a>
 *
 * @author Bruno Farache
 * @author Juan Fernández
 */
public class ImportEventsAction extends PortletAction {

	public void processAction(
			ActionMapping mapping, ActionForm form, PortletConfig portletConfig,
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		try {
			UploadPortletRequest uploadRequest =
				PortalUtil.getUploadPortletRequest(actionRequest);

			ServiceContext serviceContext = ServiceContextFactory.getInstance(
				CalEvent.class.getName(), actionRequest);

			File file = uploadRequest.getFile("file");

			validate(file);

			CalEventServiceUtil.importICal4j(
				serviceContext.getScopeGroupId(), file);

			sendRedirect(actionRequest, actionResponse);
		}
		catch (Exception e) {
			if (!(e instanceof ImportEventsException)) {
				_log.error(e, e);
			}
			SessionErrors.add(actionRequest, e.getClass().getName());
			setForward(actionRequest, "portlet.calendar.error");
		}
	}

	private void validate(File file) throws ImportEventsException {
		String fileNameExtension = FileUtil.getExtension(file.getName());

		if (!fileNameExtension.equals(CalendarUtil.ICAL_EXTENSION)) {
			throw new ImportEventsException();
		}
	}

	private static Log _log = LogFactoryUtil.getLog(ExportEventsAction.class);

}