package com.belatrixsf.tishadow.server;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

@SuppressWarnings("restriction")
public class LaunchServerShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (element instanceof IResource) {
				IProject project = ((IResource) element).getProject();
				searchAndLaunch(project, mode);
			}
		}
	}
	
	private void searchAndLaunch(IProject project, String mode) {
		ILaunchConfiguration launch = getExistingLaunch(project);
		if (launch != null) {
			try {
				launch.launch(mode, new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	private ILaunchConfiguration getExistingLaunch(IProject project) {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = launchManager.getLaunchConfigurationType("com.belatrixsf.tishadow.server.launchTiShadowServer");
		ILaunchConfiguration[] launchs;
		try {
			launchs = launchManager.getLaunchConfigurations(type);
			for (ILaunchConfiguration iLaunchConfiguration : launchs) {
				try {
					if ( isSameLaunch(project, iLaunchConfiguration)) {
						return iLaunchConfiguration;
					}
				} catch (CoreException e) {
				}
			}
		} catch (CoreException e1) {
		}
		
		try {
			ILaunchConfigurationWorkingCopy launch = type.newInstance(null, launchManager.generateLaunchConfigurationName(project.getName()));
			LaunchTiShadowServer.setLaunchAttributes(launch, project);
			return launch.doSave();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean isSameLaunch(IProject project, ILaunchConfiguration iLaunchConfiguration) throws CoreException {
		return iLaunchConfiguration.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, "").equals(LaunchTiShadowServer.getLaunchDir(project));
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		if (editor.getClass().getSimpleName().contains(("JSSourceEditor"))) {
	        IEditorInput input = editor.getEditorInput();
	        IProject project = ((IFileEditorInput) input).getFile().getProject();
			searchAndLaunch(project, mode);
        }
	}

}
