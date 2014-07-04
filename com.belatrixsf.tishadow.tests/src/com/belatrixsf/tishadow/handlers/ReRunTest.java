package com.belatrixsf.tishadow.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.belatrixsf.tishadow.common.ArgsBuilder;
import com.belatrixsf.tishadow.tests.*;
import com.belatrixsf.tishadow.LaunchUtils;
import com.belatrixsf.tishadow.preferences.page.PreferenceValues;
import com.belatrixsf.tishadow.runner.Constants;
import com.belatrixsf.tishadow.runner.IRunnerCallback;
import com.belatrixsf.tishadow.runner.TiShadowRunner;


public class ReRunTest extends AbstractHandler implements IRunnerCallback {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		LaunchUtils launchUtil = new LaunchUtils();
		final ILaunchConfiguration lConfig = launchUtil.getLaunchConfiguration();
		
            Job job = new Job("TiShadow Tests") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					this.setThread(Thread.currentThread());
					try {
						lConfig.launch("run", monitor);
					} catch (CoreException e) {
						e.printStackTrace();
					}
					return ASYNC_FINISH;
				}
			};
			job.setUser(true);
			job.schedule();
		
		return null;
	}

	@Override
	public void onRunnerTishadowFinish(Object response) {
		
	}
	
}
