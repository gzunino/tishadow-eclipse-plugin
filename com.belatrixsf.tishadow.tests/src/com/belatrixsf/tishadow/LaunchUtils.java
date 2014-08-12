package com.belatrixsf.tishadow;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
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
import com.belatrixsf.tishadow.tests.LaunchShortcut;

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
	
	public static boolean hasAppified(IProject selectedProject) {
		
		//boolean indicating if there are more than one appified projects for the selected project;
//		boolean moreThanOneAppified = false;
		
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		IProject appifiedProject = null;
//		int appifiedCount = 0;
		String baseProjectName = null;
		
		if (isTiModule(selectedProject)) {
			baseProjectName = getBaseProjectName(selectedProject, projects);
		} else {
			baseProjectName = selectedProject.getName();
		}
		
		try {
			for (IProject project : projects) {
				if ((project.getDescription().getReferencedProjects() != null) && (project.getDescription().getReferencedProjects().length > 0)) {
					if (baseProjectName == project.getDescription().getReferencedProjects()[0].getName()) {
						if (isTiApp(project)) {
//							appifiedCount+=1;
							appifiedProject = project;
						}
					}
				}
			}
		} catch (CoreException e1) {
				e1.printStackTrace();
		}
		
//		if (appifiedCount > 1) {
//			moreThanOneAppified = true;
//		}
//		
//		if (moreThanOneAppified) {
//			Shell dialogShell = Display.getCurrent().getActiveShell();
//			ElementTreeSelectionDialog dialog = null; //createProjectsDialog(dialogShell);
//			if(dialog.getReturnCode() == 0){ //0 is OK
////				launchSelectedProject(dialog);
////				finish(TiLaunchShortcuts.LaunchShortcutContributionItem.this.extension);
//			} else {
////				finish(TiLaunchShortcuts.LaunchShortcutContributionItem.this.extension);
//			}
//		} else {
//			if (appifiedProject != null) {
//				ISelection fSelection = new StructuredSelection(appifiedProject);
////				TiLaunchShortcuts.LaunchShortcutContributionItem.this.extension.launch(fSelection, ILaunchManager.RUN_MODE);
//			} else {
//				if (isTiModule(selectedProject)) {
//					Display.getDefault().syncExec(new Runnable() {
//						@Override
//						public void run() {
//							MessageDialog.openError(null, "No TiShadow appified projects found", "To run tests on a Titanium module, you need a Tishadow app to run on.");
//						}
//					});
//				} else {
////					appifyProjectDialog();
//				}
//			}
////			finish(TiLaunchShortcuts.LaunchShortcutContributionItem.this.extension);
//		}
		
		if (appifiedProject != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isTiModule(IProject project) {
    	IFile timodule = project.getFile("timodule.xml");
        if (timodule.exists()) {
        	return true;
    	}
        return false;
    }
	
	public static boolean isTiApp(IProject project) {
    	IFile tiapp = project.getFile("tiapp.xml");
        if (tiapp.exists()) {
        	return true;
    	}
        return false;
    }
	
	public static String getBaseProjectName(IProject selectedProject, IProject[] projects) {
		LaunchShortcut ls = new LaunchShortcut();
		ILaunchConfiguration lc = ls.getExistingLaunch(selectedProject);
		String arguments = null;
		try {
			arguments = lc.getAttribute(Constants.TISHADOW_TOOL_ARGUMENTS, "");
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		String baseProjectName = "";
		for(IProject project : projects) {
			if (arguments.contains(project.getName())) {
				baseProjectName = project.getName();
			}
		}
		return baseProjectName;
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
