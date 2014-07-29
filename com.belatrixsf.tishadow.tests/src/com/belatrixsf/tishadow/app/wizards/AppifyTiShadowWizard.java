package com.belatrixsf.tishadow.app.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;

public class AppifyTiShadowWizard extends AbstractTiShadowWizard {
		
	@Override
	public void addPages() {
		wizardPage = new AppifyTiShadowPage("TiShadow Wizard");
		wizardPage.setTitle("Appify Project");
		wizardPage.setDescription("Select a project to generate an appify.");
		this.addPage(wizardPage);
	}

	@Override
	String getTiShadowCommandName() {
		return "TiShadow Appify";
	}
	
	@Override
	String getArguments() {
		AppifyTiShadowPage appifyProjectWizardPage = (AppifyTiShadowPage)wizardPage;
		String outputFolder = getOutputFolderPath();
		String host = appifyProjectWizardPage.getHost();
		String port = appifyProjectWizardPage.getPort();
		String room = appifyProjectWizardPage.getRoom();
		/**
		 * TiShadow appify command:
		 * 
		 * tishadow appify -d <dest_directory> -o <host> -p <port> -r <room>
		 */
		String arguments = "appify -d " + outputFolder;
		// host, port and room flags are optional. If they are empty default
		// values will be used.
		// + " -o " + host + " -p " + port;
		arguments = host.isEmpty() ? arguments
				: (arguments + " -o '" + host + "'");
		arguments = port.isEmpty() ? arguments
				: (arguments + " -p '" + port + "'");
		arguments = room.isEmpty() ? arguments
				: (arguments + " -r '" + room + "'");
		arguments = (arguments + " -m"); 
		return arguments;
	}

	@Override
	String getWorkingDirectory() {
		return wizardPage.getWorkingDirectory();
	}

	@Override
	String getInputForRunTiShadowCommand() {
		return null;
	}
	
	@Override
	protected void setReferencedProjects(final IProjectDescription description,
			IProject[] refProjects) {
		IProject[] projects = new IProject[refProjects.length+1];
		AppifyTiShadowPage appifyProjectWizardPage = (AppifyTiShadowPage)wizardPage;
		projects[refProjects.length] = appifyProjectWizardPage.getSelectedBaseProject();
		super.setReferencedProjects(description, projects);
	}
}
