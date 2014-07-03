package com.belatrixsf.tishadow;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.dialogs.MessageDialog;

import com.belatrixsf.tishadow.preferences.page.PreferenceValues;
import com.belatrixsf.tishadow.runner.Constants;

public class LaunchUtils {
	
	static Boolean isServerLaunched = null;
	
	public static Map<String, String> getEnvVars() {
		// Get current value for PATH environment variable
		String pathVariable = System.getenv("PATH");
		pathVariable += ":/usr/local/bin";
		
		Map<String, String> envVariables = new HashMap<String, String>();
		envVariables.put("PATH", pathVariable);
		return envVariables;
	}
	
	public static void handleError(String msg, Exception e) {
		MessageDialog.openError(null, msg, e.toString() + "\n" + e.getLocalizedMessage().toString());
		e.printStackTrace();
	}

	public static IProject getProject(final String projectLoc) {
		try {
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

			for (IProject project : projects) {
				if(projectLoc.equals(project.getLocation().toString())) {
					return project;
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isServerLaunched(boolean forceSocketTest) {
		if (forceSocketTest || isServerLaunched == null) {
			testSocket();
		}
		return isServerLaunched;
	}

	protected static void testSocket() {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(PreferenceValues.getTishadowHost(), PreferenceValues.getTishadowPort()), 500);
			socket.close();
			isServerLaunched = true;
		} catch (Exception e) {
			isServerLaunched = false;
		}
	}
	
	public static void stopTiShadowServer() throws CoreException, DebugException {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (ILaunch iLaunch : launches) {
			if (iLaunch.canTerminate() 
					&& "com.belatrixsf.tishadow.server.launchTiShadowServer".equals(iLaunch.getLaunchConfiguration().getType().getIdentifier())
					|| "org.eclipse.ui.externaltools.ProgramLaunchConfigurationType".equals(iLaunch.getLaunchConfiguration().getType().getIdentifier())
							&& iLaunch.getLaunchConfiguration().getAttribute(Constants.TISHADOW_LOCATION, "").contains("tishadow"))
			{
				iLaunch.terminate();
			}
		}
	}
}
