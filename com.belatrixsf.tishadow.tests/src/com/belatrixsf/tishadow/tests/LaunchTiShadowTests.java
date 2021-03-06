package com.belatrixsf.tishadow.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.belatrixsf.tishadow.LaunchUtils;
import com.belatrixsf.tishadow.app.wizards.AppifyTiShadowWizard;
import com.belatrixsf.tishadow.common.TiLaunchShortcuts;
import com.belatrixsf.tishadow.handlers.RunServer;
import com.belatrixsf.tishadow.preferences.page.PreferenceValues;

@SuppressWarnings("restriction")
public class LaunchTiShadowTests implements ILaunchConfigurationDelegate {

	private ILaunchConfiguration previousTestConfig = null;
	private static final String RESULT_XML = "_result.xml";
	boolean created_tiapp = false;

	@Override
	public void launch(ILaunchConfiguration configuration, final String mode,
			ILaunch launch, final IProgressMonitor monitor)
			throws CoreException {
		monitor.beginTask("Running Tests", 1500);
		showWizard();

		final String projectLoc = configuration.getAttribute(
				IExternalToolConstants.ATTR_WORKING_DIRECTORY, "");
		final IProject project = LaunchUtils.getProject(projectLoc);

		if (project == null || !project.isOpen()) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					Display display = Display.getDefault();
					Shell shell = display.getActiveShell();
					MessageDialog.openError(shell, "Error", "Project "
							+ ((project != null) ? project.getName() : "")
							+ " is closed or doesn't exist");
				}
			});
			monitor.done();
			return;
		}

		ILaunchConfigurationType type = DebugPlugin
				.getDefault()
				.getLaunchManager()
				.getLaunchConfigurationType(
						"org.eclipse.ui.externaltools.ProgramLaunchConfigurationType");

		final ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(
				null, "TiShadow Spec");
		@SuppressWarnings("unchecked")
		final Map<String, String> envVars = configuration.getAttribute(
				ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
				new HashMap<String, String>());
		final String location = configuration.getAttribute(
				IExternalToolConstants.ATTR_LOCATION, "");
		final boolean showConsole = configuration.getAttribute(
				IExternalToolConstants.ATTR_SHOW_CONSOLE, false);
		final String toolArguments = configuration.getAttribute(
				IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "");

		workingCopy
				.setAttribute(IExternalToolConstants.ATTR_LOCATION, location);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE,
				showConsole);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS,
				toolArguments);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY,
				projectLoc);
		workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
				envVars);

		try {

			ILaunchConfiguration newConfiguration = configuration;

			final boolean spec_touch = touchSpecFiles(configuration, project,
					monitor);

			launchTests(mode, monitor, projectLoc, project, workingCopy,
					spec_touch, newConfiguration);

		} catch (final Exception ex) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					Display display = Display.getDefault();
					Shell shell = display.getActiveShell();
					MessageDialog.openError(shell, "Error", ex.getMessage());
					ex.printStackTrace();
				}
			});
		}
	}

	protected void launchTests(String mode,
			final IProgressMonitor monitor, final String projectLoc, final IProject project,
			final ILaunchConfigurationWorkingCopy workingCopy,
			final boolean spec_touch, final ILaunchConfiguration configuration) throws CoreException {
		if(spec_touch) {
			if(LaunchUtils.isServerLaunched(true)) {
				if(LaunchUtils.hasAppified(project)) {
					if(LaunchUtils.isADeviceConnected()){
						
						final IFolder folder = getTiShadowResultFolder(projectLoc);
						removeOldResults(monitor, folder);
						
						if(configuration != previousTestConfig){
							String toolArguments = configuration.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "");
							workingCopy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, toolArguments.replace("-u ", ""));
						}
						
						ILaunch launch = workingCopy.launch(mode, monitor);
						
						monitor.subTask("Running tests...");
						
						final Job job = Job.getJobManager().currentJob();
						final AtomicBoolean finished = new AtomicBoolean(false);
						
						// wait for termination and show results
						DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
							@Override
							public void handleDebugEvents(DebugEvent[] events) {
								if (events.length > 0 && (events[0].getKind() == DebugEvent.TERMINATE)) {
									DebugPlugin.getDefault().removeDebugEventListener(this);
		
									Display.getDefault().asyncExec(new Runnable() {
										@Override
										public void run() {
											try {
												workingCopy.delete();
											} catch (CoreException e) {
												e.printStackTrace();
											}
											
											previousTestConfig = configuration;
		
											LaunchUtils launchUtils = new LaunchUtils();
											launchUtils.setLaunchConfiguration(configuration);
		
											final ArrayList<IPath> junitXMLResources = getXmlResults(monitor, folder);
											if (junitXMLResources.isEmpty()) {
												showError("Error", "Cannot find JUnit XML results for TiShadow run. Check the console logs.");
												return;
											}
		
											JUnitViewEditorLauncher junit = new JUnitViewEditorLauncher();
											String mergedXml = folder.getLocation().toOSString() + "/fullTestSuite.xml";
											mergeXMLFiles(junitXMLResources, mergedXml);
											refreshProject(project);
											junit.open(new Path(mergedXml));
											monitor.done();
											
											finished.set(true);
										}
									});
								}
							}
							
						});
						
						while (!finished.get() && !monitor.isCanceled()) {
							Random r = new Random();
							monitor.worked((int) (Math.max(1, Math.min(10, (int) 2 + r.nextGaussian() * 20))));
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if (job != null) {
							job.done(monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS);
						}
						if (monitor.isCanceled() && launch.canTerminate()) {
							launch.terminate();
						}
					} else {
						LaunchUtils launchUtils = new LaunchUtils();
						launchUtils.setLaunchConfiguration(previousTestConfig);
						
						showErrorWithLaunchShortcuts("Error", "There are no TiShadow apps running on a device and connected to the server.\nYou can select a device from the list below to run the app.");
					}
				}else{
					LaunchUtils launchUtils = new LaunchUtils();
					launchUtils.setLaunchConfiguration(previousTestConfig);
					
					appifyIfNotTiModule(project);
					
				}
			} else {

				LaunchUtils launchUtils = new LaunchUtils();
				launchUtils.setLaunchConfiguration(previousTestConfig);
				
				showErrorWithOptions("Error", "TiShadow Server is not running. Would you like to start it?");
			}
		}
		return;
	}

	protected void appifyIfNotTiModule(final IProject project) {
		if (LaunchUtils.isTiModule(project)) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					Display display = Display.getDefault();
					Shell shell = display.getActiveShell();
					MessageDialog
							.openError(shell, "Error",
									"There are no appified versions of the base project.");
				}
			});
		} else {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					Display display = Display.getDefault();
					Shell shell = display.getActiveShell();
					boolean result = MessageDialog
							.openQuestion(shell, "Error",
									"There are no appified versions of the project.\nWould you like to appify it?");
					if (result) {
						AppifyTiShadowWizard wizard = new AppifyTiShadowWizard();
						WizardDialog w = new WizardDialog(shell, wizard);
						w.open();
					}
				}
			});
		}
		final Job job = Job.getJobManager().currentJob();
		if (job != null) {
			job.done(Status.OK_STATUS);
		}
	}

	protected void showErrorWithOptions(final String title, final String message) {
		final Job job = Job.getJobManager().currentJob();
		if (job != null) {
			job.done(Status.OK_STATUS);
		}
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Display display = Display.getCurrent();
				Shell shell = display.getActiveShell();
				boolean result = MessageDialog.openQuestion(shell, title, message);
				if (result && message.contains("Server is not running")) {
					RunServer runServer = new RunServer();
					try {
						runServer.startTiShadowServerUpdateToolbar();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
		});
	}
	
	protected void showErrorWithLaunchShortcuts(final String title, final String message) {
		
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog md = new MessageDialog(null, title, null, message, MessageDialog.ERROR, new String[] {"Cancel"}, 0) {
					@Override
					protected void createButtonsForButtonBar(Composite parent) {
						createDropdown(parent);
						super.createButtonsForButtonBar(parent);
					}
				};			
				md.open();
			}
		});
		final Job job = Job.getJobManager().currentJob();
		if (job != null) {
			job.done(Status.OK_STATUS);
		}
	}
	
	private ToolBar createDropdown(Composite parent) {
		((GridLayout) parent.getLayout()).numColumns++;
        ToolBar toolbar = new ToolBar(parent, SWT.NONE);
        
        TiLaunchShortcuts tils = new TiLaunchShortcuts();
        tils.createControl(toolbar);
        
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		Point minSize = toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = minSize.x;
		toolbar.setLayoutData(data);
		
        return toolbar;
    }
	
	private void removeOldResults(final IProgressMonitor monitor,final IFolder folder) throws CoreException {
		if (!folder.exists()){
			return;
		}
		IResource[] members = folder.members();
		try {
			for (IResource iResource : members) {
				if (iResource.getFullPath().toPortableString().contains(RESULT_XML)) {
					iResource.delete(true, monitor);
				}
			}
		}
		catch (CoreException e) {
		}
	}

	private void showWizard() {
		String hideWizard = Activator.getDefault().getPreferenceStore()
				.getString("tishadow.hideWizard");

		if (!MessageDialogWithToggle.ALWAYS.equals(hideWizard)) {
			final String instructionsMessage = "1 - Make sure the tishadow server is running. You can start it using the Run Tishadow server option in the context menu.\n 2 - Open the tishadow application on the devices you want to use to run the tests and connect them to the server.\n 3 - Once this is done the tests will run properly.\n";

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					Display display = Display.getDefault();
					Shell shell = display.getActiveShell();
					MessageDialogWithToggle.openInformation(shell,
							"TiShadow Wizard", instructionsMessage,
							"Do not show this wizard again", true, Activator
									.getDefault().getPreferenceStore(),
							"tishadow.hideWizard");
				}
			});
		}
	}

	private boolean touchSpecFiles(ILaunchConfiguration configuration, IProject project,IProgressMonitor monitor) {
		try {
			IFolder folder = project.getFolder(Path.fromOSString("spec/"));
			if(folder.exists()) {
				IResource[] resources = folder.members();
				boolean enter  = false;
				for(IResource resource : resources) {
					if(resource instanceof IFile) {
						folder.getFile(resource.getName().toString()).setLocalTimeStamp(System.currentTimeMillis());
						enter = true;
						break;
					}
				}
				return enter;
			} else {
				
				LaunchUtils launchUtils = new LaunchUtils();
				launchUtils.setLaunchConfiguration(previousTestConfig);
				
				showError("Error", "Project not testeable, 'spec' folder doesn't exist.");
				
				try {
					configuration.delete();
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return false;
			}
		}
		catch (Exception ex) {
			
			LaunchUtils launchUtils = new LaunchUtils();
			launchUtils.setLaunchConfiguration(previousTestConfig);
			
			showError("Error", "Spec folder error");
			
			ex.printStackTrace();
			return false;
		}
	}

	protected void showError(final String title, final String message) {
		final Job job = Job.getJobManager().currentJob();
		if (job != null) {
			job.done(Status.OK_STATUS);
		}
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(null, title, message);
			}
		});
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
			} catch (final Exception e) {

				LaunchUtils launchUtils = new LaunchUtils();
				launchUtils.setLaunchConfiguration(previousTestConfig);
				
				showError("Error merging results", e.toString() + "\n" + e.getLocalizedMessage().toString());
				
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
		} catch (final Exception e) {
			
			LaunchUtils launchUtils = new LaunchUtils();
			launchUtils.setLaunchConfiguration(previousTestConfig);
			
			LaunchUtils.handleError("Error merging results", e);
			showError("Error merging results", e.toString() + "\n" + e.getLocalizedMessage().toString());
			
			e.printStackTrace();
		}
		
		return "";
	}

	private IFolder getTiShadowResultFolder(final String projectLoc) {
		IProject project = LaunchUtils.getProject(projectLoc);
		IFolder folder = project.getFolder("build");
		try {
			if (!folder.exists()) {
				folder.create(true, true, new NullProgressMonitor());
			}
			folder = folder.getFolder("tishadow");			
			deleteXMLFiles(folder);
			
		} catch (CoreException e) {
		}
		return folder;
	}
	
	private boolean deleteXMLFiles(IFolder folder) throws CoreException {
		if (!folder.exists()) {
			return true;
		}
		// Lists all files in folder
		IResource fList[] = folder.members();
		boolean success = false;
		// Searchs .xml
		for (int i = 0; i < fList.length; i++) {
			IResource file = fList[i];
		    if (file.getName().endsWith(".xml")) {
		        // and deletes
		        success = (new File(fList[i].getName()).delete());
		    }
		}
		return success;
		
	}

	private ArrayList<IPath> getXmlResults(final IProgressMonitor monitor, IFolder folder) {
		ArrayList<IPath> jUnitResources = new ArrayList<IPath>();
		if (folder.exists()) {
			try {
				folder.refreshLocal(IFolder.DEPTH_INFINITE, monitor);
				IResource[] members;
				members = folder.members();
				for (IResource iResource : members) {
					if (iResource.getFullPath().toPortableString().contains(RESULT_XML)) {
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

	public static void setLaunchAttributesWithArguments(ILaunchConfigurationWorkingCopy configuration, IResource context, String arguments)  throws CoreException {
		configuration.setAttribute(IExternalToolConstants.ATTR_LOCATION,PreferenceValues.getTishadowDirectory());
		String project = null;
		if (context != null) {
			project = LaunchTiShadowTests.getLaunchDir(context.getProject());
		}
		configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, project);
		configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, arguments);
		configuration.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, true);
		configuration.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, LaunchUtils.getEnvVars());
	}
}
