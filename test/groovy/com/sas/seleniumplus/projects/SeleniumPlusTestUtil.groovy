package com.sas.seleniumplus.projects

import static org.safs.Constants.ENV_SELENIUM_PLUS;
import static org.safs.seleniumplus.projects.BaseProject.SELENIUM_PLUS

import com.sas.seleniumplus.Activator
import com.sas.seleniumplus.builders.AppMapBuilder
import com.sas.seleniumplus.consoles.TestErrorConsoleLineTracker

import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.resources.IProject

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
			runTest(projectInfo, testClass)
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
			SELENIUM_PLUS = System.getenv(ENV_SELENIUM_PLUS)
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
		def srcDir = new File(projectDir, "Tests")
		testUtil.compile(srcDir:srcDir, destDir:binDir)
	}
	
	public void runTest(projectInfo, testClass) {
		def projectDir = projectInfo.projectDir
		def binDir = projectInfo.binDir

		def ant = new AntBuilder()
		def logFile = new File(projectDir, "log.txt")
		def logFileUtil = new LogFileUtil()
		def javaClassPath = System.getProperty("java.class.path")
		def forkedJVMDebugPort = 0
		def forkedJVMDebugSuspend = true
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
		}
	}
}