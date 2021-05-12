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
package com.sas.seleniumplus.popupmenu;
/**
 * MAY 10, 2018	(LeiWang) Refactored code to easily add file (source test, map, .ini, spring configuration etc.) to the project.
 * MAY 11, 2018	(LeiWang) Added template source code for Cycle, Suite and TestCase level.
 *                       Moved some general codes to org.safs.projects.seleniumplus.popupmenu.FileTemplates.
 * MAY 22, 2018	(LeiWang) Added testINI(): to get template content of test INI file.
 *                                        The value of 'SAFS Data Service URL' will be replaced by the value read from preference page.
 *
 */
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import org.safs.tools.drivers.DriverConstant.DataServiceConstant;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.CommonLib;
import com.sas.seleniumplus.preferences.PreferenceConstants;
import com.sas.seleniumplus.projects.BaseProject;

public class FileTemplates extends org.safs.projects.seleniumplus.popupmenu.FileTemplates {
	/**
	 * @param seleniumloc String, the SeleniumPlus installation path.
	 * @param projectName String, the project's name
	 * @return String, the content of INI file with this working template.<br>
	 *                 The value of 'SAFS Data Service URL' will be replaced by the value read from preference page.<br>
	 */
	public static InputStream testINI(String seleniumloc,String projectName) {

		String contents =	org.safs.projects.seleniumplus.popupmenu.FileTemplates.testINIContents(seleniumloc, projectName);

		//Read the 'SAFS Data Service URL' from preference page, use it to replace its value in the INI file.
		String safsdataServiceURL = null;
		try{
			safsdataServiceURL = CommonLib.getPreferenceStore().getString(PreferenceConstants.SAFS_DATA_SERVICE_URL);
		}catch(Exception e){
			Activator.warn(" Cannot deduce the safs data service URL from 'preference'。", e);
			safsdataServiceURL = DataServiceConstant.DEFAULT_SERVER_URL;
		}

		int serverurlIndex = contents.indexOf(TOKEN_SERVER_URL_EQUAL);
		if(serverurlIndex>0){
			//replace the URL
			contents = contents.substring(0, serverurlIndex+TOKEN_SERVER_URL_EQUAL.length())+safsdataServiceURL+"\n";

			//If the URL is not the last line, append the rest content of INI file
			int endServerurlIndex = contents.indexOf("\n", serverurlIndex);
			if(endServerurlIndex>0){
				contents += contents.substring(endServerurlIndex);
			}
		}

		return new ByteArrayInputStream(contents.getBytes());
	}

	/**
	 * We will initialize file contents with a sample text.
	 */
	public static InputStream appMap() {
		StringWriter write = new StringWriter();
		write.append("[ApplicationConstants]\n\n\n");
		write.append("# Window and Component definitions are added below.\n" +
		             "# Example:\n" +
				     "# \n" +
				     "# [WindowName]\n" +
		             "# Child1=\"recognition string\"\n" +
		             "# Child2=\"recognition string\"\n\n" +

		             "# Refer to the SearchObject link below for info on recognition string formats:\n" +
		             "# \n" +
		             "# http://safsdev:8880/doc/org/safs/selenium/webdriver/lib/SearchObject.html\n");
		write.flush();
		return new ByteArrayInputStream(write.toString().getBytes());
	}

	/**
	 * We will initialize file contents with a sample text.
	 * @param projectName
	 */
	public static InputStream appMapOrder(String projectName) {
		String contents = projectName+"App.map\n"+
	                      projectName+"App_en.map\n";

		return new ByteArrayInputStream(contents.getBytes());
	}

	public static InputStream getAppMap(String projectName, MapFileType fileType){
		InputStream stream = null;

		if (BaseProject.PROJECTNAME_SAMPLE.equalsIgnoreCase(projectName)){
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if(MapFileType.Map.equals(fileType)){
				stream = loader.getResourceAsStream(BaseProject.APPMAP_RESOURCE);

			}else if(MapFileType.MapEn.equals(fileType)){
				stream = loader.getResourceAsStream(BaseProject.APPMAP_EN_RESOURCE);

			}else if(MapFileType.Order.equals(fileType)){
				stream = loader.getResourceAsStream(BaseProject.APPMAP_ORDER_RESOURCE);

			}
		}else{
			if(MapFileType.Map.equals(fileType) || MapFileType.MapEn.equals(fileType)){
				stream = FileTemplates.appMap();

			}else if(MapFileType.Order.equals(fileType)){
				stream = FileTemplates.appMapOrder(projectName);
			}
		}

		return stream;
	}

	public static InputStream getTestLevelClass(String projectName, TestFileType fileType, String testClassName, String mapClassname, String childLevelTestClassName){

		if(TestFileType.TestClass.equals(fileType)){
			String[] packageAndName = splitClassName(testClassName);
			return FileTemplates.testClass(projectName, packageAndName[0], mapClassname, packageAndName[1]);

		}else if(TestFileType.TestRunClass.equals(fileType)){
			String[] packageAndName = splitClassName(testClassName);
			return FileTemplates.testRunClass(projectName, packageAndName[0], mapClassname, packageAndName[1]);

		}else if(TestFileType.TestCycle.equals(fileType)){
			return FileTemplates.testCycle(projectName, testClassName, mapClassname, childLevelTestClassName);

		}else if(TestFileType.TestSuite.equals(fileType)){
			return FileTemplates.testSuite(projectName, testClassName, mapClassname, childLevelTestClassName);

		}else if(TestFileType.TestCase.equals(fileType)){
			return FileTemplates.testCase(projectName, testClassName, mapClassname, childLevelTestClassName);

		}else{
			return null;
		}
	}

	private static InputStream testCycle(String projectName, String testClassName, String mapClassname, String childLevelTestClassName) {

		String[] pacakgeAndClassName = splitClassName(testClassName);
		String packageName = pacakgeAndClassName[0];

		String contents = packageName.isEmpty()? "":"package "+packageName+";\n\n" +

		"import org.safs.model.annotations.TestCycle;\n" +
		"import org.safs.selenium.webdriver.SeleniumPlus;\n" +
		"import org.springframework.beans.factory.annotation.Autowired;\n" +
		"import org.springframework.stereotype.Component;\n" +
		"import " + mapClassname + ";\n\n" +

		"/**\n" +
		" * It serves as a container class at Test Cycle Level, it should contain several Suite Level test classes.\n" +
		" * <p>\n" +
		" * Ultimately, it is the runTest() method that is important in this class.\n" +
		" * It is the runTest() method that is going to be automatically invoked at runtime to kickoff the test.\n" +
		" * This runTest() method is annotated by {@link TestCycle}.\n" +
		" * <p>\n" +
		" * To execute as a SeleniumPlus test, the following JARs must be in the JVM CLASSPATH:\n" +
		" * <pre>\n" +
		" * 	 pathTo/yourClasses/bin or yourTest.jar,\n" +
		" * 	 pathTo/seleniumplus.jar,\n" +
		" * 	 pathTo/JSTAFEmbedded.jar, (or JSTAF.jar if using STAF and other external tools or engines.)\n" +
		" * </pre>\n" +
		" * Then, you can execute this test with an invocation similar to:\n" +
		" * <pre>\n" +
		" * 	 java -cp %CLASSPATH% "+testClassName+"\n" +
		" * </pre>\n" +
		" *\n" +
		" * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n" +
		" */\n" +
		"/* DO NOT remove annotation @Component! */\n" +
		"@Component\n" +
		"public class Cycle extends SeleniumPlus {\n" +
		"	/* DO NOT remove annotation @Autowired! If you have more TestSuite classes,\n" +
		"	 * please add them as below with annotation @Autowired */\n" +
		"	@Autowired\n" +
		"	private "+childLevelTestClassName+" suite;\n\n" +

		"	/*\n" +
		"	 * Insert (generally) setup/tear-down methods below.\n" +
		"	 * You call them from the method runTest().\n" +
		"	 */\n\n" +

		"	/**\n" +
		"	 * This is the entry point for the automatic execution of a SeleniumPlus test.\n" +
		"	 * This will be called automatically and is not normally invoked by the developer.\n" +
		"	 * <p>\n" +
		"	 * Within this method, add calls to the test of TestSuite level.\n" +
		"	 * <p>\n" +
		"	 * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n" +
		"	 */\n" +
		"	@Override\n" +
		"	/* DO NOT remove annotation @TestCycle! */\n" +
		"	@TestCycle\n" +
		"	public void runTest() throws Throwable {\n" +
		"		suite.runTest();\n" +
		"		//otherSuite.runTest();\n" +
		"	}\n\n" +

		"	/**\n" +
		"	 * This is the entry point for the automatic execution of this SeleniumPlus test\n" +
		"	 * when executed from the command-line outside of the SeleniumPlus IDE.\n" +
		"	 * <p>\n" +
		"	 * This will be called automatically by the Java JVM when this class is invoked from the command-line by Java.\n" +
		"	 * <p>\n" +
		"	 * This method should not be altered by the user.\n" +
		"	 * <p>\n" +
		"	 * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n" +
		"	 */\n" +
		"	public static void main(String[] args) {\n" +
		"		SeleniumPlus.main(args);\n" +
		"	}\n" +
		"}";

		return new ByteArrayInputStream(contents.getBytes());
	}

	private static InputStream testSuite(String projectName, String testClassName, String mapClassname, String childLevelTestClassName) {

		String[] pacakgeAndClassName = splitClassName(testClassName);
		String packageName = pacakgeAndClassName[0];

		String contents = packageName.isEmpty()? "":"package "+packageName+";\n\n" +

		"import org.safs.model.annotations.TestSuite;\n" +
		"import org.safs.selenium.webdriver.SeleniumPlus;\n" +
		"import org.springframework.beans.factory.annotation.Autowired;\n" +
		"import org.springframework.stereotype.Component;\n" +
		"import " + mapClassname + ";\n\n" +

		"/**\n" +
		" * It serves as a container class at Test Suite Level, it should contain several TestCase Level test classes.\n" +
		" * <p>\n" +
		" * Ultimately, it is the runTest() method that is important in this class.\n" +
		" * It is the runTest() method that is going to be automatically invoked at runtime to kickoff the test.\n" +
		" * This runTest() method is annotated by {@link TestSuite}.\n" +
		" * <p>\n" +
		" * To execute as a SeleniumPlus test, the following JARs must be in the JVM CLASSPATH:\n" +
		" * <pre>\n" +
		" * 	 pathTo/yourClasses/bin or yourTest.jar,\n" +
		" * 	 pathTo/seleniumplus.jar,\n" +
		" * 	 pathTo/JSTAFEmbedded.jar, (or JSTAF.jar if using STAF and other external tools or engines.)\n" +
		" * </pre>\n" +
		" * Then, you can execute this test with an invocation similar to:\n" +
		" * <pre>\n" +
		" * 	 java -cp %CLASSPATH% "+testClassName+"\n" +
		" * </pre>\n" +
		" *\n" +
		" * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n" +
		" */\n" +
		"/* DO NOT remove annotation @Component! */\n" +
		"@Component\n" +
		"public class Suite extends SeleniumPlus {\n" +
		"	/* DO NOT remove annotation @Autowired! If you have more TestCase classes,\n" +
		"	 * please add them as below with annotation @Autowired */\n" +
		"	@Autowired\n" +
		"	private "+childLevelTestClassName+" cases1;\n\n" +

		"	/**\n" +
		"	 * This is the entry point for the automatic execution of a SeleniumPlus test.\n" +
		"	 * This will be called automatically and is not normally invoked by the developer.\n" +
		"	 * <p>\n" +
		"	 * Within this method, add calls to the test of TestCase level.\n" +
		"	 * <p>\n" +
		"	 * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n" +
		"	 */\n" +
		"	@Override\n" +
		"	/* DO NOT remove annotation @TestSuite! */\n" +
		"	@TestSuite\n" +
		"	public void runTest() throws Throwable {\n" +
		"		cases1.runTest();\n" +
		"		//otherCases.runTest();\n" +
		"	}\n\n" +

		"	/**\n" +
		"	 * This is the entry point for the automatic execution of this SeleniumPlus test\n" +
		"	 * when executed from the command-line outside of the SeleniumPlus IDE.\n" +
		"	 * <p>\n" +
		"	 * This will be called automatically by the Java JVM when this class is invoked from the command-line by Java.\n" +
		"	 * <p>\n" +
		"	 * This method should not be altered by the user.\n" +
		"	 * <p>\n" +
		"	 * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n" +
		"	 */\n" +
		"	public static void main(String[] args) {\n" +
		"		SeleniumPlus.main(args);\n" +
		"	}\n" +
		"}";

		return new ByteArrayInputStream(contents.getBytes());
	}

	private static InputStream testCase(String projectName, String testClassName, String mapClassname, String childLevelTestClassName) {

		String[] pacakgeAndClassName = splitClassName(testClassName);
		String packageName = pacakgeAndClassName[0];

		String contents = packageName.isEmpty()? "":"package "+packageName+";\n\n" +

		"import org.safs.SAFSTestLevelFailure;\n" +
		"import org.safs.SAFSTestLevelError;\n" +
		"import org.safs.model.annotations.TestCase;\n" +
		"import org.safs.selenium.webdriver.SeleniumPlus;\n" +
		"import org.springframework.beans.factory.annotation.Autowired;\n" +
		"import org.springframework.stereotype.Component;\n" +
		"import " + mapClassname + ";\n\n" +

		"/**\n" +
		" * It serves as a container class at Test Case Level, it should contain several {@link TestCase} methods.\n" +
		" * <p>\n" +
		" * Ultimately, it is the runTest() method that is important in this class.\n" +
		" * It is the runTest() method that is going to be automatically invoked at runtime to kickoff the test.\n" +
		" * This runTest() method should call the methods annotated by {@link TestCase}.\n" +
		" * <p>\n" +
		" * To execute as a SeleniumPlus test, the following JARs must be in the JVM CLASSPATH:\n" +
		" * <pre>\n" +
		" * 	 pathTo/yourClasses/bin or yourTest.jar,\n" +
		" * 	 pathTo/seleniumplus.jar,\n" +
		" * 	 pathTo/JSTAFEmbedded.jar, (or JSTAF.jar if using STAF and other external tools or engines.)\n" +
		" * </pre>\n" +
		" * Then, you can execute this test with an invocation similar to:\n" +
		" * <pre>\n" +
		" * 	 java -cp %CLASSPATH% "+testClassName+"\n" +
		" * </pre>\n" +
		" *\n" +
		" * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n" +
		" */\n" +
		"/* DO NOT remove annotation @Component! */\n" +
		"@Component\n" +
		"public class Cases1 extends SeleniumPlus {\n" +
		"	/* DO NOT remove annotation @Autowired! */\n" +
		"	/**\n" +
		" 	* The field 'self' represents this TestCase level class itself,\n" +
		" 	* any methods annotated with @TestCase MUST be called through it.\n" +
		" 	*/\n" +
		"	@Autowired\n" +
		"	private "+pacakgeAndClassName[1]+" self;\n\n" +

		"	/* DO NOT remove annotation @TestCase! */\n" +
		"	@TestCase\n" +
		"	public void case1() throws Throwable {\n" +
		"		//Insert test codes \n" +
		"	}\n\n" +

		"	/* DO NOT remove annotation @TestCase!\n" +
		"	 * The annotation TestCase's property 'skipped' is set to true, this TestCase will be skipped.\n" +
		"	 * The annotation TestCase's property 'skippedMessage' is set with the reason why it is skipped. */\n" +
		"	@TestCase(skipped=true, skippedMessage=\"This test case will be skipped for some reason\")\n" +
		"	public void case2(){\n" +
		"		//Insert test codes \n" +
		"	}\n\n" +

		"	/* DO NOT remove annotation @TestCase!\n" +
		"	 * This case shows how to use SAFSTestLevelFailure to mark the failure of a test case.\n" +
		"	 * This SAFSTestLevelFailure contains ONE failure message. */\n" +
		"	@TestCase\n" +
		"	public void case3(){\n" +
		"		if(!SeleniumPlus.VerifyFileToFile(\"benchFile.txt\", \"actualFile.txt\")){\n" +
		"			throw new SAFSTestLevelFailure(\"benchFile.txt doesn't match actualFile.txt.\",\"detailed failure message.\", \"DIFF\");\n"+
		"		}\n" +
		"	}\n\n" +

		"	/* DO NOT remove annotation @TestCase!\n" +
		"	 * This case shows how to use SAFSTestLevelFailure to mark the failure of a test case.\n" +
		"	 * This SAFSTestLevelFailure contains multiple failure messages. */\n" +
		"	@TestCase\n" +
		"	public void case4(){\n" +
		"		SAFSTestLevelFailure testLevelException = new SAFSTestLevelFailure();\n"+
		"		org.safs.model.Component nonExistGui = new org.safs.model.Component(\"FANTACY_GUI\");\n\n"+
		"		if(!Misc.IsComponentExists(nonExistGui)){\n"+
		"			testLevelException.addFailure(\"IsComponentExists failed.\", nonExistGui.getName()+\" doesn't exist!\", \"NON_EXIST\");\n"+
		"		}\n\n"+
		"		if(!SeleniumPlus.WaitForGUI(nonExistGui, 3)){\n"+
		"			testLevelException.addFailure(\"WaitForGUI failed.\", nonExistGui.getName()+\" doesn't exist!\", \"TIMEOUT\");\n"+
		"		}\n"+
		"		throw testLevelException;\n"+
		"	}\n\n" +

		"	/* DO NOT remove annotation @TestCase!\n" +
		"	 * This case shows how to use SAFSTestLevelError to mark the error of a test case.*/\n" +
		"	@TestCase\n" +
		"	public void case5(){\n" +
		"		String name = null;\n" +
		"		try{\n"+
		"			name.length();\n"+
		"		}catch(NullPointerException e){\n"+
		"			throw new SAFSTestLevelError(e.getMessage(), null, e.getClass().getSimpleName());\n"+
		"		}\n"+
		"	}\n\n" +

		"	/*\n" +
		"	 * Insert more TestCase methods below.\n" +
		"	 * You call them from the method runTest() through field 'self'.\n" +
		"	 * DON NOT forget the annotate the method with @TestCase.\n" +
		"	 */\n\n" +

		"	/**\n" +
		"	 * This is the entry point for the automatic execution of a SeleniumPlus test.\n" +
		"	 * This will be called automatically and is not normally invoked by the developer.\n" +
		"	 * <p>\n" +
		"	 * Within this method, call the methods annotated by {@link TestCase}.\n" +
		"	 * <font color=\"red\">These TestCase methods MUST be called through the field {@link #self} annotated by {@link Autowired}.</font>\n" +
		"	 * <p>\n" +
		"	 * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n" +
		"	 */\n" +
		"	@Override\n" +
		"	public void runTest() throws Throwable {\n" +
		"		self.case1();\n" +
		"		self.case2();\n" +
		"		self.case3();\n" +
		"		self.case4();\n" +
		"		self.case5();\n" +
		"		//self.otherCase();\n" +
		"	}\n\n" +

		"	/**\n" +
		"	 * This is the entry point for the automatic execution of this SeleniumPlus test\n" +
		"	 * when executed from the command-line outside of the SeleniumPlus IDE.\n" +
		"	 * <p>\n" +
		"	 * This will be called automatically by the Java JVM when this class is invoked from the command-line by Java.\n" +
		"	 * <p>\n" +
		"	 * This method should not be altered by the user.\n" +
		"	 * <p>\n" +
		"	 * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n" +
		"	 */\n" +
		"	public static void main(String[] args) {\n" +
		"		SeleniumPlus.main(args);\n" +
		"	}\n" +
		"}";

		return new ByteArrayInputStream(contents.getBytes());
	}

	/**
	 * We will initialize a new TestCase class with this template.
	 */
	public static InputStream testClass(String projectName, String packageName, String mapPkgName, String filename) {

		//Return the special test class for 'sample' project
		if (BaseProject.PROJECTNAME_SAMPLE.equalsIgnoreCase(projectName)){
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			return loader.getResourceAsStream(BaseProject.TESTCASECLASS_RESOURCE);
		}

		String contents =
				packageName.isEmpty()? "": "package " + packageName + ";\n\n" +
				"import org.safs.selenium.webdriver.SeleniumPlus;\n" +
				"import " + mapPkgName + ";\n\n" +
				"/** \n" +
				" * Used to hold a number of related testcase methods invokable from any class needing them. \n"  +
				" * <p>\n"  +
				" * To execute as a SeleniumPlus Unit test for this class, the runTest() method must exist and \n" +
				" * should contain appropriate testcase method invocations. \n" +
				" * The following JARs must be in the JVM CLASSPATH for such a Unit test invocation. \n"  +
				" * This is the same as any other SeleniumPlus test invocation: \n"  +
				" * <pre>\n"  +
				" * \t pathTo/yourClasses/bin or yourTest.jar,\n"  +
				" * \t pathTo/seleniumplus.jar,\n"  +
				" * \t pathTo/JSTAFEmbedded.jar, (or JSTAF.jar if using STAF and other external tools or engines.)\n"  +
				" * </pre>\n"  +
				" * Then, you can execute this test with an invocation similar to:\n"  +
				" * <pre>\n" +
				" * \t java -cp %CLASSPATH% "+ (packageName.isEmpty()? "": packageName+".") + filename +"\n"  +
				" * </pre>\n"  +
				" * \n" +
				" * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n"  +
				" */ \n" +
				"public class " + filename + " extends SeleniumPlus {\n"  +
				"\n\n" +

				"\t/* \n" +
				"\t * Insert (generally) static testcase methods below. \n"  +
				"\t * You call these from your TestRun runTest() method for normal testing, \n "+
				"\t * your TestCase runTest() method for Unit testing, \n"  +
				"\t * or from other testcases, testcase classes, or anywhere they are needed. \n"  +
				"\t */ \n\n\n\n"  +

				"\t/** \n" +
				"\t * Normally not used for TestCase classes. \n" +
				"\t * Can be used to implement a Unit test for this TestCase class, or as a test suite. \n" +
				"\t * <p>\n" +
				"\t * Within this method, add calls to the testcase methods you wish to execute. \n" +
				"\t * You are not limited to calling methods in this class only. \n" +
				"\t * <p>\n" +
				"\t * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n"  +
				"\t */\n" +
				"\t@Override\n" +
				"\tpublic void runTest() throws Throwable {\n\n" +
				"\t}\n" +

				"\t/** \n" +
				"\t * Normally not used for TestCase classes. \n" +
				"\t * This is the entry point for the automatic execution of this SeleniumPlus test \n" +
				"\t * when executed from the command-line outside of the SeleniumPlus IDE. \n" +
				"\t * <p>\n" +
				"\t * This will be called automatically by the Java JVM if this class is invoked from the command-line by Java. \n" +
				"\t * <p>\n" +
				"\t * This method should not be altered by the user. \n" +
				"\t * <p>\n" +
				"\t * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n"  +
				"\t */\n" +
				"\tpublic static void main(String[] args) { SeleniumPlus.main(args); }\n" +
				"}\n";
		return new ByteArrayInputStream(contents.getBytes());
	}

	/**
	 * We will initialize a new TestRun class with this template.
	 */
	public static InputStream testRunClass(String projectName, String packageName, String mapPkgName, String filename) {
		//Return the special test run class for 'sample' project
		if (BaseProject.PROJECTNAME_SAMPLE.equalsIgnoreCase(projectName)){
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			return loader.getResourceAsStream(BaseProject.TESTRUNCLASS_RESOURCE);
		}

		String contents =
				packageName.isEmpty()? "": "package " + packageName + ";\n\n" +
				"import org.safs.selenium.webdriver.SeleniumPlus;\n" +
				"import " + mapPkgName + ";\n\n" +
				"/** \n" +
				" * Used to hold a number of related testcase methods invokable from any class needing them. \n"  +
				" * For such a TestRun class, this may be certain setup and tear-down methods used prior to calling \n"  +
				" * the testcase methods of other TestCase classes. \n"  +
				" * <p>\n"  +
				" * Ultimately, it is the runTest() method that is important in this class. \n"  +
				" * It is the runTest() method that is going to be automatically invoked at runtime to kickoff the test. \n"  +
				" * <p>\n"  +
				" * To execute as a SeleniumPlus test, the following JARs must be in the JVM CLASSPATH:\n"  +
				" * <pre>\n"  +
				" * \t pathTo/yourClasses/bin or yourTest.jar,\n"  +
				" * \t pathTo/seleniumplus.jar,\n"  +
				" * \t pathTo/JSTAFEmbedded.jar, (or JSTAF.jar if using STAF and other external tools or engines.)\n"  +
				" * </pre>\n"  +
				" * Then, you can execute this test with an invocation similar to:\n"  +
				" * <pre>\n" +
				" * \t java -cp %CLASSPATH% "+ (packageName.isEmpty()? "": packageName+".") + filename +"\n"  +
				" * </pre>\n"  +
				" * \n" +
				" * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n"  +
				" */ \n" +
				"public class " + filename + " extends SeleniumPlus {"  +
				"\n\n" +

				"\t/* \n" +
				"\t * Insert (generally) static testcase methods or setup/teardown methods below. \n"  +
				"\t * You call these from your runTest() method for normal testing, \n "+
				"\t * or from other testcases, testcase classes, or anywhere they are needed. \n"  +
				"\t */ \n\n\n\n"  +

				"\t/** \n" +
				"\t * This is the entry point for the automatic execution of a SeleniumPlus test. \n" +
				"\t * This will be called automatically and is not normally invoked by the developer. \n" +
				"\t * <p>\n" +
				"\t * Within this method, add calls to the testcase methods you wish to execute for this test. \n" +
				"\t * <p>\n" +
				"\t * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n"  +
				"\t */\n" +
				"\t@Override\n" +
				"\tpublic void runTest() throws Throwable {\n\n" +
				"\n\n\n\n\n\n\n\n\n\n" +
				"\t}\n\n\n" +

				"\t/** \n" +
				"\t * This is the entry point for the automatic execution of this SeleniumPlus test \n" +
				"\t * when executed from the command-line outside of the SeleniumPlus IDE. \n" +
				"\t * <p>\n" +
				"\t * This will be called automatically by the Java JVM when this class is invoked from the command-line by Java. \n" +
				"\t * <p>\n" +
				"\t * This method should not be altered by the user. \n" +
				"\t * <p>\n" +
				"\t * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])\n"  +
				"\t */\n" +
				"\tpublic static void main(String[] args) { SeleniumPlus.main(args); }\n" +
				"}\n";
		return new ByteArrayInputStream(contents.getBytes());
	}

	/**
	 *  We will initialize a 'Test01' testcase method with this working template.
	 */
	public static String getMethodSignature(){
		return "\t/** \n" +
			   "\t * Edit (or remove) the description of the testcase method here. \n" +
			   "\t * Rename the testcase method as appropriate. \n" +
			   "\t * Add parameters/arguments to the testcase method, if needed. \n" +
			   "\t */\n" +
	           "\tpublic static void Test01() {\n" +
			   "\n\n" +
			   "\t}\n\n";
	}

	/**
	 *  This template is used to generate testing method in Regression test case.
	 */
	public static String getRegressionTestingMethodSignature(){
		return "\t/** \n" +
			   "\t * Edit (or remove) the description of the testing method here. \n" +
			   "\t * Rename the testing method as appropriate. \n" +
			   "\t * Add parameters/arguments to the testing method, if needed. \n" +
			   "\t * But the default parameter 'counterPrefix' can NOT be deleted,\n" +
			   "\t * which will be used to generate Regression summary report.\n" +
			   "\t */\n" +
	           "\tprivate static int testXXXAPI(String counterPrefix) throws Throwable{\n" +
			   "\t\tString counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));\n" +
	           "\t\tint fail = 0;\n" +
			   "\t\tCounters.StartCounter(counterID);\n" +
	           "\n\t\t// Insert the contents of your testing method here:\n" +
	           "\t\t// ...\n" +
			   "\n\n\n" +
			   "\t\tCounters.StopCounter(counterID);\n" +
			   "\t\tCounters.StoreCounterInfo(counterID, counterID);\n" +
			   "\t\tCounters.LogCounterInfo(counterID);\n" +
			   "\n" +
			   "\t\tif(fail > 0){\n" +
			   "\t\t\tLogging.LogTestFailure(counterID + \" \" + fail + \" UNEXPECTED test failures!\");\n" +
			   "\t\t }else{\n" +
			   "\t\t\tLogging.LogTestSuccess(counterID + \" did not report any UNEXPECTED test failures!\");\n" +
			   "\t\t}\n" +
			   "\n\t\treturn fail;\n" +
			   "\t}\n\n";
	}
}
