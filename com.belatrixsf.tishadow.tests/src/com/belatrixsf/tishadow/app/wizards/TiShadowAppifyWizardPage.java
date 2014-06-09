package com.belatrixsf.tishadow.app.wizards;

import java.net.URI;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;
import com.belatrixsf.tishadow.preferences.page.*;

/**
 * Standard main page for a wizard that is creates a project resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Example usage:
 * 
 * <pre>
 * mainPage = new WizardNewProjectCreationPage(&quot;basicNewProjectPage&quot;);
 * mainPage.setTitle(&quot;Project&quot;);
 * mainPage.setDescription(&quot;Create a new project resource.&quot;);
 * </pre>
 * 
 * </p>
 */
public class TiShadowAppifyWizardPage extends WizardPage {

	// widgets
	private Text inputFolder;
	private Text port;
	private Text room;
	private Text host;
	private Text projectNameField;

	private String initialProjectFieldValue;

	private ResourceListSelectionDialog dialog;
	private ProjectContentsLocationArea outputFolder;
	private IProject selectedBaseProject;

	private WorkingSetGroup workingSetGroup;

	/**
	 * Creates a new project creation wizard page.
	 * 
	 * @param pageName
	 *            the name of this page
	 */
	public TiShadowAppifyWizardPage(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	/**
	 * (non-Javadoc) Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		initializeDialogUnits(parent);

		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridData data = new GridData(SWT.LEFT);

		// PROJECT NAME
		Label l = new Label(composite, SWT.NONE);
		l.setText(IDEWorkbenchMessages.WizardNewProjectCreationPage_nameLabel);
		l.setLayoutData(data);
		l.pack();
		projectNameField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		projectNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		addSeparator(composite);

		// BASE PROJECT
		l = new Label(composite, SWT.NONE);
		l.setText("Base Project:  ");
		l.setLayoutData(data);
		l.pack();
		setProjectResourceInput(new Text(composite, SWT.SINGLE | SWT.BORDER));
		getProjectResourceInput().setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		getProjectResourceInput().setEnabled(false);

		// add a button to load resources.
		createResourcesButton(parent.getShell(), composite);

		addSeparator(composite);

		data = new GridData(SWT.FILL, SWT.TOP, false, false);

		// OUTPUT FOLDER
		l = new Label(composite, SWT.NONE);
		l.setText("Output folder:");
		l.setLayoutData(data);
		openFolderDialog(composite);
		if (initialProjectFieldValue != null) {
			outputFolder.updateProjectName(initialProjectFieldValue);
		}

		addSeparator(composite);

		// PORT
		l = new Label(composite, SWT.NONE);
		l.setLayoutData(data);
		l.setText("Port:");
		setPort(new Text(composite, SWT.SINGLE | SWT.BORDER));
		getPort().setTextLimit(6);
		String port = String.valueOf(PreferenceValues.getTishadowPort());
		System.out.println("ESTE ES MI PUERTO!!! " + port);
		getPort()
				.setToolTipText(
						"Sets the port for tiShadow.\n'" + port + "' is the port by default.\n*Only numbers are allowed*");
		getPort().setText(port); // Default port

		// Allows only numbers for the port input.
		getPort().addVerifyListener(new VerifyListener() {

			@Override
			public void verifyText(final VerifyEvent event) {
				switch (event.keyCode) {
				case SWT.BS: // Backspace
				case SWT.DEL: // Delete
				case SWT.HOME: // Home
				case SWT.END: // End
				case SWT.ARROW_LEFT: // Left arrow
				case SWT.ARROW_RIGHT: // Right arrow
					return;
				}

				if (!Character.isDigit(event.character)) {
					event.doit = false; // disallow the action
				}
			}

		});

		addSeparator(composite);

		// HOST
		l = new Label(composite, SWT.NONE);
		l.setLayoutData(data);
		l.setText("Host:");
		setHost(new Text(composite, SWT.SINGLE | SWT.BORDER));
		getHost().setToolTipText("Sets the host for tiShadow.\n*Optional*");
		getHost().setText(PreferenceValues.getTishadowHost());
		addSeparator(composite);

		// ROOM (OPTIONAL)
		l = new Label(composite, SWT.NONE);
		l.setLayoutData(data);
		l.setText("Room (optional):");
		setRoom(new Text(composite, SWT.SINGLE | SWT.BORDER));
		getRoom().setToolTipText("Sets the room for tiShadow.\n*Optional*");

		setControl(composite);
		Dialog.applyDialogFont(composite);

		getProjectResourceInput().addListener(SWT.Modify,
				inputFolderModifyListener);
		projectNameField.addListener(SWT.Modify, nameModifyListener);
	}

	/**
	 * Creates button to select the input folder to appify.
	 * 
	 * @param shell
	 * @param composite
	 */
	private void createResourcesButton(Shell shell, Composite composite) {
		Button button = new Button(composite, SWT.PUSH);
		button.setText("Select Project");
		openResourcesDialog(shell);

		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dialog.open();
				selectedBaseProject = (IProject) dialog.getResult()[0];
				selectedBaseProject.getLocation().toOSString();
				getProjectResourceInput().setText(
						dialog.getResult()[0].toString().substring(2));
			}
		});
	}

	private void openFolderDialog(Composite composite) {

		setLocationArea(new ProjectContentsLocationArea(getErrorReporter(),
				composite));

		// Scale the button based on the rest of the dialog
		setButtonLayoutData(getOutputFolder().getBrowseButton());

	}

	/**
	 * Draws a separator line for UI.
	 * 
	 * @param separator
	 * @param composite
	 */
	private void addSeparator(Composite composite) {
		Label separator;
		// create a new label which is used as a separator
		GridData separatorGrid = new GridData(SWT.FILL, SWT.TOP, true, false,
				3, 1);

		separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(separatorGrid);
	}

	/**
	 * 
	 * @param shell
	 */
	private void openResourcesDialog(Shell shell) {
		IResource[] resourcesArray = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		dialog = new ResourceListSelectionDialog(shell, resourcesArray);
		dialog.setTitle("Resource Selection");
	}

	/**
	 * Get an error reporter for the receiver.
	 * 
	 * @return IErrorMessageReporter
	 */
	private IErrorMessageReporter getErrorReporter() {
		return new IErrorMessageReporter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea
			 * .IErrorMessageReporter#reportError(java.lang.String)
			 */
			public void reportError(String errorMessage, boolean infoOnly) {
				if (infoOnly) {
					setMessage(errorMessage, IStatus.INFO);
					setErrorMessage(null);
				} else
					setErrorMessage(errorMessage);
				boolean valid = errorMessage == null;
				if (valid) {
					valid = validatePage();
				}

				setPageComplete(valid);
			}
		};
	}

	/**
	 * Returns whether this page's controls currently all contain valid values.
	 * 
	 * @return <code>true</code> if all controls are valid, and
	 *         <code>false</code> if at least one is invalid
	 */
	protected boolean validatePage() {
		@SuppressWarnings("restriction")
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

		// Check Project Name
		String projectFieldContents = getProjectNameFieldValue();
		if (projectFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
			return false;
		}

		// Check Input Resource Folder
		if (inputFolder.getText().equals("")) {
			setErrorMessage(null);
			setMessage("Base Project to appify must me specified");
			return false;
		}

		IStatus nameStatus = workspace.validateName(projectFieldContents,
				IResource.PROJECT);
		if (!nameStatus.isOK()) {
			setErrorMessage(nameStatus.getMessage());
			return false;
		}

		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(getProjectNameFieldValue());

		getOutputFolder().setExistingProject(project);

		// Check outputFolder
		String validLocationMessage = getOutputFolder().checkValidLocation();
		if (validLocationMessage != null) { // there is no destination location
											// given
			setErrorMessage(validLocationMessage);
			return false;
		}
		
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	/**
	 * Returns the value of the project name field with leading and trailing
	 * spaces removed.
	 * 
	 * @return the project name in the field
	 */
	protected String getProjectNameFieldValue() {
		if (projectNameField == null) {
			return ""; //$NON-NLS-1$
		}

		return projectNameField.getText().trim();
	}

	/**
	 * @param projectResourceInput
	 *            the projectResourceInput to set
	 */
	public void setProjectResourceInput(Text projectResourceInput) {
		this.inputFolder = projectResourceInput;
	}

	/**
	 * @return the projectResourceInput
	 */
	private Text getProjectResourceInput() {
		return inputFolder;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(Text port) {
		this.port = port;
	}

	/**
	 * @return the port
	 */
	private Text getPort() {
		return port;
	}

	protected String getPortFieldValue() {
		return getPort().getText();
	}

	/**
	 * @param locationArea
	 *            the locationArea to set
	 */
	public void setLocationArea(ProjectContentsLocationArea locationArea) {
		outputFolder = locationArea;
	}

	/**
	 * @return the locationArea
	 */
	protected ProjectContentsLocationArea getOutputFolder() {
		return outputFolder;
	}

	protected String getOutputFolderLocation() {
		return getOutputFolder().getProjectLocation();
	}

	/**
	 * @param room
	 *            the room to set
	 */
	public void setRoom(Text room) {
		this.room = room;
	}

	/**
	 * @return the room
	 */
	public Text getRoom() {
		return room;
	}

	protected String getRoomFieldValue() {
		return getRoom().getText();
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(Text host) {
		this.host = host;
	}

	/**
	 * @return the host
	 */
	private Text getHost() {
		return host;
	}

	protected String getHostFieldValue() {
		return getHost().getText();
	}

	/**
	 * @return the Selected Base Project
	 */
	private IProject getSelectedBaseProject() {
		return this.selectedBaseProject;
	}

	protected String getSelectedBaseProjectPath() {
		return getSelectedBaseProject().getLocation().toOSString();
	}

	/**
	 * Set the location to the default location if we are set to useDefaults.
	 */
	void setLocationForSelection() {
		outputFolder.updateProjectName(getProjectNameFieldValue());
	}

	/**
	 * Creates a project resource handle for the current project name field
	 * value. The project handle is created relative to the workspace root.
	 * <p>
	 * This method does not create the project resource; this is the
	 * responsibility of <code>IProject::create</code> invoked by the new
	 * project resource wizard.
	 * </p>
	 * 
	 * @return the new project resource handle
	 */
	public IProject getProjectHandle() {
		return ResourcesPlugin.getWorkspace().getRoot()
				.getProject(getProjectName());
	}

	/**
	 * Returns the current project name as entered by the user, or its
	 * anticipated initial value.
	 * 
	 * @return the project name, its anticipated initial value, or
	 *         <code>null</code> if no project name is known
	 */
	public String getProjectName() {
		if (projectNameField == null) {
			return initialProjectFieldValue;
		}

		return getProjectNameFieldValue();
	}

	/**
	 * Returns the current project location URI as entered by the user, or
	 * <code>null</code> if a valid project location has not been entered.
	 * 
	 * @return the project location URI, or <code>null</code>
	 * @since 3.2
	 */
	public URI getLocationURI() {
		return getOutputFolder().getProjectLocationURI();
	}

	/**
	 * Returns the useDefaults.
	 * 
	 * @return boolean
	 */
	public boolean useDefaults() {
		return getOutputFolder().isDefault();
	}

	/**
	 * Return the selected working sets, if any. If this page is not configured
	 * to interact with working sets this will be an empty array.
	 * 
	 * @return the selected working sets
	 * @since 3.4
	 */
	public IWorkingSet[] getSelectedWorkingSets() {
		return workingSetGroup == null ? new IWorkingSet[0] : workingSetGroup
				.getSelectedWorkingSets();
	}

	/**
	 * Create a working set group for this page. This method can only be called
	 * once.
	 * 
	 * @param composite
	 *            the composite in which to create the group
	 * @param selection
	 *            the current workbench selection
	 * @param supportedWorkingSetTypes
	 *            an array of working set type IDs that will restrict what types
	 *            of working sets can be chosen in this group
	 * @return the created group. If this method has been called previously the
	 *         original group will be returned.
	 * @since 3.4
	 */
	public WorkingSetGroup createWorkingSetGroup(Composite composite,
			IStructuredSelection selection, String[] supportedWorkingSetTypes) {
		if (workingSetGroup != null)
			return workingSetGroup;
		workingSetGroup = new WorkingSetGroup(composite, selection,
				supportedWorkingSetTypes);
		return workingSetGroup;
	}

	// initial value stores
	private Listener nameModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setLocationForSelection();
			boolean valid = validatePage();
			setPageComplete(valid);

		}
	};

	private Listener inputFolderModifyListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			boolean valid = validatePage();
			setPageComplete(valid);
		}
	};

}