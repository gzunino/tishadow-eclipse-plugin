package com.belatrixsf.tishadow.app.wizards;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.eclipse.ui.dialogs.WorkingSetGroup;
//import org.eclipse.ui.dialogs.ResourceListSelectionDialog.ResourceDescriptor;
//import org.eclipse.ui.dialogs.ResourceListSelectionDialog.UpdateFilterThread;
//import org.eclipse.ui.dialogs.ResourceListSelectionDialog.UpdateGatherThread;
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
 * <pre>
 * mainPage = new WizardNewProjectCreationPage("basicNewProjectPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Create a new project resource.");
 * </pre>
 * </p>
 */
public class TiShadowAppifyWizardPage extends WizardPage {

    // widgets
    Text projectNameField;
    ResourceListSelectionDialog dialog;
//    CustomDialog a;
    /**
     * Creates a new project creation wizard page.
     *
     * @param pageName the name of this page
     */
    public TiShadowAppifyWizardPage(String pageName) {
    	super(pageName);
	    setPageComplete(true);
    }

	/** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
    

        initializeDialogUnits(parent);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
                IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // create a label and a button
        Label label = new Label(composite, SWT.NONE);
        label.setText("A label");
        Button button = new Button(composite, SWT.PUSH);
        button.setText("Press Me");
        IResource[] resourcesArray = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        dialog = new ResourceListSelectionDialog(parent.getShell(), resourcesArray);
        dialog.setTitle("Resource Selection");
//        a = new CustomDialog(parent.getShell(), resourcesArray);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
 //               a.open();
            }
        }); 
        
        setControl(composite);
        Dialog.applyDialogFont(composite);
    }
    
//    public class CustomDialog extends ResourceListSelectionDialog{
//
//    	Table resourceNames;
//		public CustomDialog(Shell parentShell, IResource[] resources) {
//			super(parentShell, resources);
//		}
//		@Override
//		protected Control createDialogArea(Composite parent) {
//			   Control a = super.createDialogArea(parent);
//			   
//		        Composite dialogArea = (Composite) super.createDialogArea(parent);
//		        Label l = new Label(dialogArea, SWT.NONE);
//		        l.setText(IDEWorkbenchMessages.ResourceSelectionDialog_label);
//		        GridData data = new GridData(GridData.FILL_HORIZONTAL);
//		        l.setLayoutData(data);
//
//		        Text pattern = new Text(dialogArea, SWT.SINGLE | SWT.BORDER);
//		        pattern.setText("*");
//		        pattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		        l = new Label(dialogArea, SWT.NONE);
//		        l.setText(IDEWorkbenchMessages.ResourceSelectionDialog_matching);
//		        data = new GridData(GridData.FILL_HORIZONTAL);
//		        l.setLayoutData(data);
//		        resourceNames = new Table(dialogArea, SWT.SINGLE | SWT.BORDER
//		                | SWT.V_SCROLL);
//		        data = new GridData(GridData.FILL_BOTH);
//		        data.heightHint = 12 * resourceNames.getItemHeight();
//		        resourceNames.setLayoutData(data);
//
//		        l = new Label(dialogArea, SWT.NONE);
//		        l.setText(IDEWorkbenchMessages.ResourceSelectionDialog_folders);
//		        data = new GridData(GridData.FILL_HORIZONTAL);
//		        l.setLayoutData(data);
//
//		        Table folderNames = new Table(dialogArea, SWT.SINGLE | SWT.BORDER
//		                | SWT.V_SCROLL | SWT.H_SCROLL);
//		        data = new GridData(GridData.FILL_BOTH);
//		        data.widthHint = 300;
//		        data.heightHint = 4 * folderNames.getItemHeight();
//		        folderNames.setLayoutData(data);
//
////		        if (gatherResourcesDynamically) {
////		        	UpdateGatherThread updateGatherThread = new UpdateGatherThread();
////		        } else {
////		        	UpdateFilterThread updateFilterThread = new UpdateFilterThread();
////		        }
//
//		        pattern.addKeyListener(new KeyAdapter() {
//		            public void keyReleased(KeyEvent e) {
//		                if (e.keyCode == SWT.ARROW_DOWN) {
//							resourceNames.setFocus();
//						}
//		            }
//		        });
//
//		        pattern.addModifyListener(new ModifyListener() {
//		            public void modifyText(ModifyEvent e) {
//		                refresh(false);
//		            }
//		        });
//
//		        resourceNames.addSelectionListener(new SelectionAdapter() {
//		            public void widgetSelected(SelectionEvent e) {
//		                updateFolders((ResourceDescriptor) e.item.getData());
//		            }
//
//		            public void widgetDefaultSelected(SelectionEvent e) {
//		                okPressed();
//		            }
//		        });
//
//		        folderNames.addSelectionListener(new SelectionAdapter() {
//		            public void widgetDefaultSelected(SelectionEvent e) {
//		                okPressed();
//		            }
//		        });
//
//		        if (getAllowUserToToggleDerived()) {
//		            showDerivedButton = new Button(dialogArea, SWT.CHECK);
//		            showDerivedButton.setText(IDEWorkbenchMessages.ResourceSelectionDialog_showDerived);
//		            showDerivedButton.addSelectionListener(new SelectionAdapter() {
//		                public void widgetSelected(SelectionEvent e) {
//		                    setShowDerived(showDerivedButton.getSelection());
//		                    refresh(true);
//		                }
//		            });
//		            showDerivedButton.setSelection(getShowDerived());
//		        }
//		            
//		        applyDialogFont(dialogArea);
//		        return dialogArea;
//			   
//		   }
//		
//    }

    
}
