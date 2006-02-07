package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ResourceTransfer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPartState;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;


public class IpsPasteAction extends IpsAction {

    private Clipboard clipboard;
    
    public IpsPasteAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        clipboard = new Clipboard(shell.getDisplay());
    }

    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        if (selected instanceof IIpsObjectPartContainer) {

            String stored = (String)clipboard.getContents(TextTransfer.getInstance());

            try {
                IpsObjectPartState state = new IpsObjectPartState(stored);
                state.newPart((IIpsObjectPartContainer)selected);
            } catch (RuntimeException e) {
                IpsPlugin.log(e);
            }
        }
        else if (selected instanceof IIpsPackageFragmentRoot || selected instanceof IIpsPackageFragment) {
        	Object stored = clipboard.getContents(ResourceTransfer.getInstance());
        	if (stored instanceof IResource[]) {
				IResource[] res = (IResource[]) stored;
				for (int i = 0; i < res.length; i++) {
					try {
						
						// TODO resolve name-collisions by asking user for new name
						
						IPath parent = ((IIpsElement)selected).getCorrespondingResource().getFullPath();
						if (parent != null) {
							
							res[i].copy(parent.append(res[i].getName()), true, null);
						}
					} catch (CoreException e) {
						IpsPlugin.logAndShowErrorDialog(e);
					}
				}
			}
        }
    }
}
