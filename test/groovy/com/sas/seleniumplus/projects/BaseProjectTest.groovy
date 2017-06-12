package com.sas.seleniumplus.projects

import com.sas.seleniumplus.projects.BaseProject

import java.nio.file.ClosedFileSystemException
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Platform
import org.eclipse.jdt.internal.core.JavaModelManager
import org.eclipse.jdt.internal.core.JavaModel
import org.eclipse.jdt.internal.core.JavaProject
import static org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner



@SuppressWarnings('MethodName') // prevent CodeNarc from complaining about String method names
@RunWith( PowerMockRunner.class )
@PrepareForTest([ResourcesPlugin.class, Platform.class, JavaProject.class, JavaModelManager.class, JavaModel.class])
class BaseProjectTest {
	def seleniumPlusTestUtil = new SeleniumPlusTestUtil()
	
	@Test
	void "Test SAMPLE project with mocked Eclipse"() {
		seleniumPlusTestUtil.buildProjectAndRunTest(
				projectName:  SampleProjectNewWizard.PROJECT_NAME,
				projectType:  BaseProject.PROJECTTYPE_SAMPLE,
				testClass:    'sample.testcases.TestCase1',
		) { projectInfo ->
			/*
			 * Make sure the screenshot was taken.
			 */
			def projectDir = projectInfo.projectDir
			def actualDir = new File(projectDir, "Actuals")
			assertTrue(actualDir.exists())
			def files = actualDir.listFiles() as List
			
			assertFalse("The screenshot was not taken.", files.isEmpty())
		}
	}
}
