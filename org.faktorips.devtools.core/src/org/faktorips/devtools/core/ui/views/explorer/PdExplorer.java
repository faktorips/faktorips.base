package org.faktorips.devtools.core.ui.views.explorer;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;



/**
 *
 */
public class PdExplorer extends ViewPart {
    
    public final static String EXTENSION_ID = IpsPlugin.PLUGIN_ID + ".pdexplorer";
    
    private TreeViewer viewer;

    /**
     * 
     */
    public PdExplorer() {
        super();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		parent.setLayout(layout);

		viewer = new TreeViewer(parent);
		//set this tree viewer as the selection provider of this workbench's site
		getSite().setSelectionProvider(viewer);
		
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new DefaultLabelProvider());
		viewer.setUseHashlookup(true);
		viewer.setAutoExpandLevel(3);
		viewer.setInput(IpsPlugin.getDefault().getIpsModel());
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(layoutData);
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        // nothing to do
    }

}
