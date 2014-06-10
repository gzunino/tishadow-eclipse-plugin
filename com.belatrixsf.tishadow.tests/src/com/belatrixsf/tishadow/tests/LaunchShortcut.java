package com.belatrixsf.tishadow.tests;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

@SuppressWarnings("restriction")
public class LaunchShortcut implements ILaunchShortcut {
	
	private ArrayList<String> allowedProjectsList;

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (element instanceof IResource) {
				IProject project = ((IResource) element).getProject();
				searchAndLaunch(project, mode);
			}
		} 
	}

	private void searchAndLaunch(IProject project, String mode) {
		ILaunchConfiguration launch = getExistingLaunch(project);
		if (launch != null) {
			try {
				launch.launch(mode, new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private ILaunchConfiguration getExistingLaunch(IProject project) {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = launchManager.getLaunchConfigurationType("com.belatrixsf.tishadow.tests.launchTiShadowTests");
		ILaunchConfiguration[] launchs;
		String selectedTarget = detectTiModule(project);
		try {
			launchs = launchManager.getLaunchConfigurations(type);
			for (ILaunchConfiguration iLaunchConfiguration : launchs) {
				try {
					if ( isSameLaunch(project, iLaunchConfiguration)) {
						return iLaunchConfiguration;
					}
				} catch (CoreException e) {
				}
			}
		}
		catch (CoreException e1) {
		}

		try {
			ILaunchConfigurationWorkingCopy launch = type.newInstance(null, launchManager.generateLaunchConfigurationName(project.getName()));
			LaunchTiShadowTests.setLaunchAttributes(launch, project, selectedTarget);
			return launch.doSave();
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Detects if the selected project is a Titanium Module
	 * and appends the possible appifyied projects to run on.
	 * @param project
	 * @return
	 */
	private String detectTiModule(IProject project) {
		
		ArrayList<String> appifyedProjectsList = getTiShadowProjectsList();
		displayProjectsList(appifyedProjectsList);
		String appifyedProjectName="MB-Next-Gen-Phone";
		
		return "-T " + appifyedProjectName;
	}
	
	/**
	 * Display the list of appifyed projects to select.
	 * @param projectsList
	 */
	private void displayProjectsList(ArrayList<String> projectsList) {
		Shell shell = new Shell (Display.getCurrent());
		final List list = new List(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		for (int i=0; i<projectsList.size(); i++) list.add (projectsList.get(i));
		org.eclipse.swt.graphics.Rectangle clientArea = shell.getClientArea ();
		list.setBounds (clientArea.x, clientArea.y, 200, 200);
		list.addListener (SWT.Selection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				String string = "";
				int [] selection = list.getSelectionIndices ();
				for (int i=0; i<selection.length; i++) string += selection [i] + " ";
				System.out.println ("Selection={" + string + "}");
			}
		});
		list.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				String string = "";
				int [] selection = list.getSelectionIndices ();
				for (int i=0; i<selection.length; i++) string += selection [i] + " ";
				System.out.println ("DefaultSelection={" + string + "}");
			}
		});
		shell.pack ();
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!Display.getCurrent().readAndDispatch ()) Display.getCurrent().sleep ();
		}
		Display.getCurrent().dispose ();
	}

	/**
	 * Gets all the project names projects that contains appname:TiShadow.
	 * @throws IOException 
	 */
	public ArrayList<String> getTiShadowProjectsList(){
		String projectName;
		setAllowedProjectsList(new ArrayList<String>());
		
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		
		System.out.println(projects.length);
		
		for (IProject project : projects) {
			projectName = project.getName();
			
			System.out.println(projectName + ":::::::::::::::::::");
		
			IFile manifest = project.getFile("manifest");
			
			if (manifest != null) {
				BufferedReader in = null;
				try {
					in = new BufferedReader(new InputStreamReader(manifest.getContents()));
				} catch (CoreException e) {
					e.printStackTrace();
				}

				String inputLine;
				
				try {
					while ((inputLine = in.readLine()) != null) {
						System.out.println(inputLine.toString());
						if (inputLine.contains("appname:TiShadow")){
							System.out.println("!!!!!!!!!!!!!!!!!!!!!");
							getAllowedProjectsList().add(projectName);
							break;
						}
					}
				} catch (IOException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return getAllowedProjectsList();
	}
	

	public ArrayList<String> getAllowedProjectsList() {
		return allowedProjectsList;
	}

	public void setAllowedProjectsList(ArrayList<String> allowedProjectsList) {
		this.allowedProjectsList = allowedProjectsList;
	}
	

	private boolean isSameLaunch(IProject project, ILaunchConfiguration iLaunchConfiguration) throws CoreException {
		return iLaunchConfiguration.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, "").equals(LaunchTiShadowTests.getLaunchDir(project));
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		if (editor.getClass().getSimpleName().contains(("JSSourceEditor"))) {
	        IEditorInput input = editor.getEditorInput();
	        IProject project = ((IFileEditorInput) input).getFile().getProject();
			searchAndLaunch(project, mode);
        } 
	}
}