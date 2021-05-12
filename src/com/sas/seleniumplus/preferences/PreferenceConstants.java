/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2018-06-15    (LeiWang) Added SAFS_DATA_SERVICE_TEST_COUNT_UNIT, SUFFIX_OPTIONS
 */
package com.sas.seleniumplus.preferences;

import java.io.File;

/**
 * Constant definitions for plug-in preferences
 * The default values are put into preferences.properties resource bundle file.
 */
public class PreferenceConstants {

	/** 'preferences' the resource bundle name for preference */
	public static final String RESOURCE_BUNDLE_PREFERENCES 			= "preferences";
	/** '<b>eclipse\configuration\com.sas.seleniumplus\preferences.properties</b>' under %SELENIUM_PLUS%.<br/>
	 * It is used to hold the custom resource bundles  */
	public static final String RESOURCE_BUNDLE_CUSTOM_FOLDER		= File.separator+"eclipse"+File.separator+"configuration"+File.separator+"com.sas.seleniumplus";

	/** '<b>extra\automation\</b>' under %SELENIUM_PLUS%.<br/>
	 * It is used to hold the default configuration file safstid.ini  */
	public static final String EXTRA_AUTOMATION_FOLDER				= File.separator+"extra"+File.separator+"automation";


	public static final String PAGE_DESCRIPTION_REMOTE_SERVER 			= "remote.server.preference.page.desc";
	public static final String PAGE_DESCRIPTION_UPDATE_SITE 			= "update.site.preference.page.desc";
	public static final String PAGE_DESCRIPTION_SAFS_DATA_SERVICE		= "safs.data.service.preference.page.desc";
	public static final String PAGE_DESCRIPTION_SAFS_DOCUMENTS			= "documents.preference.page.desc";

	//The name of StringFieldEditors and they also served as key prefix in perferences.properties
	public static final String UPDATESITE_LIB_URL 			= "UpdatesiteLibUrl";
	public static final String UPDATESITE_PLUGIN_URL 		= "UpdatesitePluginUrl";
	public static final String UPDATESITE_SOURCECODE_URL 	= "UpdatesiteSourceCodeUrl";
	public static final String UPDATESITE_JAVADOC_URL 		= "UpdatesiteJavadocUrl";

	public static final String SAFS_DATA_SERVICE_URL 		= "SAFSDataServiceURL";

	public static final String SAFS_DOC_BASE_URL 			= "SAFSDocBaseURL";
	public static final String SAFS_DOC_SELENIUMPLUS		= "SAFSDocSeleniumPlus";
	public static final String SAFS_DOC_WHATSNEW 			= "SAFSDocWhatsNew";
	public static final String SAFS_DOC_KEYWORDS 			= "SAFSDocKeywords";
	public static final String SAFS_DOC_XPATH_GROUP			= "SAFSDocXpathGroup";
	public static final String SAFS_DOC_XPATH_TABLE			= "SAFSDocXpathTable";

	//Please update the array 'stringEditorNames' if any new string key is added
	public static final String[] stringEditorNames = {
		UPDATESITE_LIB_URL,
		UPDATESITE_PLUGIN_URL,
		UPDATESITE_SOURCECODE_URL,
		UPDATESITE_JAVADOC_URL,
		SAFS_DATA_SERVICE_URL,
		SAFS_DOC_BASE_URL,
		SAFS_DOC_SELENIUMPLUS,
		SAFS_DOC_WHATSNEW,
		SAFS_DOC_KEYWORDS,
		SAFS_DOC_XPATH_GROUP,
		SAFS_DOC_XPATH_TABLE
	};

	//The name of ComboFieldEditors and they also served as key prefix in perferences.properties
	public static final String SAFS_DATA_SERVICE_TEST_COUNT_UNIT	= "SAFSDataServiceTestCountUnit";
	//Please update the array 'comboEditorNames' if any new combobox key is added
	public static final String[] comboEditorNames = {
		SAFS_DATA_SERVICE_TEST_COUNT_UNIT
	};

	//The name of BooleanFieldEditors and they also served as key prefix in perferences.properties
	public static final String BOOLEAN_VALUE_LIB 			= "chkLibValue";
	public static final String BOOLEAN_VALUE_PLUGIN 		= "chkPluginValue";
	public static final String BOOLEAN_VALUE_SOURCE_CODE 	= "chkSourceCodeValue";
	public static final String BOOLEAN_VALUE_JAVADOC 		= "chkJavadocValue";
	public static final String BOOLEAN_VALUE_UPDATE_JRE		= "chkUpdateJRE";
	//Please update the array 'booleanEditorNames' if any new boolean key is added
	public static final String[] booleanEditorNames 	= {
		BOOLEAN_VALUE_LIB,
		BOOLEAN_VALUE_PLUGIN,
		BOOLEAN_VALUE_SOURCE_CODE,
		BOOLEAN_VALUE_JAVADOC,
		BOOLEAN_VALUE_UPDATE_JRE
	};

	//The name of IntegerFieldEditors and they also served as key prefix in perferences.properties
	public static final String TIME_OUT 					= "timeout";
	public static final String SERVER_TIMEOUT 				= "servertimeout";
	public static final String BROWSER_TIMEOUT 				= "browsertimeout";
	//Please update the array 'intEditorNames' if any new int key is added
	public static final String[] intEditorNames = {
		TIME_OUT,
		SERVER_TIMEOUT,
		BROWSER_TIMEOUT
	};

	public static final int    TIME_OUT_VALUE 				= 5; // update site timeout in minutes

	/** '.default' served as the suffix in the properties file to form key to get default value */
	public static final String SUFFIX_DEFAULT 				= ".default";
	/** '.label' served as the suffix in the properties file to form key to get label value */
	public static final String SUFFIX_LABEL 				= ".label";

	/** '.options' served as the suffix in the properties file to form key to get options */
	public static final String SUFFIX_OPTIONS				= ".options";

}
