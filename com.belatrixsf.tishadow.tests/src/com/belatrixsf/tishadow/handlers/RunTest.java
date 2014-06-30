package com.belatrixsf.tishadow.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
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


public class RunTest extends AbstractHandler implements IRunnerCallback {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection ) {
			
			Object element = ((IStructuredSelection)selection).getFirstElement();
			
			if (element instanceof IResource) {
				IProject project = ((IResource) element).getProject();
				String projectName = project.getName();
				LaunchShortcut shortcut = new LaunchShortcut();
				shortcut.launch(selection, "run");
			} else {
				MessageDialog.openError(null, "Error", "To run tests, you need to select a project.");
			}
		} 
		
		return null;
	}

	@Override
	public void onRunnerTishadowFinish(Object response) {
		// TODO Auto-generated method stub
		
	}
	
}
