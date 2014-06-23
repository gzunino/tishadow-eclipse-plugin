/**
 * 
 */
package com.belatrixsf.tishadow.app.wizards;

import org.eclipse.jface.resource.ImageDescriptor;

public class ApplicationTiShadowPage extends AbstractTiShadowPage{

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
