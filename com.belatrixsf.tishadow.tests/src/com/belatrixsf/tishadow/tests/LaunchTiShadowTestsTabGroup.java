package com.belatrixsf.tishadow.tests;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.belatrixsf.tishadow.common.TiShadowTab;

public class LaunchTiShadowTestsTabGroup extends
		AbstractLaunchConfigurationTabGroup {
		
	public LaunchTiShadowTestsTabGroup() {
	}

	TiShadowTab tishadowSpecTab = new TiShadowSpecTab();
	
	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				tishadowSpecTab, new CommonTab() };
		setTabs(tabs);
	}
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		IResource context = getContext();
		try {
			LaunchTiShadowTests.setLaunchAttributesWithArguments(configuration, context, tishadowSpecTab.getArguments());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		super.setDefaults(configuration);
	}

	protected IResource getContext() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
		
		if (page != null) {
			ISelection selection = page.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (!ss.isEmpty()) {
					Object obj = ss.getFirstElement();
					if (obj instanceof IResource) {
						return (IResource) obj;
					}
				}
			}
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IEditorInput input = part.getEditorInput();
				return (IResource) input.getAdapter(IResource.class);
			}
		}
		return null;
	}
}
