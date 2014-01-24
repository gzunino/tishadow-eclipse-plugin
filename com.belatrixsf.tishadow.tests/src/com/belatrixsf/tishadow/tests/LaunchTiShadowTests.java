package com.belatrixsf.tishadow.tests;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.internal.junit.ui.JUnitViewEditorLauncher;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.belatrixsf.tishadow.LaunchUtils;

@SuppressWarnings("restriction")
public class LaunchTiShadowTests implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, final IProgressMonitor monitor) throws CoreException {
		final SubMonitor mon = SubMonitor.convert(monitor);
		mon.beginTask("Running Tests", IProgressMonitor.UNKNOWN);
		
		String hideWizard = Activator.getDefault().getPreferenceStore().getString("tishadow.hideWizard");
		
		if(!MessageDialogWithToggle.ALWAYS.equals(hideWizard)) {
			String instructionsMessage = "1 - Make sure the tishadow server is running. You can start it using the Run Tishadow server option in the context menu.\n";
			instructionsMessage += "2 - Open the tishadow application on the devices you want to use to run the tests and connect them to the server.\n";
			instructionsMessage += "3 - Once this is done the tests will run properly.\n";
			
			MessageDialogWithToggle.openInformation(
				null, 
				"TiShadow Wizard", 
				instructionsMessage, 
				"Do not show this wizard again", 
				true, 
				Activator.getDefault().getPreferenceStore(), 
				"tishadow.hideWizard"
			);
		}
		
		final String projectLoc = configuration.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, "");
		final IProject project = getProject(projectLoc);
		if (project == null || !project.isOpen()) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(null, "Error", "Project " + ((project != null) ? project.getName() : "" )+" is closed or doesn't exists");
					mon.done();
				}
			});
			return;
		}

		ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType("org.eclipse.ui.externaltools.ProgramLaunchConfigurationType");
		
		final ILaunchConfigurationWorkingCopy workingCopy =
		      type.newInstance( null, "TiShadow Spec");
		
		final Map<String, String> envVars = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap<String, String>());
		final String location = configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, "");
		final boolean showConsole = configuration.getAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, false);
		final String toolArguments = configuration.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "");
		
		workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION, location);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, showConsole);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, toolArguments);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, projectLoc);
		workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, envVars);
		
		final boolean created_tiapp = createTiApp(project);
		workingCopy.launch(mode, mon);

		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
			@Override
			public void handleDebugEvents(DebugEvent[] events) {
				
				if (events.length > 0 && (events[0].getKind() == DebugEvent.TERMINATE)) {
					DebugPlugin.getDefault().removeDebugEventListener(this);
					
					final IFolder folder = getTiShadowResultFolder(projectLoc);
					
					final ArrayList<IPath> junitXMLResources = getXmlResults(mon, folder);
					
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								workingCopy.delete();
							} catch (CoreException e) {
								e.printStackTrace();
							}
							
							if (created_tiapp) {
								deleteTiApp(project);
							}
							
							if (junitXMLResources.isEmpty()) {
								MessageDialog.openError(null, "Error", "Cannot find Junit XML results for TiShadow run.");
								return;
							}
							
							String tishadowDirectory = folder.getProject().getLocation().toOSString() + "/build/tishadow";
							JUnitViewEditorLauncher junit = new JUnitViewEditorLauncher();
							refreshProject(project);
							mergeXMLFiles(junitXMLResources, tishadowDirectory+"/fullTestSuite.xml");
							refreshProject(project);
							junit.open(new Path(tishadowDirectory+"/fullTestSuite.xml"));
							mon.done();
						}
					});
				}
			}
		});
	}
	
	private boolean createTiApp(IProject project) {
		IFile file = project.getFile("tiapp.xml");

		if(!file.exists()) {
			try {
				String pathPropertiesFile = "tiapp.xml"; 
				InputStream source = this.getClass().getResourceAsStream(pathPropertiesFile);
				file.create(source, false, null);
			} catch (Exception ex) {
		       return false;
		    }
		}
		
		return true;
	}

	private void deleteTiApp(IProject project) {
		IFile file = project.getFile("tiapp.xml");
		try {
			if(file.exists()) {
				file.delete(true, null);
			}
		} catch (Exception ex) { }
	}

	private void refreshProject (IProject project) {
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private String mergeXMLFiles(ArrayList<IPath> junitXMLResources, String mergedFileName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
	
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Document mergedDocument = builder.newDocument();
		mergedDocument.appendChild(mergedDocument.createElement("testsuites"));
		
		for (IPath junitXMLResource : junitXMLResources) {
			try {
		        Document document = builder.parse(new File(junitXMLResource.toString()));
		        
		        Node testSuitesElement = mergedDocument.getElementsByTagName("testsuites").item(0);
		        
		        NodeList testSuiteElements = document.getElementsByTagName("testsuite");
		        
		        String fileName = junitXMLResource.toString();
		        
		        fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
		        
		        String[] xmlName = fileName.split("_");
		        
		        String deviceName = "";
		        
		        // Remove the ip address from the file name
		        for (int i = 0; i < xmlName.length - 5; i++) {
		        	if (i+1 == 1) {
		        		deviceName += " ";
		        	}
		        	
		        	deviceName += xmlName[i];
		        	
		        	if (StringUtils.isNumeric(xmlName[i]) && i+1 != (xmlName.length - 5)) {
		        		deviceName += ".";
		        	}
		        }
		        
		        for(int i = 0; i < testSuiteElements.getLength(); i++) {
		        	Node importedNode = mergedDocument.importNode(testSuiteElements.item(i), true);
		        	String namePreviousValue = ((Element)importedNode).getAttribute("name");
		        	((Element)importedNode).setAttribute("name", deviceName + " - " + namePreviousValue);
		        	testSuitesElement.appendChild(importedNode);
		        }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	     // Use a Transformer for output
        TransformerFactory tFactory =
        TransformerFactory.newInstance();
        Transformer transformer;
		try {
			transformer = tFactory.newTransformer();
			
			DOMSource source = new DOMSource(mergedDocument);
	        StreamResult result = new StreamResult(mergedFileName);
	        transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	private IFolder getTiShadowResultFolder(final String projectLoc) {
		IProject project = getProject(projectLoc);
		IFolder folder = project.getFolder(Path.fromOSString("build/tishadow/"));
		return folder;
	}
	
	private IProject getProject(final String projectLoc) {
		try {
			String projectName = Path.fromOSString(projectLoc).lastSegment();
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			return project;
		} catch (Exception e) {
			return null;
		}
	}
	
	private ArrayList<IPath> getXmlResults(final IProgressMonitor monitor, IFolder folder) {
		ArrayList<IPath> jUnitResources = new ArrayList<IPath>();
		if (folder.exists()) {
			try {
				folder.refreshLocal(1, monitor);
				IResource[] members;
				members = folder.members();
				for (IResource iResource : members) {
					if (iResource.getFullPath().toPortableString().contains("_result.xml")) {
						jUnitResources.add(iResource.getLocation());
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return jUnitResources;
	}
	
	public static String getLaunchDir(IProject project) {
		return project.getLocation().toPortableString();
	}

	public static void setLaunchAttributes(ILaunchConfigurationWorkingCopy configuration, IResource context) throws CoreException {
		configuration.setAttribute(IExternalToolConstants.ATTR_LOCATION,
				"/usr/local/bin/tishadow");
		String project = null;
		if (context != null) {
			project = LaunchTiShadowTests.getLaunchDir(context.getProject());
		}
		configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, project);
		configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "spec -u -x -p 8181");
		configuration.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, true);
		configuration.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, LaunchUtils.getEnvVars());
	}
}
