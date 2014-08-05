package com.belatrixsf.tishadow.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.belatrixsf.tishadow.LaunchUtils;
import com.belatrixsf.tishadow.tests.LaunchShortcut;


@SuppressWarnings("restriction")
public class TiLaunchShortcuts {
	
	private List<LaunchShortcutExtension> fShortcuts;
	private static Composite toolbar = null;
	
	public void createControl(final Composite toolbar) {
		TiLaunchShortcuts.setToolbar(toolbar);
		
		final ToolItem item = new ToolItem ((ToolBar) toolbar, SWT.DROP_DOWN);
		item.setText("Launch app");
		item.addListener (SWT.Selection, new Listener () {
			@Override
			public void handleEvent(Event event) {
				if (event.detail == SWT.ARROW) {
					
					MenuManager menuManager = new MenuManager(null, "group.shortcuts");
					fillMenu(menuManager);
					
					menuManager.createContextMenu(toolbar).setVisible(true);
					
					Rectangle rect = item.getBounds ();
					Point pt = new Point (rect.x, rect.y + rect.height);
					pt = toolbar.toDisplay (pt);
				}
			}
		});
		
		toolbar.pack ();
	}
	
	@SuppressWarnings("unchecked")
	private void fillMenu(MenuManager menuManager) {
		
		LaunchConfigurationManager configurationManager = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
		fShortcuts = configurationManager.getLaunchShortcuts();
		
		List<LaunchShortcutExtension> filteredList = getShortcutsMatchingMode();
		
		IContributionItem item = menuManager.find("group.shortcuts");
		if (item == null) {
			menuManager.add(new GroupMarker("group.shortcuts"));
		}

		Map<String, List<LaunchShortcutExtension>> configTypeIdToShortcut = new HashMap<String, List<LaunchShortcutExtension>>();
		for (LaunchShortcutExtension extension : filteredList) {
			for (@SuppressWarnings("rawtypes") Iterator localIterator2 = extension.getAssociatedConfigurationTypes().iterator(); localIterator2.hasNext();) {
				Object configTypeId = localIterator2.next();

				List<LaunchShortcutExtension> list = configTypeIdToShortcut.get(configTypeId);
				if (list == null) {
					list = new ArrayList<LaunchShortcutExtension>();
					list.add(extension);
				} else {
					list.add(extension);
				}
				configTypeIdToShortcut.put((String) configTypeId, list);
			}
		}
		

		Map<String, Object> mapping = new TreeMap<String, Object>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		
		for (Map.Entry<String, List<LaunchShortcutExtension>> entry : configTypeIdToShortcut.entrySet()) {
			List<LaunchShortcutExtension> extensions = entry.getValue();
			if (extensions.size() == 1) {
				LaunchShortcutExtension extension = (LaunchShortcutExtension) extensions
						.get(0);
				mapping.put(extension.getLabel(), new LaunchShortcutContributionItem(extension));
			} else {
				String configTypeId = (String) entry.getKey();

				String name = DebugPlugin.getDefault().getLaunchManager()
						.getLaunchConfigurationType(configTypeId).getName();
				mapping.put(name, new MultipleLaunchShortcutsContributionItem(
						name, configTypeId, extensions));
			}

		}

		for (Map.Entry<String, Object> entry : mapping.entrySet()) {
			menuManager.appendToGroup("group.shortcuts", (IContributionItem) entry.getValue());
		}
		
	}
	
	private List<LaunchShortcutExtension> getShortcutsMatchingMode(){
	
		List<LaunchShortcutExtension> filtered = new ArrayList<LaunchShortcutExtension>();
		
		for (int i=0 ; i< fShortcuts.size() ; i++){
			String id = fShortcuts.get(i).getId();
			if (id.contains("com.appcelerator.titanium.mobile.ui")
				&& !id.contains("package")
				&& !id.contains("module")){
				filtered.add(fShortcuts.get(i));
			}
		}
		
		return filtered;
		
	}

	protected void finish(LaunchShortcutExtension extension) {
		getToolbar().getParent().getParent().dispose();
	}

public static Composite getToolbar() {
		return toolbar;
	}

	public static void setToolbar(Composite toolbar) {
		TiLaunchShortcuts.toolbar = toolbar;
	}

private class MultipleLaunchShortcutsContributionItem extends
			ContributionItem {
		private final String configTypeId;
		private final List<LaunchShortcutExtension> extensions;
		private final String label;

		MultipleLaunchShortcutsContributionItem(String label,
				String configTypeId, List<LaunchShortcutExtension> extension) {
			this.label = label;
			this.configTypeId = configTypeId;
			this.extensions = extension;
		}

		public void fill(Menu menu, int index) {
			ILaunchConfigurationType type = DebugPlugin.getDefault()
					.getLaunchManager()
					.getLaunchConfigurationType(this.configTypeId);
			MenuItem menuItem = new MenuItem(menu, 64);
			menuItem.setText(this.label);
			menuItem.setEnabled(true);
			menuItem.setImage(DebugUIPlugin.getDefaultLabelProvider().getImage(
					type));
			Menu subMenu = new Menu(menuItem);
			int index2 = 0;
			for (LaunchShortcutExtension extension : this.extensions) {
				new LaunchShortcutContributionItem(extension).fill(
						subMenu, index2++);
			}
			menuItem.setMenu(subMenu);
		}
	}

	private class LaunchShortcutContributionItem extends ContributionItem {
		private final LaunchShortcutExtension extension;

		LaunchShortcutContributionItem(
				LaunchShortcutExtension paramLaunchShortcutExtension) {
			this.extension = paramLaunchShortcutExtension;
		}

		public void fill(Menu menu, int index) {
			MenuItem menuItem = new MenuItem(menu, 32);
			menuItem.setText(this.extension.getLabel());
			menuItem.setEnabled(true);
			menuItem.setImage(this.extension.getImageDescriptor().createImage());
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent me) {
					
					//boolean indicating if there are more than one appified projects for the selected project;
					boolean moreThanOneAppified = false;
					
					Shell dialogShell = Display.getCurrent().getActiveShell();
					
					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
					ISelectionService selectionService = Workbench.getInstance().getActiveWorkbenchWindow().getSelectionService();

					ISelection selection = selectionService.getSelection();

					
					IProject selectedProject = null;
					if(selection instanceof IStructuredSelection) {
					    Object element = ((IStructuredSelection)selection).getFirstElement();
					    if (element instanceof IResource) {
					        selectedProject = ((IResource)element).getProject();
					    } 
					}
					
					IProject appifiedProject = null;
					int appifiedCount = 0;
					String baseProjectName = null;
					
					if (LaunchUtils.isTiModule(selectedProject)) {
						baseProjectName = LaunchUtils.getBaseProjectName(selectedProject, projects);
					} else {
						baseProjectName = selectedProject.getName();
					}
					
					try {
						for (IProject project : projects) {
							if ((project.getDescription().getReferencedProjects() != null) && (project.getDescription().getReferencedProjects().length > 0)) {
								if (baseProjectName == project.getDescription().getReferencedProjects()[0].getName()) {
									if (LaunchUtils.isTiApp(project)) {
										appifiedCount+=1;
										appifiedProject = project;
									}
								}
							}
						}
					} catch (CoreException e1) {
							e1.printStackTrace();
					}
					
					if (appifiedCount > 1) {
						moreThanOneAppified = true;
					}
					
					if (moreThanOneAppified) {
						ElementTreeSelectionDialog dialog = createProjectsDialog(dialogShell);
						if(dialog.getReturnCode() == 0){ //0 is OK
							launchSelectedProject(dialog);
							finish(TiLaunchShortcuts.LaunchShortcutContributionItem.this.extension);
						} else {
							finish(TiLaunchShortcuts.LaunchShortcutContributionItem.this.extension);
						}
					} else {

						ISelection fSelection = new StructuredSelection(appifiedProject);
						TiLaunchShortcuts.LaunchShortcutContributionItem.this.extension.launch(fSelection, ILaunchManager.RUN_MODE);

						finish(TiLaunchShortcuts.LaunchShortcutContributionItem.this.extension);
					}
				}

				protected void launchSelectedProject(ElementTreeSelectionDialog dialog) {
						
					IProject tiShadowProjectSelected;
					tiShadowProjectSelected = (IProject)dialog.getFirstResult();
					ISelection fSelection = new StructuredSelection(tiShadowProjectSelected);
					
					TiLaunchShortcuts.LaunchShortcutContributionItem.this.extension.launch(fSelection, ILaunchManager.RUN_MODE);
				}

				protected ElementTreeSelectionDialog createProjectsDialog(Shell dialogShell) {
						
					LaunchShortcut ls = new LaunchShortcut();
					ArrayList<IProject> tiShadowProjectsList;
    				tiShadowProjectsList = ls.getTiShadowProjectsList();
					ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(dialogShell, new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider() {
			    		@SuppressWarnings("unchecked")
						@Override
			    		public Object[] getElements(Object element) {
			    			return ((ArrayList<IProject>)element).toArray();
			    		}
			    		@Override
			    		public Object[] getChildren(Object element) {
			    			return new Object[0];
			    		}
			    	});
			    	dialog.setTitle("Project Selection");
			    	dialog.setMessage("To run tests on a Titanium module, you need a Tishadow app to run on. Please select one from the list:");
			    	dialog.setInput(tiShadowProjectsList);
			    	dialog.open();
					return dialog;
				}
			});
		}
	}
}
