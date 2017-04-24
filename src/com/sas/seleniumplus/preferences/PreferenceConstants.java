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

	//The name of StringFieldEditors and they also served as key prefix in perferences.properties
	public static final String UPDATESITE_LIB_URL 			= "UpdatesiteLibUrl";
	public static final String UPDATESITE_PLUGIN_URL 		= "UpdatesitePluginUrl";
	public static final String UPDATESITE_SOURCECODE_URL 	= "UpdatesiteSourceCodeUrl";
	public static final String UPDATESITE_JAVADOC_URL 		= "UpdatesiteJavadocUrl";
	//Please update the array 'stringEditorNames' if any new string key is added
	public static final String[] stringEditorNames = {
		UPDATESITE_LIB_URL,
		UPDATESITE_PLUGIN_URL,
		UPDATESITE_SOURCECODE_URL,
		UPDATESITE_JAVADOC_URL
	};

	//The name of BooleanFieldEditors and they also served as key prefix in perferences.properties
	public static final String BOOLEAN_VALUE_LIB 			= "chkLibValue";
	public static final String BOOLEAN_VALUE_PLUGIN 		= "chkPluginValue";
	public static final String BOOLEAN_VALUE_SOURCE_CODE 	= "chkSourceCodeValue";
	public static final String BOOLEAN_VALUE_JAVADOC 		= "chkJavadocValue";
	//Please update the array 'booleanEditorNames' if any new boolean key is added
	public static final String[] booleanEditorNames 	= {
		BOOLEAN_VALUE_LIB,
		BOOLEAN_VALUE_PLUGIN,
		BOOLEAN_VALUE_SOURCE_CODE,
		BOOLEAN_VALUE_JAVADOC
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

}
