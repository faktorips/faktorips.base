/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.outline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

public class OutlinePage extends ContentOutlinePage {

    private final IIpsSrcFile ipsSrcFile;

    public OutlinePage(IIpsSrcFile ipsSrcFile) {
        this.ipsSrcFile = ipsSrcFile;
    }

    @Override
    public void createControl(Composite gParent) {
        super.createControl(gParent);
        TreeViewer treeView = super.getTreeViewer();
        treeView.setContentProvider(new OutlineContentProvider());
        treeView.setLabelProvider(new WorkbenchLabelProvider());
        treeView.setInput(ipsSrcFile);
        treeView.expandAll();
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
