package com.belatrixsf.tishadow.app.wizards;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.belatrixsf.tishadow.LaunchUtils;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "mpe". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class TiShadowAppWizard extends BasicNewProjectResourceWizard implements INewWizard {
	
	@Override
	public void addPages() {
		super.addPages();
		
		((WizardNewProjectCreationPage) getPage("basicNewProjectPage")).setInitialProjectName("tishadowapp");
	}
	
	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public boolean performFinish() {
		boolean finished = super.performFinish();
		
		IProject project = getNewProject();
		if (project != null) {
			createTiProject(project);
		}
		
		return finished;
	}

	private void createTiProject(final IProject project) {
		try {
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType("org.eclipse.ui.externaltools.ProgramLaunchConfigurationType");
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, launchManager.generateLaunchConfigurationName("tishadow app"));
			
			workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION, "/usr/local/bin/tishadow");
			workingCopy.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, true);
			workingCopy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "app -d "+project.getLocation().toOSString());
			workingCopy.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, project.getParent().getLocation().toOSString());
			workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, LaunchUtils.getEnvVars());

			ILaunch launch = workingCopy.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
			try {
				launch.getProcesses()[0].getStreamsProxy().write("com.test.app");

				DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
					@Override
					public void handleDebugEvents(DebugEvent[] events) {
						
						if (events.length > 0 && (events[0].getKind() == DebugEvent.TERMINATE)) {
							DebugPlugin.getDefault().removeDebugEventListener(this);
							try {
								project.refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());
							} catch (CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static Map<String, String> getEnvVars() {
		// Get current value for PATH environment variable
		String pathVariable = System.getenv("PATH");
		pathVariable += ":/usr/local/bin";
		
		Map<String, String> envVariables = new HashMap<String, String>();
		envVariables.put("PATH", pathVariable);
		return envVariables;
	}
}