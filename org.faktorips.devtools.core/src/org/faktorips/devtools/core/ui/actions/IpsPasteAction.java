package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ResourceTransfer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPartState;
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
        	Object s2 = clipboard.getContents(TextTransfer.getInstance());
        	System.out.println(stored + " / " + s2);
        }
    }
}
