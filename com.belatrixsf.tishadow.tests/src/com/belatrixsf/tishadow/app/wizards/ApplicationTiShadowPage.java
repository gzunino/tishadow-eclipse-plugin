/**
 * 
 */
package com.belatrixsf.tishadow.app.wizards;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author vvillegas
 * 
 */
public class ApplicationTiShadowPage extends TiShadowPage{

	/**Constructor*/
	protected ApplicationTiShadowPage(String pageName) {
		super(pageName);
		setPageComplete(false);
	}
	
	@Override
	void addExtraFields() {
		// TODO Auto-generated method stub
		
	}

	@Override
	String getWorkingDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
