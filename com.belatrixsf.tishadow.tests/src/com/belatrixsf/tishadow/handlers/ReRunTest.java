package com.belatrixsf.tishadow.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.belatrixsf.tishadow.LaunchUtils;
import com.belatrixsf.tishadow.runner.IRunnerCallback;


public class ReRunTest extends AbstractHandler implements IRunnerCallback {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		LaunchUtils launchUtils = new LaunchUtils();
		final ILaunchConfiguration lConfig = launchUtils.getLaunchConfiguration();
		
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
