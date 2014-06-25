/**
 * 
 */
package com.belatrixsf.tishadow.app.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;

import com.belatrixsf.tishadow.preferences.page.PreferenceValues;

@SuppressWarnings("restriction")
public class AppifyTiShadowPage extends AbstractTiShadowPage {

	private Text port;
	private Text room;
	private Text host;
	private ResourceListSelectionDialog dialog;
	
	/**Name of project that will be used as base to appify*/
	private Text baseProjectName;
	
	/**Project object that will be used to appify*/
	private IProject selectedBaseProject;

	/** Constructor */
	protected AppifyTiShadowPage(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	@Override
	void addExtraFields() {

		super.addSeparator();

		GridData extraFieldsGrid = new GridData(SWT.FILL, SWT.TOP, false, false);
		
		addBaseProjectField(extraFieldsGrid);
	
		super.addSeparator();

		addPortField(extraFieldsGrid);

		super.addSeparator();

		addHostField(extraFieldsGrid);

		super.addSeparator();

		addRoomField(extraFieldsGrid);

		baseProjectName.addListener(SWT.Modify, inputFolderModifyListener);
	}

	public String getPort() {
		return port.getText();
	}

	public String getRoom() {
		return room.getText();
	}

	public String getHost() {
		return host.getText();
	}

	@Override
	String getWorkingDirectory() {
		return selectedBaseProject.getLocation().toOSString();
	}

	/**
	 * Returns whether this page's controls currently all contain valid values.
	 * 
	 * @return <code>true</code> if all controls are valid, and
	 *         <code>false</code> if at least one is invalid
	 */
	@Override
	protected boolean validatePage() {
		if(!super.validatePage()){
			return false;
		}
		if (baseProjectName != null && baseProjectName.getText().equals("")) {
			setErrorMessage(null);
			setMessage("Base Project to appify must me specified");
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
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
				if(dialog.getResult() != null){
					selectedBaseProject = (IProject) dialog.getResult()[0];
					baseProjectName.setText(dialog.getResult()[0].toString().substring(2));
				}
			}
		});
	}

	private Listener inputFolderModifyListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			projectNameField.setText(getStandardProjectName());
			boolean valid = validatePage();
			setPageComplete(valid);
		}
	};

	private String getStandardProjectName() {
		return baseProjectName.getText() + "Appified";
	}
	/**
	 * 
	 * @param shell
	 */
	private void openResourcesDialog(Shell shell) {
		IResource[] resourcesArray = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		dialog = new ResourceListSelectionDialog(shell, resourcesArray) {
			@Override
			protected String adjustPattern() {
				String text = super.adjustPattern();
				if ("".equals(text)) {
					text = "*";
				}
				return text;
			}

			@Override
			public void create() {
				super.create();
				super.refresh(true);
			}
		};
		dialog.setTitle("Resource Selection");
	}	

	private void addRoomField(GridData data) {
		Label roomLabel = new Label(composite, SWT.NONE);
		roomLabel.setLayoutData(data);
		roomLabel.setText("Room (optional):");
		this.room = new Text(composite, SWT.SINGLE | SWT.BORDER);
		this.room.setToolTipText("Sets the room for tiShadow.\n*Optional*");
		this.room.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void addHostField(GridData data) {
		Label hostLabel = new Label(composite, SWT.NONE);
		hostLabel.setLayoutData(data);
		hostLabel.setText("Host:");
		this.host = new Text(composite, SWT.SINGLE | SWT.BORDER);
		this.host.setToolTipText("Sets the host for tiShadow.\n*Optional*");
		this.host.setText(PreferenceValues.getTishadowHost());
		this.host.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void addPortField(GridData data) {
		Label portLabel = new Label(composite, SWT.NONE);
		portLabel.setLayoutData(data);
		portLabel.setText("Port:");
		this.port = new Text(composite, SWT.SINGLE | SWT.BORDER);
		this.port.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.port.setTextLimit(6);
		String port = String.valueOf(PreferenceValues.getTishadowPort());
		this.port.setToolTipText("Sets the port for tiShadow.\n'" + port
				+ "' is the port set by default.\n*Only numbers are allowed*");
		this.port.setText(port); // Default port

		// Allows only numbers for the port input.
		this.port.addVerifyListener(new VerifyListener() {
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
	}

	private void addBaseProjectField(GridData data) {
		Label projectLocationLabel = new Label(composite, SWT.NONE);
		projectLocationLabel.setText("Base Project:  ");
		projectLocationLabel.setLayoutData(data);
		projectLocationLabel.pack();
		this.baseProjectName = new Text(composite, SWT.SINGLE | SWT.BORDER);
		baseProjectName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		baseProjectName.setEnabled(false);
		if (getCurrentProject() != null) {
			String projectName = getCurrentProject().getName().toString();
			selectedBaseProject = getCurrentProject();
			baseProjectName.setText(projectName);
			super.projectNameField.setText(getStandardProjectName());
		} else {
			baseProjectName.setMessage("The project to be appifyied");
		}
		// add a button to load resources.
		createResourcesButton(super.compositeParent.getShell(), super.composite);
	}

	/*
	 * Returns the selected project.
	 * 
	 * @return project
	 */
	private static IProject getCurrentProject() {
		ISelectionService selectionService = Workbench.getInstance()
				.getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = selectionService.getSelection();
		IProject project = null;

		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
					.getFirstElement();
			if (element instanceof IResource) {
				project = ((IResource) element).getProject();
			}
		}
		return project;
	}
}
