package org.faktorips.devtools.core.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;


/**
 *
 */
public class PdObjectSelectionDialog extends TwoPaneElementSelector {

    /**
     * @param parent
     * @param elementRenderer
     * @param qualifierRenderer
     */
    public PdObjectSelectionDialog(Shell parent, String title, String message) {
        super(parent, new DefaultLabelProvider(), new QualifierLabelProvider());
        setTitle(title);
        setMessage(message);
        setUpperListLabel("Matches:");
        setLowerListLabel("Qualifier:");
        setIgnoreCase(true);
        setMatchEmptyString(true);
    }
    
    private static class QualifierLabelProvider extends LabelProvider {
        
        public Image getImage(Object element) {
            return ((IIpsObject)element).getIpsPackageFragment().getImage();
        }
        
        public String getText(Object element) {
            IIpsPackageFragment pck = ((IIpsObject)element).getIpsPackageFragment(); 
            return pck.getName()
            	+ " - " + pck.getEnclosingResource().getFullPath().toString();
        }
    }

}
