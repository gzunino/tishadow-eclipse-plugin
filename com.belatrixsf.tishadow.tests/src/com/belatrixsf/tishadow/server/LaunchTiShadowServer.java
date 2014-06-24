package com.belatrixsf.tishadow.server;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;

import com.belatrixsf.tishadow.LaunchUtils;
import com.belatrixsf.tishadow.preferences.page.PreferenceValues;
import com.belatrixsf.tishadow.runner.Constants;
import com.belatrixsf.tishadow.runner.TiShadowRunner;

@SuppressWarnings("restriction")
public class LaunchTiShadowServer implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if(LaunchUtils.isServerLaunched()) {
			MessageDialog.openError(null, "Error", "An instance of the server is already running.");
			return;
		}

		final SubMonitor mon = SubMonitor.convert(monitor);
		mon.beginTask("Starting server", IProgressMonitor.UNKNOWN);

		final String projectLoc = configuration.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, "");

		final Map<String, String> envVars = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap<String, String>());
		final String location = configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, "");
		final boolean showConsole = configuration.getAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, false);
		String toolArguments = configuration.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "");

		ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType("org.eclipse.ui.externaltools.ProgramLaunchConfigurationType");
		final ILaunchConfigurationWorkingCopy workingCopy = type.newInstance( null, "TiShadow Server");
		workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION, location);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, showConsole);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, toolArguments);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, projectLoc);
		workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, envVars);
		workingCopy.launch(mode, mon);
		
		mon.done();
	}

	public static String getLaunchDir(IProject project) {
		return project.getLocation().toPortableString();
	}

	public static void setLaunchAttributes(ILaunchConfigurationWorkingCopy configuration, IResource context) throws CoreException {
		configuration.setAttribute(IExternalToolConstants.ATTR_LOCATION, PreferenceValues.getTishadowDirectory());

		String project = null;
		if (context != null) {
			project = LaunchTiShadowServer.getLaunchDir(context.getProject());
		}
		configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, project);
		configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "server");
		configuration.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, true);
		configuration.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, LaunchUtils.getEnvVars());
	}
}