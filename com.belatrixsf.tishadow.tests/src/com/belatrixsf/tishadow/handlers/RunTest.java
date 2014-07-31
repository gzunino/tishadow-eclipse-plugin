package com.belatrixsf.tishadow.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.belatrixsf.tishadow.runner.IRunnerCallback;
import com.belatrixsf.tishadow.tests.LaunchShortcut;


public class RunTest extends AbstractHandler implements IRunnerCallback {

	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(event.getParameter("com.belatrixsf.tishadow.tests.config") != null){
			final ILaunchConfiguration launchConfiguration = getLaunchConfiguration(event.getParameter("com.belatrixsf.tishadow.tests.config"));
			
				System.out.println("Config executed >> " + launchConfiguration.getName());
			
	            Job job = new Job("TiShadow Tests") {
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
			if (selection instanceof IStructuredSelection ) {
				
				Object element = ((IStructuredSelection)selection).getFirstElement();
				
				if (element instanceof IResource) {
					LaunchShortcut shortcut = new LaunchShortcut();
					shortcut.launch(selection, "run");
				} else {
					MessageDialog.openError(null, "Error", "To run tests, you need to select a project.");
				}
			} 
		}

		return null;
	}
	
	private ILaunchConfiguration getLaunchConfiguration(String runConfigName){
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = launchManager.getLaunchConfigurationType("com.belatrixsf.tishadow.tests.launchTiShadowTests");
        ILaunchConfiguration[] launchs = null;
        
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