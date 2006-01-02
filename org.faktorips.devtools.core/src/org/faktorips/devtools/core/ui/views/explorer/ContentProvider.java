package org.faktorips.devtools.core.ui.views.explorer;

import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DefaultTreeContentProvider;


/**
 *
 */
public class ContentProvider extends DefaultTreeContentProvider {

    /* package */ ContentProvider() {
        super();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        try {
            return IpsPlugin.getDefault().getIpsModel().getIpsProjects();    
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return new Object[0]; 
        }
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to do
    }

}
