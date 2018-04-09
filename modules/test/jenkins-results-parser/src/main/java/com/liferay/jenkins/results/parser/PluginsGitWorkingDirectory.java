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

package com.liferay.jenkins.results.parser;

import java.io.File;
import java.io.IOException;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 * @author Peter Yoo
 */
public class PluginsGitWorkingDirectory extends GitWorkingDirectory {

	public PluginsGitWorkingDirectory(
			String upstreamBranchName, String workingDirectoryPath)
		throws IOException {

		super(
			_getPluginsUpstreamBranchName(upstreamBranchName),
			workingDirectoryPath);
	}

	public PluginsGitWorkingDirectory(
			String upstreamBranchName, String workingDirectoryPath,
			String repositoryName)
		throws IOException {

		super(
			_getPluginsUpstreamBranchName(upstreamBranchName),
			workingDirectoryPath, repositoryName);
	}

	private static String _getPluginsUpstreamBranchName(
		String upstreamBranchName) {

		if (upstreamBranchName.contains("7.0.x") ||
			upstreamBranchName.contains("7.1.x") ||
			upstreamBranchName.contains("master")) {

			return "7.0.x";
		}

		return upstreamBranchName;
	}

}