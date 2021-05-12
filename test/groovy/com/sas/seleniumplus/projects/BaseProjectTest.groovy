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

	@Test
	void "Test SELENIUM_PLUS environment variable override"() {
		/*
		 * This is largely a copy of "Test SAMPLE project with mocked Eclipse" above.
		 * However, it overrides the SELENIUM_PLUS environment variable.
		 * The system property of the same name is used instead.
		 *
		 * Note:  this override functionality is part of SAFS Core.
		 * This really should be tested in the SAFS Core tests.
		 * However, the SAFS Core code requires the use of a packaged
		 * distribution.
		 * Having a test in SAFS Core that requires a package is not good.
		 * Changes to the code would require a repackaging.
		 * So, putting a test in this project is better because it
		 * already requires the package distribution.
		 * It would be best if the SAFS Core code that requires the
		 * packaged distribution was changed to not require it.
		 * Then, this functionality could be tested by the SAFS Core tests.
		 * So, the SAFS Core tests would be testing SAFS Core functionality.
		 */

		// set the SELENIUM_PLUS environment variable to something
		// that does not exist.  If it is not properly overriden, this
		// test fails.
		def seleniumPlusEnvDir = new File("/should/not/exist")
		assert ! seleniumPlusEnvDir.exists()

		seleniumPlusTestUtil.buildProjectAndRunTest(
				projectName:  SampleProjectNewWizard.PROJECT_NAME,
				projectType:  BaseProject.PROJECTTYPE_SAMPLE,
				testClass:    'sample.testcases.TestCase1',
				seleniumPlusEnvDir: seleniumPlusEnvDir,
				// now set the property to the value of the environment variable.
				seleniumPlusPropertyDir: System.getenv("SELENIUM_PLUS"),
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
