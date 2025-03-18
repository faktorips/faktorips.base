/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.adapter.IIpsSrcFileWrapper;

/**
 * Standard drag listener for IPS elements in a structured viewer.
 */
public class IpsElementDragListener implements DragSourceListener {

    StructuredViewer dragSource;

    /**
     * Constructor for <code>IpsElementDragListener</code> needs a <code>StructuredViewer</code>
     *
     * @param dragSource The source you want to add drag support to; you want to drag from this
     *            structured viewer
     */
    public IpsElementDragListener(StructuredViewer dragSource) {
        this.dragSource = dragSource;
    }

    @Override
    public void dragStart(DragSourceEvent event) {
        event.doit = getFilenames((IStructuredSelection)dragSource.getSelection()).length > 0;
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
        event.data = getFilenames((IStructuredSelection)dragSource.getSelection());
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
        // Nothing to do.
    }

    /**
     * To get the filenames of the selected elements in the structured selection
     *
     * @param selection the selection you want to get the selected filenames from
     *
     * @return an array of string containing the filenames
     */
    public static String[] getFilenames(IStructuredSelection selection) {
        ArrayList<String> list = new ArrayList<>(selection.size());
        Iterator<?> iter = selection.iterator();
        while (iter.hasNext()) {
            Object selected = iter.next();
            if (selected instanceof Object[] objetcs) {
                for (Object objetc : objetcs) {
                    addSelectedObject(list, objetc);
                }
            } else {
                addSelectedObject(list, selected);
            }
        }

        return list.toArray(new String[list.size()]);
    }

    private static void addSelectedObject(List<String> list, Object selected) {
        switch (selected) {
            case IResource resource -> list.add(resource.getLocation().toOSString());
            case IIpsElement ipsElement when ipsElement.getEnclosingResource() != null -> list
                    .add(((IIpsElement)selected).getEnclosingResource().getLocation().toString());
            case IIpsSrcFileWrapper ipsSrcFileWrapper -> list
                    .add(ipsSrcFileWrapper.getWrappedIpsSrcFile().getEnclosingResource().getLocation().toString());
            default -> {
                // nothing to do
            }
        }
    }

}
