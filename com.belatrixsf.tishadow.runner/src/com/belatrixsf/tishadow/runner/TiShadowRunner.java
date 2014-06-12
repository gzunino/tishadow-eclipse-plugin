package com.belatrixsf.tishadow.runner;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;


/**
 * @author vvillegas
 * 
 */
public class TiShadowRunner {
	private ILaunchConfigurationWorkingCopy workingCopy;
	private Object objectToReturn;
	
	/** Constructor */
	public TiShadowRunner(String configurationName) {
		ILaunchManager launchManager = DebugPlugin.getDefault()
				.getLaunchManager();
		ILaunchConfigurationType type = DebugPlugin
				.getDefault()
				.getLaunchManager()
				.getLaunchConfigurationType(
						"org.eclipse.ui.externaltools.ProgramLaunchConfigurationType");
		try {
			workingCopy = type.newInstance(null, launchManager
					.generateLaunchConfigurationName(configurationName));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** Run command */
	public void runTiShadow(final IRunnerCallback callback) {
		try {
			ILaunch launch = workingCopy.launch(ILaunchManager.RUN_MODE, 
					new NullProgressMonitor());
			addDebugEventListener(launch, callback);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** Set string value to working copy */
	public TiShadowRunner setAttribute(String attribute, String value) {
		workingCopy.setAttribute(attribute, value);
		return this;
	}

	/** Set boolean value to working copy */
	public TiShadowRunner setAttribute(String attribute, boolean value) {
		workingCopy.setAttribute(attribute, value);
		return this;
	}

	/** Set integer value to working copy */
	public TiShadowRunner setAttribute(String attribute, int value) {
		workingCopy.setAttribute(attribute, value);
		return this;
	}

	/** Set Map<String, String> value to working copy */
	public TiShadowRunner setAttribute(String attribute,
			Map<String, String> value) {
		workingCopy.setAttribute(attribute, value);
		return this;
	}
	
	private void addDebugEventListener(final ILaunch launch, final IRunnerCallback callback) {
		setObjectToReturn(launch);
		DebugPlugin.getDefault().addDebugEventListener(
			new IDebugEventSetListener() {
				@Override
				public void handleDebugEvents(DebugEvent[] events) {
					if (events.length > 0
							&& (events[0].getKind() == DebugEvent.TERMINATE)) {
						DebugPlugin.getDefault().removeDebugEventListener(
								this);
						if(callback != null){
							callback.onRunnerTishadowFinish(objectToReturn);
						}
					}
				}
			});
		
	}

	private void setObjectToReturn(final ILaunch launch) {
		launch.getProcesses()[0].getStreamsProxy().getOutputStreamMonitor().addListener(new IStreamListener() {
			@Override
			public void streamAppended(String text, IStreamMonitor monitor) {
				objectToReturn = text;
			}
		});
	}
}
