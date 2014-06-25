package com.belatrixsf.tishadow.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class RunTest extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection ) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (element instanceof IResource) {
				IProject project = ((IResource) element).getProject();
				String projectName = project.getName();
				MessageDialog.openInformation(null, "Crazy info", "You have selected: " + projectName);
			} else {
				MessageDialog.openError(null, "Error", "To run tests, you need to select at least one project.");
			}
		} 
		
		return null;
	}

}
