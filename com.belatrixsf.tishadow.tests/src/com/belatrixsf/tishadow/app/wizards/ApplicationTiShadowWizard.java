package com.belatrixsf.tishadow.app.wizards;

public class ApplicationTiShadowWizard extends TiShadowWizard {

	@Override
	public void addPages() {
		wizardPage = new ApplicationTiShadowPage("Base Project Creation");
		wizardPage.setTitle("Project");
		wizardPage.setDescription("Settings");
		this.addPage(wizardPage);
	}

	@Override
	String getTiShadowCommandName() {
		return "TiShadow Project";
	}

	@Override
	String getArguments() {
		return "app -d " + getNewProject().getLocation().toOSString();
	}

	@Override
	String getWorkingDirectory() {
		return getNewProject().getParent().getLocation().toOSString();
	}

	@Override
	String getInputForRunTiShadowCommand() {
		return "com.test.app";
	}

}
