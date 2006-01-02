package org.faktorips.devtools.core;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.faktorips.devtools.core.ui.views.explorer.PdExplorer;


/**
 * Product definition PerspectiveFactory.
 * 
 * @author Jan Ortmann
 */
public class IpsPerspectiveFactory implements IPerspectiveFactory
{
	/**
	 * Constructor. 
	 */
	public IpsPerspectiveFactory()
	{
		super();
	}
	
	/**
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout)
	{
		String editorArea = layout.getEditorArea();
		
		IFolderLayout folder= layout.createFolder("left", IPageLayout.LEFT, (float)0.25, editorArea); //$NON-NLS-1$
		folder.addView(PdExplorer.EXTENSION_ID);
		folder.addPlaceholder(IPageLayout.ID_RES_NAV);
		
		IFolderLayout outputfolder= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
		outputfolder.addView(IPageLayout.ID_TASK_LIST);
		outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		
		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
				
		// new actions - Java project creation wizard
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
	}
}
