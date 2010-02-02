/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class ReferenceDeleteAction extends Action {

    private final TreeViewer viewer;

    public ReferenceDeleteAction(TreeViewer viewer) {
        this.viewer = viewer;
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                if (event.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                    IProductCmptLink[] selectedLinks = getSelectedLinks(selection, new ArrayList<IIpsSrcFile>());
                    setEnabled(selectedLinks != null);
                }
            }
        });
    }

    @Override
    public void run() {
        if (viewer.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
            List<IIpsSrcFile> srcFilesToSave = new ArrayList<IIpsSrcFile>();
            IProductCmptLink[] links = getSelectedLinks(selection, srcFilesToSave);
            if (links == null) {
                return;
            }
            for (IProductCmptLink link : links) {
                link.delete();
            }
            CoreException exception = null;
            for (IIpsSrcFile srcFile : srcFilesToSave) {
                try {
                    srcFile.save(false, null);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                    exception = e;
                }
            }
            if (exception != null) {
                IpsPlugin.logAndShowErrorDialog(exception);
            }
        }
    }

    public IProductCmptLink[] getSelectedLinks(IStructuredSelection selection, List<IIpsSrcFile> srcFilesToSave) {
        Object[] selectedObject = selection.toArray();
        IProductCmptLink[] links = new IProductCmptLink[selectedObject.length];
        int i = 0;
        for (Object o : selectedObject) {
            // first check all objects if it has a link.
            // Only delete if only links are selected
            // and every src file is editable
            // --> all or nothing!
            if (o instanceof IProductCmptReference) {
                IProductCmptReference cmptReference = (IProductCmptReference)o;
                IProductCmptLink link = cmptReference.getLink();
                if (link != null && IpsUIPlugin.isEditable(link.getIpsSrcFile())) {
                    links[i] = link;
                    IIpsSrcFile srcFile = link.getIpsSrcFile();
                    if (!srcFile.isDirty()) {
                        srcFilesToSave.add(srcFile);
                    }
                    i++;
                    continue;
                }
            }
            return null;
        }
        return links;
    }

}
