package com.belatrixsf.tishadow.app.wizards;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;

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
	private Text projectResourceInput;
	private Text port;
	private Text outputFolder;
	
	private ResourceListSelectionDialog dialog;
	private ProjectContentsLocationArea locationArea;
	static int value = 0;

	/**
	 * Creates a new project creation wizard page.
	 * 
	 * @param pageName
	 *            the name of this page
	 */
	public TiShadowAppifyWizardPage(String pageName) {
		super(pageName);

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

		// create a label and a button
		Label l = new Label(composite, SWT.NONE);
		l.setText("Base Project:  ");
		GridData data = new GridData(SWT.LEFT);
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

		l = new Label(composite, SWT.NONE);
		l.setText("Output folder:");
		l.setLayoutData(data);
		// outputFolder = new Text(composite, SWT.SINGLE | SWT.BORDER);
		// outputFolder.setToolTipText("The folder where the appifyied application will be created.");

		// add a button to set the output folder
		openFolderDialog(composite);

		addSeparator(composite);

		l = new Label(composite, SWT.NONE);
		l.setLayoutData(data);
		l.setText("Port:");
		setPort(new Text(composite, SWT.SINGLE | SWT.BORDER));
		getPort().setTextLimit(5);
		getPort().setToolTipText("Sets the port por tiShadow. /n'3000' is the port by default./nOnly numbers are allowed");
		
		// Default port
		getPort().setText("3000");
		
		// Allows only numbers.
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

		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	/**
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
				getProjectResourceInput().setText(
						dialog.getResult()[0].toString().substring(2));
			}

		});
	}

	private void openFolderDialog(Composite composite) {

		setLocationArea(new ProjectContentsLocationArea(getErrorReporter(), composite));

		// Scale the button based on the rest of the dialog
		setButtonLayoutData(getLocationArea().getBrowseButton());

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
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

		String projectFieldContents = getProjectNameFieldValue();
		if (projectFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
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
		getLocationArea().setExistingProject(project);

		String validLocationMessage = getLocationArea().checkValidLocation();
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
	private String getProjectNameFieldValue() {
		if (projectResourceInput == null) {
			return ""; //$NON-NLS-1$
		}

		return projectResourceInput.getText().trim();
	}

	/**
	 * @param projectResourceInput
	 *            the projectResourceInput to set
	 */
	public void setProjectResourceInput(Text projectResourceInput) {
		this.projectResourceInput = projectResourceInput;
	}

	/**
	 * @return the projectResourceInput
	 */
	public Text getProjectResourceInput() {
		return projectResourceInput;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(Text port) {
		this.port = port;
	}

	/**
	 * @return the port
	 */
	public Text getPort() {
		return port;
	}

	/**
	 * @param locationArea the locationArea to set
	 */
	public void setLocationArea(ProjectContentsLocationArea locationArea) {
		this.locationArea = locationArea;
	}

	/**
	 * @return the locationArea
	 */
	public ProjectContentsLocationArea getLocationArea() {
		return locationArea;
	}

}
