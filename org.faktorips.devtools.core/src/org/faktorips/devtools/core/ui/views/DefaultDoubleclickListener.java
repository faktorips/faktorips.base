package org.faktorips.devtools.core.ui.views;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.DummyRoot;

public class DefaultDoubleclickListener implements IDoubleClickListener {

    private TreeViewer tree;
    
    public DefaultDoubleclickListener(TreeViewer tree) {
        this.tree = tree;
    }
    
    public void doubleClick(DoubleClickEvent event) {
        if (event.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection sel = (IStructuredSelection)event.getSelection();
            Object obj = sel.getFirstElement();
            if (obj instanceof IIpsPackageFragment) {
                List list = Arrays.asList(tree.getVisibleExpandedElements());
                if (list.contains(obj)) {
                    tree.collapseToLevel(obj, 1);
                }
                else {
                    tree.expandToLevel(obj, 1);
                }
            }
            else if (obj instanceof DummyRoot) {
                openEditor(((DummyRoot)obj).data);
            }
            else if (obj instanceof IIpsElement) {
                openEditor((IIpsElement)obj);
            }

        }
    }

    private void openEditor(IIpsElement e) {
        for(; e != null && !(e instanceof IIpsSrcFile); e = e.getParent());
        try {
            if (e != null) {
                IpsPlugin.getDefault().openEditor((IIpsSrcFile)e);
            }
        } catch (PartInitException e1) {
            IpsPlugin.logAndShowErrorDialog(e1);
        }
    }

}
