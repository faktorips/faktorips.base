/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;

public class ReferenceDeleteAction extends Action {

    private final TreeViewer viewer;

    public ReferenceDeleteAction(TreeViewer viewer) {
        this.viewer = viewer;
        viewer.addSelectionChangedListener(event -> {
            if (event.getSelection() instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                IProductCmptLink[] selectedLinks = getSelectedLinks(selection, new ArrayList<IIpsSrcFile>());
                setEnabled(selectedLinks != null);
            }
        });
    }

    @Override
    public void run() {
        if (viewer.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
            List<IIpsSrcFile> srcFilesToSave = new ArrayList<>();
            IProductCmptLink[] links = getSelectedLinks(selection, srcFilesToSave);
            if (links == null) {
                return;
            }
            for (IProductCmptLink link : links) {
                link.delete();
            }
            IpsException exception = null;
            for (IIpsSrcFile srcFile : srcFilesToSave) {
                try {
                    srcFile.save(false, null);
                } catch (IpsException e) {
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
