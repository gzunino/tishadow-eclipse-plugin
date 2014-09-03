package com.belatrixsf.tishadow.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.belatrixsf.tishadow.run.LaunchRunShortcut;
import com.belatrixsf.tishadow.runner.IRunnerCallback;
import com.belatrixsf.tishadow.tests.LaunchTestsShortcut;

/**
 *  This class consists on the command parameter or the contextual selection.
 * */
public class TiShadowHandler extends AbstractHandler implements IRunnerCallback {

	public static final String CONFIGURATION_PARAMETER = "com.belatrixsf.tishadow.config";
	public static final String TEST_LAUNCH_CONFIGURATION_PARAMETER = "com.belatrixsf.tishadow.tests.launchTiShadowTests";
	public static final String DEPLOY_LAUNCH_CONFIGURATION_PARAMETER = "com.belatrixsf.tishadow.run.launchTiShadowRun";
	public static final String RUN_TYPE_PARAMETER = "com.belatrixsf.tishadow.runType";
	public static final String DEPLOY = "deploy";
	public static final String TEST = "test";
	private static String runTypeParameter;
	 
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(event.getParameter(CONFIGURATION_PARAMETER) != null){
			final ILaunchConfiguration launchConfiguration = getLaunchConfiguration(event.getParameter(CONFIGURATION_PARAMETER), event.getParameter(RUN_TYPE_PARAMETER));
			
				System.out.println("Config executed >> " + launchConfiguration.getName());
			
	            Job job = new Job("TiShadow") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						this.setThread(Thread.currentThread());
						try {
							launchConfiguration.launch("run", monitor);
						} catch (CoreException e) {
							e.printStackTrace();
						}
						return ASYNC_FINISH;
					}
				};
				job.setUser(true);
				job.schedule();
		} else {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			
			runTypeParameter = event.getParameter(RUN_TYPE_PARAMETER);
			if (runTypeParameter.equals(DEPLOY)) {
				LaunchRunShortcut shortcut = new LaunchRunShortcut();
				shortcut.launch(selection, "run");
			}
			if (runTypeParameter.equals(TEST)) {
				LaunchTestsShortcut shortcut = new LaunchTestsShortcut();
				shortcut.launch(selection, "run");
			}
		}

		return null;
	}
	
	private ILaunchConfiguration getLaunchConfiguration(String runConfigName, String runTypeParameter){
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = null;
		
		if (runTypeParameter.equals(DEPLOY)) {
			type = launchManager.getLaunchConfigurationType(DEPLOY_LAUNCH_CONFIGURATION_PARAMETER);
		}
        ILaunchConfiguration[] launchs = null;
		if (runTypeParameter.equals(TEST)) {
			 type = launchManager.getLaunchConfigurationType(TEST_LAUNCH_CONFIGURATION_PARAMETER);
		}
		
        try {
        	launchs = launchManager.getLaunchConfigurations(type);
		} catch (CoreException e) {
			e.printStackTrace();
		}			
		for(int i = 0 ; i < launchs.length ; i++){
			if(launchs[i].getName().equals(runConfigName)){
				return launchs[i];
			}
		}
		return null;
	}

	@Override
	public void onRunnerTishadowFinish(Object response) {
		// TODO Auto-generated method stub
		
	}
	
}
