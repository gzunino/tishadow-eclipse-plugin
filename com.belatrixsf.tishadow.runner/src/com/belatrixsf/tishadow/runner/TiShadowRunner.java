package com.belatrixsf.tishadow.runner;

import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
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


/**
 * @author vvillegas
 * 
 */
public class TiShadowRunner {
	private ILaunchConfigurationWorkingCopy workingCopy;
	private Object objectToReturn;
	
	/** Constructor */
	public TiShadowRunner(String configurationName) throws Exception {
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
			throw new Exception(e.getCause());
		}
	}
	
	/** Run command 
	 * @throws Exception */
	public void runTiShadow(final IRunnerCallback callback) throws Exception {
		runTiShadow(callback, null);
	}
	
	/** Run command with input
	 * @throws Exception */
	public void runTiShadow(final IRunnerCallback callback, String input) throws Exception {
		try {
			ILaunch launch = workingCopy.launch(ILaunchManager.RUN_MODE, 
					new NullProgressMonitor());
			if(input != null){
				launch.getProcesses()[0].getStreamsProxy().write(input);
			}
			addDebugEventListener(launch, callback);
		} catch (CoreException e) {
			throw new Exception(e.getCause());
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
	
	public static boolean isValidNature(String[] newNatures){
		IStatus status = ResourcesPlugin.getWorkspace().validateNatureSet(newNatures);
		return status.isOK();
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
