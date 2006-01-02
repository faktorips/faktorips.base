package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.PdSourceRootSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * A control to edit a reference to a package fragment root containing source
 * code.
 */
public class IpsPckFragmentRootRefControl extends TextButtonControl {
    
    // true if only package fragment roots contains sourcecode are allowed. 
    private boolean onlySourceRoots;

    public IpsPckFragmentRootRefControl(
            Composite parent, 
            boolean onlySourceRoots,
            UIToolkit toolkit) {
        super(parent, toolkit, "Browse");
        this.onlySourceRoots = onlySourceRoots;
    }
    
    public void setPdPckFragmentRoot(IIpsPackageFragmentRoot root) {
        if (root==null) {
            setText("");
        } else {
            String newText = root.getCorrespondingResource().getFullPath().toString().substring(1); 
            setText(newText);
        }
    }
    
    public IIpsPackageFragmentRoot getPdPckFragmentRoot() {
        IWorkspaceRoot wpRoot = ResourcesPlugin.getWorkspace().getRoot();
        String pathString = IPath.SEPARATOR + getText();
        try {
            Path path = new Path(pathString);
            IFolder folder = wpRoot.getFolder(path);
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(folder);
            if (element instanceof IIpsPackageFragmentRoot) {
                return (IIpsPackageFragmentRoot)element;
            }
        } catch (IllegalArgumentException e) {
            // string is not a valid path
        }
        return null;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.TextButtonControl#buttonClicked()
     */ 
    protected void buttonClicked() {
        try {
            PdSourceRootSelectionDialog dialog = new PdSourceRootSelectionDialog(getShell(), onlySourceRoots);
            if (dialog.open()==Window.OK) {
                IIpsPackageFragmentRoot root = dialog.getSelectedRoot();
                setPdPckFragmentRoot(root);
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }


}
