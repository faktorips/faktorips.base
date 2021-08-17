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

import org.eclipse.gef.dnd.SimpleObjectTransfer;
import org.eclipse.jface.viewers.TableViewer;

public class SubsetChooserTransfer extends SimpleObjectTransfer {

    private static final SubsetChooserTransfer INSTANCE = new SubsetChooserTransfer();
    private static final String TYPE_NAME = "Subset Chooser Transfer" //$NON-NLS-1$
            + System.currentTimeMillis() + ":" + INSTANCE.hashCode(); //$NON-NLS-1$
    private static final int TYPEID = registerType(TYPE_NAME);

    private AbstractSubsetChooserModel model;
    private TableViewer viewer;

    public static SubsetChooserTransfer getInstance() {
        return INSTANCE;
    }

    @Override
    protected int[] getTypeIds() {
        return new int[] { TYPEID };
    }

    /**
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[] { TYPE_NAME };
    }

    public void setModel(AbstractSubsetChooserModel model) {
        this.model = model;
    }

    public AbstractSubsetChooserModel getModel() {
        return this.model;
    }

    public TableViewer getViewer() {
        return viewer;
    }

    public void setViewer(TableViewer viewer) {
        this.viewer = viewer;
    }

}
