/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.runtime.internal.IpsStringUtils;

public class OutlinePage extends ContentOutlinePage implements IIpsSrcFilesChangeListener {

    private final IIpsSrcFile ipsSrcFile;

    public OutlinePage(IIpsSrcFile ipsSrcFile) {
        this.ipsSrcFile = ipsSrcFile;
        IIpsModel.get().addIpsSrcFilesChangedListener(this);
    }

    @Override
    public void createControl(Composite gParent) {
        super.createControl(gParent);
        final TreeViewer treeView = super.getTreeViewer();
        treeView.setContentProvider(new OutlineContentProvider());
        treeView.setLabelProvider(new WorkbenchLabelProvider());
        treeView.setInput(ipsSrcFile);
        treeView.expandAll();
        treeView.addDoubleClickListener(event -> {
            ISelection selection = event.getSelection();
            if (selection instanceof IStructuredSelection) {
                new OpenEditorAction(treeView).run((IStructuredSelection)selection);
            }
        });
    }

    @Override
    public void ipsSrcFilesChanged(final IpsSrcFilesChangedEvent event) {
        Display.getDefault().asyncExec(() -> {
            if (getTreeViewer() != null && !getTreeViewer().getTree().isDisposed()
                    && event.getChangedIpsSrcFiles().contains(ipsSrcFile)) {
                getTreeViewer().refresh();
            }
        });
    }

    private class OutlineContentProvider extends WorkbenchContentProvider {

        @Override
        public Object[] getChildren(Object element) {
            ArrayList<Object> result = new ArrayList<>(Arrays.asList(super.getChildren(element)));

            for (Iterator<Object> iter = result.iterator(); iter.hasNext();) {
                Object o = iter.next();
                if (o instanceof IIpsElement ipsElement) {
                    if (IpsStringUtils.isBlank(ipsElement.getName())
                            || ipsElement instanceof IDescription
                            || ipsElement instanceof ILabel) {
                        iter.remove();
                        continue;
                    }
                } else {
                    iter.remove();
                }
            }
            return result.toArray();
        }
    }
}
