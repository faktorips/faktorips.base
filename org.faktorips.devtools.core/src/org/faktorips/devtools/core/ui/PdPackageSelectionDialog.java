package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.core.model.IIpsPackageFragment;


/**
 *
 */
public class PdPackageSelectionDialog extends ElementListSelectionDialog {

    /**
     * @param parent
     * @param renderer
     */
    public PdPackageSelectionDialog(Shell parent) {
        super(parent, new DefaultLabelProvider());
        setTitle("Select Package");
        setMessage("Select a package (?=any character, *=any string)");
        setIgnoreCase(true);
        setMatchEmptyString(true);
        setMultipleSelection(false);
    }
    
    public IIpsPackageFragment getSelectedPackage() {
        if (getResult().length>0) {
            return (IIpsPackageFragment)getResult()[0];    
        }
        return null;
    }
    
}
