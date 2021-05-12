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
package com.sas.seleniumplus.projects

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.safs.selenium.webdriver.lib.WDLibrary

import com.sas.seleniumplus.Activator
import com.sas.seleniumplus.builders.AppMapBuilder

public class SeleniumPlusTestUtil {
	def testUtil = new TestUtil()

	/**
	 * With the Eclipse framework mocked, initialize SeleniumPlus; create
	 * a project with projectName and projectType; generate Map.java, compile
	 * the classes, and run the testClass.
	 *
	 * @param map containing projectName, projectType, and testClass.
	 */
	public void buildProjectAndRunTest(map, closure) {
		def projectName = map.projectName
		def projectType = map.projectType
		def testClass = map.testClass

		withSeleniumPlusEnv { eclipse ->

			def projectInfo = eclipse.initMocksForProject(projectName)

			IProject project = createProject(projectName, projectType)
			generateMapJavaFile(project)
			compileClasses(projectInfo)
			runTest(projectInfo, testClass, map.seleniumPlusEnvDir, map.seleniumPlusPropertyDir)
			if (closure) {
				closure(projectInfo)
			}
		}
	}

	/**
	 * Initialize SeleniumPlus and a mock of the Eclipse framework before
	 * calling the input closure that runs a test.  After the test closure
	 * returns, the Selenium server is stopped.
	 *
	 * @param closure called with a reference to the Eclipse mock to run the test.
	 */
	public void withSeleniumPlusEnv(closure) {
		// set the closure's delegate to this class so methods of this class can
		// be invoked without prefixing the class instance reference.
		closure.setResolveStrategy(Closure.DELEGATE_FIRST);
		closure.setDelegate(this);

		testUtil.withTempDir() { tempDir ->
			def workspaceDir = tempDir

			// initialize the mock of the Eclipse framework.
			def eclipse = new EclipseMock(workspaceDir:workspaceDir)
			eclipse.init()

			// initialize SeleniumPlus
			BaseProject.SELENIUM_PLUS = System.getenv(BaseProject.SELENIUM_PLUS_ENV)
			def activator = new Activator()
			activator.start(eclipse.getBundleContext())

			try {
				// call the closure that runs the test
				closure(eclipse)

			} finally {
				/*
				 * Since the selenium server has a working directory in the
				 * workspace (at the project root), it has to be stopped
				 * or the temporary directory cannot be deleted.
				 */
				stopSeleniumServer()
			}
		}
	}

	public IProject createProject(projectName, projectType) {
		def location = null
		def companyName = "sas"
		IProject project = BaseProject.createProject(
			projectName,
			location,
			companyName,
			projectType
		)
		project
	}

	public void generateMapJavaFile(IProject project) {
		// generate Map.java
		def builder = new AppMapBuilder()
		builder.setBuildConfig(project.getBuildConfig(""))

		def kind = IncrementalProjectBuilder.AUTO_BUILD
		def args = [:]
		def monitor = null
		builder.build(kind, args, monitor)
	}

	public void compileClasses(projectInfo) {
		def projectDir = projectInfo.projectDir
		def binDir = projectInfo.binDir

		// Compile the test classes to bin with the other tests
		def srcDir = new File(projectDir, BaseProject.SRC_TEST_DIR)
		testUtil.compile(srcDir:srcDir, destDir:binDir)
	}

	public void runTest(projectInfo, testClass, seleniumPlusEnvDir=null, seleniumPlusPropertyDir=null) {
		def projectDir = projectInfo.projectDir
		def binDir = projectInfo.binDir

		def ant = new AntBuilder()
		def logFile = new File(projectDir, "log.txt")
		def logFileUtil = new LogFileUtil()
		def javaClassPath = System.getProperty("java.class.path")
		def forkedJVMDebugPort = System.getProperty("org.safs.seleniumplustest.forked.jvm.debug.port", "")
		def forkedJVMDebugSuspend = true

		def bogusSafsDir = new File("/should/not/exist")
		assert ! bogusSafsDir.exists()

		def result = logFileUtil.withLogFile(logFile) {
			ant.java(
				classname:testClass,
				failonerror:true,
				fork:true,
				dir:projectDir,
				output:logFile,
				) {
				classpath {
					pathelement(location: binDir)
					pathelement(path:javaClassPath)
				}
				env(key:'SAFSDIR', file:bogusSafsDir)
				if (seleniumPlusEnvDir) {
					env(key:'SELENIUM_PLUS', file:seleniumPlusEnvDir)
				}
				if (seleniumPlusPropertyDir) {
					sysproperty(key:'SELENIUM_PLUS_OVERRIDE', value:true)
					sysproperty(key:'SELENIUM_PLUS', file:seleniumPlusPropertyDir)
				}
				if (forkedJVMDebugPort) {
					jvmarg(value:'-Xdebug')
					def suspend = forkedJVMDebugSuspend ? 'y' : 'n'
					jvmarg(value:"-Xrunjdwp:transport=dt_socket,address=${forkedJVMDebugPort},server=y,suspend=$suspend")
				}
			}
		}
		if (result.throwable) throw result.throwable

		logFile.eachLine { line ->
			assert !line.contains("**FAILED**")
		}
	}

	private stopSeleniumServer() {
		try {
			new URL("http://localhost:4444/selenium-server/driver/?cmd=shutDownSeleniumServer").text
			Thread.currentThread().sleep(2000)
		} catch (java.net.ConnectException e) {
			// the server is not running - OK.
		}catch(Exception x){
		    //catch what ever exception
		}finally{
			try{
				WDLibrary.stopSeleniumServer(null);
			}catch(Exception){ /*ignore it*/}
		}
	}
}
