/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.outline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;

public class OutlinePage extends ContentOutlinePage implements IIpsSrcFilesChangeListener {

    private final IIpsSrcFile ipsSrcFile;

    public OutlinePage(IIpsSrcFile ipsSrcFile) {
        this.ipsSrcFile = ipsSrcFile;
        IpsPlugin.getDefault().getIpsModel().addIpsSrcFilesChangedListener(this);
    }

    @Override
    public void createControl(Composite gParent) {
        super.createControl(gParent);
        final TreeViewer treeView = super.getTreeViewer();
        treeView.setContentProvider(new OutlineContentProvider());
        treeView.setLabelProvider(new WorkbenchLabelProvider());
        treeView.setInput(ipsSrcFile);
        treeView.expandAll();
        treeView.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    new OpenEditorAction(treeView).run((IStructuredSelection)selection);
                    return;
                }
            }
        });
    }

    @Override
    public void ipsSrcFilesChanged(final IpsSrcFilesChangedEvent event) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (getTreeViewer() != null && !getTreeViewer().getTree().isDisposed()
                        && event.getChangedIpsSrcFiles().contains(ipsSrcFile)) {
                    getTreeViewer().refresh();
                }
            }
        });
    }

    private class OutlineContentProvider extends WorkbenchContentProvider {

        @Override
        public Object[] getChildren(Object element) {
            ArrayList<Object> result = new ArrayList<Object>(Arrays.asList(super.getChildren(element)));
            for (Iterator<Object> iter = result.iterator(); iter.hasNext();) {
                Object o = iter.next();
                if (o instanceof IIpsElement) {
                    IIpsElement ipsElement = (IIpsElement)o;
                    if (ipsElement.getName().isEmpty()) {
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            }
            return result.toArray();
        }

    }

}
