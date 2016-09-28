/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.configuration.admin.web.internal.portlet.action;

import com.liferay.configuration.admin.web.internal.constants.ConfigurationAdminPortletKeys;
import com.liferay.configuration.admin.web.internal.model.ConfigurationModel;
import com.liferay.configuration.admin.web.internal.util.AttributeDefinitionUtil;
import com.liferay.configuration.admin.web.internal.util.ConfigurationModelRetriever;
import com.liferay.portal.configuration.metatype.definitions.ExtendedAttributeDefinition;
import com.liferay.portal.configuration.metatype.definitions.ExtendedObjectClassDefinition;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.zip.ZipWriter;
import com.liferay.portal.kernel.zip.ZipWriterFactoryUtil;

import java.io.FileInputStream;
import java.io.OutputStream;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.portlet.MimeResponse;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.felix.cm.file.ConfigurationHandler;

import org.osgi.service.cm.Configuration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.AttributeDefinition;

/**
 * @author Brian Wing Shun Chan
 * @author Vilmos Papp
 * @author Eduardo Garcia
 * @author Raymond Augé
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" +
			ConfigurationAdminPortletKeys.SYSTEM_SETTINGS,
		"mvc.command.name=export"
	},
	service = MVCResourceCommand.class
)
public class ExportConfigurationMVCResourceCommand
	implements MVCResourceCommand {

	@Override
	public boolean serveResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws PortletException {

		if (!(resourceResponse instanceof MimeResponse)) {
			return false;
		}

		String pid = ParamUtil.getString(resourceRequest, "pid");
		String factoryPid = ParamUtil.getString(resourceRequest, "factoryPid");

		try {
			if (Validator.isNotNull(pid)) {
				exportPid(resourceRequest, resourceResponse);
			}
			else if (Validator.isNotNull(factoryPid)) {
				exportFactoryPid(resourceRequest, resourceResponse);
			}
			else {
				exportAll(resourceRequest, resourceResponse);
			}
		}
		catch (Exception e) {
			throw new PortletException(e);
		}

		return true;
	}

	protected void exportAll(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String languageId = themeDisplay.getLanguageId();

		ZipWriter zipWriter = ZipWriterFactoryUtil.getZipWriter();

		Map<String, ConfigurationModel> configurationModels =
			_configurationModelRetriever.getConfigurationModels(
				themeDisplay.getLanguageId());

		for (ConfigurationModel configurationModel :
				configurationModels.values()) {

			if (configurationModel.isFactory()) {
				String curFactoryPid = configurationModel.getFactoryPid();

				List<ConfigurationModel> factoryInstances =
					_configurationModelRetriever.getFactoryInstances(
						configurationModel);

				for (ConfigurationModel factoryInstance : factoryInstances) {
					String curPid = factoryInstance.getID();

					String curFileName = getFileName(curFactoryPid, curPid);

					zipWriter.addEntry(
						curFileName,
						getPropertiesAsBytes(
							languageId, curFactoryPid, curPid));
				}
			}
			else if (configurationModel.hasConfiguration()) {
				String curPid = configurationModel.getID();

				String curFileName = getFileName(null, curPid);

				zipWriter.addEntry(
					curFileName,
					getPropertiesAsBytes(languageId, curPid, curPid));
			}
		}

		String fileName = "liferay-system-settings.zip";

		PortletResponseUtil.sendFile(
			resourceRequest, resourceResponse, fileName,
			new FileInputStream(zipWriter.getFile()),
			ContentTypes.TEXT_XML_UTF8);
	}

	protected void exportFactoryPid(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String languageId = themeDisplay.getLanguageId();

		ZipWriter zipWriter = ZipWriterFactoryUtil.getZipWriter();

		String factoryPid = ParamUtil.getString(resourceRequest, "factoryPid");

		Map<String, ConfigurationModel> configurationModels =
			_configurationModelRetriever.getConfigurationModels(
				themeDisplay.getLanguageId());

		ConfigurationModel factoryConfigurationModel = configurationModels.get(
			factoryPid);

		List<ConfigurationModel> factoryInstances =
			_configurationModelRetriever.getFactoryInstances(
				factoryConfigurationModel);

		for (ConfigurationModel factoryInstance : factoryInstances) {
			String curPid = factoryInstance.getID();

			String curFileName = getFileName(null, curPid);

			zipWriter.addEntry(
				curFileName,
				getPropertiesAsBytes(languageId, factoryPid, curPid));
		}

		String fileName =
			"liferay-system-settings-" +
				factoryConfigurationModel.getFactoryPid() + ".zip";

		PortletResponseUtil.sendFile(
			resourceRequest, resourceResponse, fileName,
			new FileInputStream(zipWriter.getFile()),
			ContentTypes.TEXT_XML_UTF8);
	}

	protected void exportPid(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		String factoryPid = ParamUtil.getString(resourceRequest, "factoryPid");
		String pid = ParamUtil.getString(resourceRequest, "pid");

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String languageId = themeDisplay.getLanguageId();

		String fileName = getFileName(factoryPid, pid);

		PortletResponseUtil.sendFile(
			resourceRequest, resourceResponse, fileName,
			getPropertiesAsBytes(languageId, factoryPid, pid),
			ContentTypes.TEXT_XML_UTF8);
	}

	protected String getFileName(String factoryPid, String pid) {
		String fileName = pid;

		if (Validator.isNotNull(factoryPid) && !factoryPid.equals(pid)) {
			String factoryInstanceId = pid.substring(factoryPid.length() + 1);

			fileName = factoryPid + StringPool.DASH + factoryInstanceId;
		}

		return fileName + ".config";
	}

	protected byte[] getPropertiesAsBytes(
			String languageId, String factoryPid, String pid)
		throws Exception {

		Dictionary propertiesDictionary = getPropertiesDictionary(
			languageId, factoryPid, pid);

		OutputStream outputStream = new UnsyncByteArrayOutputStream();

		ConfigurationHandler.write(outputStream, propertiesDictionary);

		String propertiesString = outputStream.toString();

		return propertiesString.getBytes();
	}

	protected Dictionary<String, Object> getPropertiesDictionary(
			String languageId, String factoryPid, String pid)
		throws Exception {

		Dictionary propertiesDictionary = new Hashtable<>();

		Map<String, ConfigurationModel> configurationModels =
			_configurationModelRetriever.getConfigurationModels(languageId);

		ConfigurationModel configurationModel = configurationModels.get(pid);

		if ((configurationModel == null) && Validator.isNotNull(factoryPid)) {
			configurationModel = configurationModels.get(factoryPid);
		}

		if (configurationModel == null) {
			return propertiesDictionary;
		}

		Configuration configuration =
			_configurationModelRetriever.getConfiguration(pid);

		if (configuration == null) {
			return propertiesDictionary;
		}

		ExtendedObjectClassDefinition extendedObjectClassDefinition =
			configurationModel.getExtendedObjectClassDefinition();

		ExtendedAttributeDefinition[] attributeDefinitions =
			extendedObjectClassDefinition.getAttributeDefinitions(
				ConfigurationModel.ALL);

		for (AttributeDefinition attributeDefinition : attributeDefinitions) {
			String[] values = AttributeDefinitionUtil.getProperty(
				attributeDefinition, configuration);

			if (values.length == 1) {
				propertiesDictionary.put(
					attributeDefinition.getID(), values[0]);
			}
			else if (values.length > 1) {
				propertiesDictionary.put(attributeDefinition.getID(), values);
			}
		}

		return propertiesDictionary;
	}

	@Reference
	private ConfigurationModelRetriever _configurationModelRetriever;

}