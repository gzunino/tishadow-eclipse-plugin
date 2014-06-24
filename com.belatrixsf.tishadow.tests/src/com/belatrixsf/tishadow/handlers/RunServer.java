package com.belatrixsf.tishadow.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

import com.belatrixsf.tishadow.LaunchUtils;
import com.belatrixsf.tishadow.preferences.page.PreferenceValues;
import com.belatrixsf.tishadow.runner.Constants;
import com.belatrixsf.tishadow.runner.IRunnerCallback;
import com.belatrixsf.tishadow.runner.TiShadowRunner;

public class RunServer extends AbstractHandler implements IRunnerCallback {

	boolean isServerLaunched = false;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		DebugPlugin.getDefault().addDebugEventListener(
				new IDebugEventSetListener() {
					@Override
					public void handleDebugEvents(DebugEvent[] events) {
						if (events.length > 0
								&& (events[0].getKind() == DebugEvent.CREATE || events[0]
										.getKind() == DebugEvent.TERMINATE)) {
							try {
								// In order to refresh the plugin button we need
								// to wait some time to wait for the server to
								// be running.
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							requestRefresh();
							DebugPlugin.getDefault().removeDebugEventListener(
									this);
						}
					}
				});
		isServerLaunched = LaunchUtils.isServerLaunched();
		try {
			if (isServerLaunched) {
				LaunchUtils.stopTiShadowServer();
			} else {
				startTiShadowServer();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	protected void requestRefresh() {
		try {
			final IEvaluationService evaluationService = (IEvaluationService) PlatformUI
					.getWorkbench().getService(IEvaluationService.class);
			if (evaluationService != null) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						evaluationService
								.requestEvaluation("com.belatrixsf.tishadow.tests.serverRunning");
						System.out.println("REFRESHED");
					}
				});
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void startTiShadowServer() throws Exception {
		TiShadowRunner tishadowRunner = new TiShadowRunner("Server");
		tishadowRunner
				.setAttribute(Constants.TISHADOW_WORKING_DIRECTORY, "")
				.setAttribute(Constants.TISHADOW_TOOL_ARGUMENTS, "server")
				.setAttribute(Constants.TISHADOW_SHOW_CONSOLE, true)
				.setAttribute(Constants.TISHADOW_ENVIRONMENT_VARIABLES,
						LaunchUtils.getEnvVars())
				.setAttribute(Constants.TISHADOW_LOCATION,
						PreferenceValues.getTishadowDirectory());
		tishadowRunner.runTiShadow(this);
	}

	@Override
	public void onRunnerTishadowFinish(Object response) {
		System.out.println("TiShadow server started.");
	}

}
