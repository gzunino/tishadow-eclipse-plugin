package com.belatrixsf.tishadow.tests;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.belatrixsf.tishadow.LaunchUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin  implements IWorkbenchListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.belatrixsf.tishadow.tests"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		IWorkbench iwb = PlatformUI.getWorkbench();
		IWorkbenchListener wbl = new IWorkbenchListener (){

	        @Override
	        public void postShutdown(IWorkbench w) {
	        }

	        @Override
	        public boolean preShutdown(IWorkbench w, boolean b) {
	            boolean exitEclipse = true;
	            try {
					LaunchUtils.stopTiShadowServer();
				} catch (Exception e) {
					e.printStackTrace();
				}
	    		plugin = null;
	            return exitEclipse;
	        }
	    };
	    
	    iwb.addWorkbenchListener(wbl);
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		LaunchUtils.stopTiShadowServer();
		plugin = null;
		super.stop(context);
	}

	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public IPreferenceStore getPreferenceStore() {
		return super.getPreferenceStore();
	}

	@Override
	public boolean preShutdown(IWorkbench workbench, boolean forced) {
		return false;
	}

	@Override
	public void postShutdown(IWorkbench workbench) {
		
	}
}
