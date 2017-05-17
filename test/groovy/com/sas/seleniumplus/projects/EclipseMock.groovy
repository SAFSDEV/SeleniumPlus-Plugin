package com.sas.seleniumplus.projects

import com.sas.seleniumplus.preferences.PreferenceConstants
import com.sas.seleniumplus.projects.BaseProject

import org.eclipse.core.runtime.IPath
import org.eclipse.core.internal.resources.WorkspaceRoot
import org.eclipse.core.internal.utils.FileUtil
import org.eclipse.core.resources.IBuildConfiguration
import org.eclipse.core.resources.ICommand
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.preferences.IPreferencesService
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.internal.core.JavaElement
import org.eclipse.jdt.internal.core.JavaProject
import org.eclipse.jface.preference.IPreferenceStore

import org.mockito.Matchers
import org.mockito.Mockito

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext

import org.powermock.api.mockito.PowerMockito

import spock.lang.Specification

class EclipseMock extends Specification {
	def folderMap
	def projectMap
	IWorkspace workspace
	public File workspaceDir
	
	public init() {
		projectMap = [:]
		folderMap = [:]
		
		workspace = createWorkspace()
		
		mockStaticPlatformGetPreferencesService()

	}
	
	public getBundleContext() {
		def bundleContext = Mock(BundleContext)
		
		def bundle = Mock(Bundle)
		bundle.getSymbolicName() >> "Seleniumplus_plugin"
		
		bundleContext.getBundle() >> bundle
		
		IPreferenceStore store = Mock(IPreferenceStore)
		bundleContext.getPreferenceStore() >> store
		
		bundleContext
	}

	public initMocksForProject(projectName) {
		def projectInfo = getProjectInfo(projectName)		
		projectInfo.projectDir = new File(workspaceDir, projectName)
		
		IProject project = Mock(IProject)
		projectInfo.mock = project

		IPath projectPath = Mock(IPath)
		IPath binPath = Mock(IPath)
		
		projectPath.append("bin") >> binPath

		project.getWorkspace() >> workspace
		project.getName() >> projectName
		project.isOpen() >> false

		IBuildConfiguration buildConfiguration = Mock(IBuildConfiguration)
		buildConfiguration.getProject() >> project
		project.getBuildConfig(_) >> buildConfiguration

		IPath projectLocation = Mock(IPath)
		projectLocation.toString() >> projectInfo.projectDir.absolutePath.replaceAll("\\\\", "/")
		
		project.getLocation() >> projectLocation
		
		createJavaProject(projectName)
		
		
		folderMap.put(projectInfo.projectDir, project)

		project.getFolder(_) >> { String tempPath ->
			def actualFile = new File(projectInfo.projectDir, tempPath)
			createFolder(projectInfo.projectDir, actualFile)
		}
		project.getType() >> IResource.PROJECT
		project.getFullPath() >> projectPath
		project.getDescription() >> {
			def projectDescription = getProjectInfo(projectName).description
			projectDescription
		}
		project.getFile(_) >> { IPath mypath ->
			def filename = mypath.toString()
			getFile(projectInfo.projectDir, filename)
		}
		
		project.create(_, _) >> { IProjectDescription desc, arg2 ->
			def projectDescription = getProjectInfo(projectName).description
			assert desc.is(projectDescription)
			// This is where eclipse creates the .project file.
			// This test does not need that, but it does need the project directory created.
			projectInfo.projectDir.mkdirs()
		}
		
		project.open(_) >> {
			// This is where eclipse creates the bin dir.
			projectInfo.binDir = new File(projectInfo.projectDir, "bin")
			projectInfo.binDir.mkdirs()
		}
		projectInfo
	}

	private getProjectInfo(projectName) {
		def projectInfo = projectMap.get(projectName)
		if (projectInfo == null) {
			projectInfo = [:]
			projectMap.put(projectName, projectInfo)
		}
		projectInfo
	}
	
	private IWorkspace createWorkspace() {
		IWorkspace workspace = Mock(IWorkspace)
		
		// when ResourcesPlugin.getWorkspace() is called, return the mocked workspace.
		PowerMockito.mockStatic(ResourcesPlugin.class)
		Mockito.when(ResourcesPlugin.getWorkspace()).thenReturn(workspace)
		
		def workspaceRoot = Mock(WorkspaceRoot)
		// when getRoot() is called, return the mocked workspaceRoot
		workspace.getRoot() >> workspaceRoot
		
		workspaceRoot.getProject(_) >> { String projectName ->
			def project = getProjectInfo(projectName).mock
			assert project != null
			project
		}
		workspace.newProjectDescription(_) >> { String projectName ->
			ICommand command = Mock(ICommand)
			
			// projectDescription is the .project file.
			IProjectDescription projectDescription = Mock(IProjectDescription)
			projectDescription.newCommand() >> command
			
			String[] natureIds = ["org.eclipse.jdt.core.javanature", "org.eclipse.wst.common.project.facet.core.nature"] as String[]
			projectDescription.getNatureIds() >> natureIds

			def projectInfo = getProjectInfo(projectName)
			projectInfo.description = projectDescription
			projectDescription
		}

		workspace
	}
	
	private createJavaProject(projectName) {
		JavaProject javaProject = PowerMockito.mock(JavaProject.class)
		
		// when the constructor for JavaProject is called, return the mock javaProject
		PowerMockito.whenNew(JavaProject.class).
			withParameterTypes(IProject.class, JavaElement.class).
			withArguments(Matchers.any(), Matchers.any()).
			thenReturn(javaProject)
			
		// when javaProject.getPackageFragments() is called, return a mocked packageFragment
		IPackageFragment packageFragment = Mock(IPackageFragment)
		packageFragment.getKind() >> IPackageFragmentRoot.K_SOURCE
		packageFragment.getElementName() >> projectName.toLowerCase()
		
		Mockito.when(javaProject.getPackageFragments()).thenReturn([packageFragment] as IPackageFragment[])

	}
	
	private mockStaticPlatformGetPreferencesService() {
		IPreferencesService preferencesService = Mock(IPreferencesService)
		preferencesService.get(PreferenceConstants.BOOLEAN_VALUE_JAVADOC) >> false
		
		PowerMockito.mockStatic(Platform.class)
		Mockito.when(Platform.getPreferencesService()).thenReturn(preferencesService)
	}	
	
	def createFolder(projectDir, actualFile) {
		IFolder tempFolder = Mock(IFolder)
		tempFolder.exists() >> {
			actualFile.exists()
		}
		def parentFile = actualFile.parentFile
		def parentFolder = folderMap.get(parentFile)
		if (!parentFolder) {
			parentFolder = createFolder(projectDir, parentFile)
		}
		tempFolder.getParent() >> parentFolder
		
		
		tempFolder.getFile(_) >> { String filename ->
			getFile(actualFile, filename)
		}
		IPath tempIPath = Mock(IPath)
		tempIPath.isAbsolute() >> true

		tempFolder.getFullPath() >> tempIPath
		
		tempFolder.create(_, _, _) >> {
			actualFile.mkdir()
		}
		tempFolder.toString() >> {
			def relativePath = actualFile.absolutePath - projectDir.absolutePath
			relativePath = relativePath.replaceAll("\\\\", "/")
			relativePath.substring(1)
		}
		folderMap.put(actualFile, tempFolder)
		tempFolder

	}
	
	private getFile(actualFile, filename) {
		IFile tempFile = Mock(IFile)
		tempFile.create(_, _, _) >> { InputStream source, boolean force, Object monitor ->
			// TODO: uncomment the next line after getting the samples files on the Eclipse classpath.
			// assert source != null
			if (source == null) {
				/*
				 * The files were not loaded in the normal manner, so they will be
				 * read directly as files.
				 */
				def resource
				switch (filename) {
					case "TestCase1.java":
						resource = BaseProject.TESTCASECLASS_RESOURCE
						break;
					case "TestRun1.java":
						resource = BaseProject.TESTRUNCLASS_RESOURCE
						break;
					case "SAMPLEApp.map":
						resource = BaseProject.APPMAP_RESOURCE
						break;
					case "SAMPLEApp_en.map":
						resource = BaseProject.APPMAP_EN_RESOURCE
						break;
					case BaseProject.APPMAP_ORDER_FILE:
						resource = BaseProject.APPMAP_ORDER_RESOURCE
						break;
					default:
						throw new RuntimeException("Unknown filename: $filename")
				}
				def file = new File(".$resource")
				source = new FileInputStream(file)
				assert source != null
			}
			def outFile = new File(actualFile, filename)
			def out = new FileOutputStream(outFile)
			FileUtil.transferStreams(source, out, actualFile.absolutePath, null)
		}
		tempFile
	}

}