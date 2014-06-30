package com.belatrixsf.tishadow.handlers;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.belatrixsf.tishadow.app.wizards.AppifyTiShadowWizard;

public class TiShadowAppifyCreation extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		AppifyTiShadowWizard wizard = new AppifyTiShadowWizard();
		WizardDialog w = new WizardDialog(shell, wizard);
		w.open();
		
		return null;
	}
	
}
