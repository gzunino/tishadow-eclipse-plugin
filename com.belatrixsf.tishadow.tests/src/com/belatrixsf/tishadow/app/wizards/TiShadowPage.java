package com.belatrixsf.tishadow.app.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;

@SuppressWarnings("restriction")
public abstract class TiShadowPage extends WizardPage {

	protected ProjectContentsLocationArea outputFolder;
	protected String initialProjectFieldValue;
	protected Composite composite;
	protected Composite compositeParent;
	protected Text projectNameField;
	private GridData data;

	/** Constructor */
	protected TiShadowPage(String pageName) {
		super(pageName);
	}

	/**
	 * Draws a separator line for UI.
	 * 
	 * @param separator
	 * @param composite
	 */
	protected void addSeparator() {
		Label separator;
		// create a new label which is used as a separator
		GridData separatorGrid = new GridData(SWT.FILL, SWT.TOP, true, false,
				3, 1);
		separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(separatorGrid);
	}
	
	/**
	 * (non-Javadoc) Method declared on IDialogPage.
	 */
	public final void createControl(Composite parent) {
		initializeParent(parent);
		addProjectNameField();
		addSeparator();
		addOutputFolderField();
		addExtraFields();
		setControl(composite);
	}

	public ProjectContentsLocationArea getOutputFolder() {
		return outputFolder;
	}

	// initial value stores
	private Listener nameModifyListener = new Listener() {
		public void handleEvent(Event e) {
			outputFolder.updateProjectName(getProjectNameField());
			boolean valid = validatePage();
			setPageComplete(valid);
		}
	};
	/**
	 * Returns the value of the project name field with leading and trailing
	 * spaces removed.
	 * 
	 * @return the project name in the field
	 */
	public String getProjectNameField() {
		if (projectNameField == null) {
			return ""; //$NON-NLS-1$
		}
		return projectNameField.getText().trim();
	}

	abstract void addExtraFields();
	
	abstract String getWorkingDirectory();
	
	private void addOutputFolderField() {
		Label l = new Label(composite, SWT.NONE);
		l.setText("Output folder:");
		l.setLayoutData(data);
		openFolderDialog(composite);
		if (initialProjectFieldValue != null) {
			outputFolder.updateProjectName(initialProjectFieldValue);
		}
		projectNameField.notifyListeners(SWT.Modify, new Event());
	}
	
	private void openFolderDialog(Composite composite) {
		outputFolder = new ProjectContentsLocationArea(getErrorReporter(),
				composite);
		// Scale the button based on the rest of the dialog
		setButtonLayoutData(outputFolder.getBrowseButton());
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
	
	private void addProjectNameField() {
		Label l = new Label(composite, SWT.NONE);
		l.setText(IDEWorkbenchMessages.WizardNewProjectCreationPage_nameLabel);
		l.setLayoutData(data);
		l.pack();
		projectNameField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		projectNameField.setText("TiShadowApplication");
		projectNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectNameField.addListener(SWT.Modify, nameModifyListener);
	}

	private void initializeParent(Composite parent) {
		compositeParent = parent;
		composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		data = new GridData(SWT.LEFT);
	}

	/**
	 * Returns whether this page's controls currently all contain valid values.
	 * 
	 * @return <code>true</code> if all controls are valid, and
	 *         <code>false</code> if at least one is invalid
	 */
	protected boolean validatePage() {
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

		// Check Project Name
		String projectFieldContents = getProjectNameField();
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
				.getProject(getProjectNameField());

		outputFolder.setExistingProject(project);

		// Check outputFolder
		String validLocationMessage = outputFolder.checkValidLocation();
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
		return getProjectNameField();
	}
}
