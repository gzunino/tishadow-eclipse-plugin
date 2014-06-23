package com.belatrixsf.tishadow.app.wizards;

public class ApplicationTiShadowWizard extends AbstractTiShadowWizard {

	@Override
	public void addPages() {
		wizardPage = new ApplicationTiShadowPage("TiShadow Wizard");
		wizardPage.setTitle("TiShadow Project");
		wizardPage.setDescription("Create a TiShadow project.");
		this.addPage(wizardPage);
	}

	@Override
	String getTiShadowCommandName() {
		return "TiShadow Project";
	}

	@Override
	String getArguments() {
		return "app -d " + getOutputFolderPath();
	}

	@Override
	String getWorkingDirectory() {
		return getNewProject().getParent().getLocation().toOSString();
	}

	@Override
	String getInputForRunTiShadowCommand() {
		return wizardPage.getProjectName();
	}

}
