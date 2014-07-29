package com.belatrixsf.tishadow;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.belatrixsf.tishadow.preferences.page.PreferenceValues;
import com.belatrixsf.tishadow.runner.Constants;
import com.belatrixsf.tishadow.server.TiShadowSocketClient;

public class LaunchUtils {
	
	static Boolean isServerLaunched = null;
    static ArrayList<ILaunchConfiguration> launchConfigurations = new ArrayList<ILaunchConfiguration>();
	
	public static Map<String, String> getEnvVars() {
		// Get current value for PATH environment variable
		String pathVariable = System.getenv("PATH");
		pathVariable += ":/usr/local/bin";
		
		Map<String, String> envVariables = new HashMap<String, String>();
		envVariables.put("PATH", pathVariable);
		return envVariables;
	}
	
	public static void handleError(final String msg, final Exception e) {
		e.printStackTrace();
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(null, msg, e.toString() + "\n" + e.getLocalizedMessage().toString());
			}
		});
		
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
	
	public static boolean isADeviceConnected() {
		
		TiShadowSocketClient socket = new TiShadowSocketClient();
		return socket.isADeviceConnected();
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
	
	public void setLaunchConfiguration(ILaunchConfiguration lConfig){
		if (launchConfigurations.contains(lConfig)){
			launchConfigurations.remove(lConfig);
			launchConfigurations.add(lConfig);
		} else {
			launchConfigurations.add(lConfig);
		}
	}
	
	public ILaunchConfiguration getLaunchConfiguration(){
		if(launchConfigurations.isEmpty()){
			return null;
		} else {
			return launchConfigurations.get(launchConfigurations.size()-1);
		}
	}
	
	public ArrayList<ILaunchConfiguration> getLaunchConfigurations(){
		return launchConfigurations;
	}
}
