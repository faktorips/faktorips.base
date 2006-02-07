package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.internal.model.IpsObjectPartState;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 * Copy of objects controlled by FaktorIps. 
 * 
 * @author Thorsten Guenther
 */
public class IpsCopyAction extends IpsAction {

    private Clipboard clipboard;
    
    public IpsCopyAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        clipboard = new Clipboard(shell.getDisplay());
    }

    public void run(IStructuredSelection selection) {
        List selectedObjects = selection.toList();

        List copiedObjects = new ArrayList();
        List copiedResources = new ArrayList();
        IIpsObjectPart part;
        for (Iterator iter = selectedObjects.iterator(); iter.hasNext();) {
            Object selected = iter.next();

            if (selected instanceof IIpsObjectPart) {
                part = (IIpsObjectPart)selected;
                copiedObjects.add(new IpsObjectPartState(part).toString());
            }
            else if (selected instanceof IIpsElement) {
            	
            	IResource resource = ((IIpsElement)selected).getEnclosingResource();
            	if (resource != null) {
            		copiedResources.add(resource);
            	}
            }
        }

        if (copiedObjects.size() > 0 || copiedResources.size() > 0) {
            clipboard.setContents(getDataArray(copiedObjects, copiedResources), getTypeArray(copiedObjects, copiedResources));
        }
    }
}
