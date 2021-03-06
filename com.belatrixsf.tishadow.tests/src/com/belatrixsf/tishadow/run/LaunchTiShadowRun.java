package com.belatrixsf.tishadow.run;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.belatrixsf.tishadow.LaunchUtils;
import com.belatrixsf.tishadow.preferences.page.PreferenceValues;
import com.belatrixsf.tishadow.tests.LaunchTiShadowTests;

@SuppressWarnings("restriction")
public class LaunchTiShadowRun implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		final SubMonitor mon = SubMonitor.convert(monitor);
		mon.beginTask("Running TiShadow", IProgressMonitor.UNKNOWN);

		final String projectLoc = configuration.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, "");

		final Map<String, String> envVars = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap<String, String>());
		final String location = configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, "");
		final boolean showConsole = configuration.getAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, false);
		String toolArguments = configuration.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "");

		ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType("org.eclipse.ui.externaltools.ProgramLaunchConfigurationType");
		final ILaunchConfigurationWorkingCopy workingCopy = type.newInstance( null, "TiShadow Run");
		workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION, location);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, showConsole);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, toolArguments);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, projectLoc);
		workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, envVars);
		
		Job job = null;
		try {
			job = Job.getJobManager().currentJob();
			workingCopy.launch(mode, mon);
			monitor.subTask("Running Deploy...");
		} catch (Exception e) {
			showAsyncErrorMsg(
					"Error executing TiShadow Command",
					"There was a problem while trying to run TiShadow. \nPlease check your TiShadow command path on Window>Preferences>Tishadow");
			e.printStackTrace();
		} finally {
			if (job != null) {
				job.done(monitor.isCanceled() ? Status.CANCEL_STATUS
						: Status.OK_STATUS);
			}
			if (monitor.isCanceled() && launch.canTerminate() || job == null) {
				launch.terminate();
			}
		}
		mon.done();
	}

	private void showAsyncErrorMsg(final String title, final String msg) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Display display = Display.getDefault();
				Shell shell = display.getActiveShell();
				MessageDialog.openError(shell, title,msg);
			}
		});
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