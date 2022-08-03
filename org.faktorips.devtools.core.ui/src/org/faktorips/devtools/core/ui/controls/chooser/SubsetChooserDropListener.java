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

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public class SubsetChooserDropListener extends ViewerDropAdapter {

    private ListChooserValue dropTarget;

    public SubsetChooserDropListener(Viewer viewer) {
        super(viewer);
    }

    @Override
    public boolean performDrop(Object data) {
        AbstractSubsetChooserModel model = SubsetChooserTransfer.getInstance().getModel();
        if (model == null) {
            return false;
        }

        Viewer sourceViewer = SubsetChooserTransfer.getInstance().getViewer();
        List<ListChooserValue> selectedValues = TypedSelection
                .createAtLeast(ListChooserValue.class, sourceViewer.getSelection(), 0).getElements();
        if (selectedValues == null || selectedValues.isEmpty()) {
            return false;
        }

        int index = model.getIndexOfResultingValue(dropTarget);
        boolean insertBelow = getCurrentLocation() == LOCATION_AFTER;
        boolean crossDrop = !getViewer().equals(SubsetChooserTransfer.getInstance().getViewer());

        if (crossDrop) {
            if (model.getResultingValues().contains(selectedValues.get(0))) {
                model.moveValuesFromResultingToPredefined(selectedValues);
                return true;
            } else {
                model.moveValuesFromPreDefinedToResulting(selectedValues);
            }
        }
        model.moveToPosition(selectedValues, index, insertBelow);
        return true;
    }

    @Override
    public boolean validateDrop(Object target, int operation, TransferData data) {
        if (target == null) {
            dropTarget = null;
        } else {
            dropTarget = (ListChooserValue)target;
        }

        int location = getCurrentLocation();
        if (location == LOCATION_ON) {
            return false;
        }

        return true;
    }
}
