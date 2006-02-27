package org.faktorips.devtools.core.ui.controls;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.PdPackageSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * A control to edit a package fragment reference.
 */
public class IpsPckFragmentRefControl extends TextButtonControl {

    private IIpsPackageFragmentRoot root;
    
    public IpsPckFragmentRefControl(
            Composite parent, 
            UIToolkit toolkit) {
        super(parent, toolkit, Messages.IpsPckFragmentRefControl_titleBrowse);
    }
    
    public void setPdPckFragmentRoot(IIpsPackageFragmentRoot root) {
        this.root = root;
        setButtonEnabled(root!=null && root.exists());
    }

    public IIpsPackageFragment getPdPackageFragment() {
        if (root==null) {
            return null;
        }
        return root.getIpsPackageFragment(text.getText());
    }
    
    public void setPdPackageFragment(IIpsPackageFragment newPack) {
        if (newPack==null) {
            text.setText(""); //$NON-NLS-1$
        } else {
            setText(newPack.getName());
        }
    }
    

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.TextButtonControl#buttonClicked()
     */ 
    protected void buttonClicked() {
        try {
            PdPackageSelectionDialog dialog = new PdPackageSelectionDialog(getShell());
            if (root!=null) {
                dialog.setElements(root.getIpsPackageFragments());    
            }
            if (dialog.open()==Window.OK) {
                if (dialog.getSelectedPackage()!=null) {
                    setText(dialog.getSelectedPackage().getName());
                } else {
                    setText(""); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }


}
