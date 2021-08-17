/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.chooser;

import java.util.function.Supplier;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;

public class SubsetChooserDragSourceAdapter extends DragSourceAdapter {

    private TableViewer viewer;
    private Supplier<AbstractSubsetChooserModel> modelSupplier;

    public SubsetChooserDragSourceAdapter(TableViewer viewer, Supplier<AbstractSubsetChooserModel> modelSupplier) {
        this.viewer = viewer;
        this.modelSupplier = modelSupplier;
    }

    @Override
    public void dragStart(DragSourceEvent event) {
        event.doit = !viewer.getSelection().isEmpty();
        SubsetChooserTransfer.getInstance().setViewer(viewer);
        SubsetChooserTransfer.getInstance().setModel(modelSupplier.get());
    }

}
