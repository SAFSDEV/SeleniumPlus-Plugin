package com.sas.seleniumplus.popupmenu;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

public class FileTemplates {

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
		             "# http://safsdev.sourceforge.net/doc/org/safs/selenium/webdriver/lib/SearchObject.html\n");
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
	
	/**
	 * We will initialize a new TestCase class with this template.
	 */
	public static InputStream testClass(String projectName, String packageName, String mapPkgName, String filename) {
		String contents = "package " + packageName + ";\n\n" +
				"import org.safs.selenium.webdriver.SeleniumPlus;\n" +
				"import " + mapPkgName + ";\n\n" +
				"/** \n" +
				" * Used to hold a number of related testcase methods invocable from any class needing them. \n"  +
				" * <p>\n"  +
				" * To execute as a SeleniumPlus Unit testfor this class, the runTest() method must exist and \n" +
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
				" * \t java -cp %CLASSPATH% "+ packageName +"."+ filename +"\n"  +
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
		String contents = 
				"package " + packageName + ";\n\n" +
				"import org.safs.selenium.webdriver.SeleniumPlus;\n" +
				"import " + mapPkgName + ";\n\n" +
				"/** \n" +
				" * Used to hold a number of related testcase methods invocable from any class needing them. \n"  +
				" * For such a TestRun class, this may be certain setup and teardown methods used prior to calling \n"  +
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
				" * \t java -cp %CLASSPATH% "+ packageName +"."+ filename +"\n"  +
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
	 * We will initialize the INI file with this working template.
	 */
	public static InputStream testINI(String seleniumloc,String projectName) {
		
		String contents =	"\n[STAF]\n" +
  				            "# Comment out the line below (using ; or #) to turn OFF the Debug Log to improve performance slightly.\n" +
				            "EmbedDebug=\"DebugLog.txt\"\n" +
							"\n" +
							
				            "[SAFS_DRIVER]\n" + 
							"DriverRoot=\"%SELENIUM_PLUS%\\extra\\automation\"\n" +
							"# Uncomment showMonitor below to use the SAFS Monitor during testing.\n" +
							"# showMonitor=True\n" +
							"\n" +
							
							"[SAFS_DIRECTORIES]\n" +
							"DATADIR=Maps\n" +
							"BENCHDIR=Benchmarks\n" +
							"DIFFDIR=Diffs\n" +
							"LOGDIR=Logs\n" +
							"TESTDIR=Actuals\n" +
							"\n" +
							
							"[SAFS_SELENIUM]\n" +
							"# Grid or Remote Selenium Server\n" +
							"#SELENIUMHOST=host.domain.com\n" +
							"#SELENIUMPORT=4444\n" +
							"\n" +
							
							"[SAFS_TEST]\n" +
							"TestName=\""+ projectName + "\"\n" + 
							"TestLevel=\"Cycle\"\n" +
							"CycleSeparator=\"\t\"\n" +
							"# CycleLogName=\""+ projectName + "\"\n" +
							"\n" +
							
							"# 3 logmodes all enabled below.\n" +
							"# Delete those you will not use to improve performance.\n" +
							"CycleLogMode=\"TEXTLOG CONSOLELOG XMLLOG\"\n" +
							"\n" + 
							
							"# secsWaitForWindow=30\n" +
							"# secsWaitForComponent=30\n";		
			
		return new ByteArrayInputStream(contents.getBytes());
	}	
	
}
