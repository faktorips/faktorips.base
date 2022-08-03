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

import java.util.List;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.model.IIpsElement;

/**
 * Interface for {@link DropTargetListener} capable to provide {@link IIpsElement} from the dragged
 * data.
 */
public interface IIpsElementDropListener extends DropTargetListener {

    /**
     * Returns all {@link IIpsElement} items transfered during this drag.
     * <p>
     * It might be the case that calling this method causes the implementation to first create the
     * {@link IIpsElement} items (for example during a drag from another product system).
     */
    List<IIpsElement> getDraggedElements(TransferData transferData);

    /**
     * Returns all the supported {@link Transfer} types.
     */
    Transfer[] getSupportedTransfers();

    /**
     * @return The bitwise OR'ing of allowed operations; this may be a combination of any of
     *             {@link DND#DROP_NONE}, {@link DND#DROP_COPY}, {@link DND#DROP_MOVE},
     *             {@link DND#DROP_LINK}
     */
    int getSupportedOperations();

}
