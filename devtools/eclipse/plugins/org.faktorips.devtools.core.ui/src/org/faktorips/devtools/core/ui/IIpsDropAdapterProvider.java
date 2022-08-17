/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;

/**
 * Provides informations about and an adapter to handle drop events.
 * 
 * @author Thorsten Günther
 */
public interface IIpsDropAdapterProvider {

    /**
     * @return The list of supported {@link Transfer} types.
     */
    List<Transfer> getSupportedTransferTypes();

    /**
     * @return The bitwise OR'ing of allowed operations; this may be a combination of any of
     *             {@link DND#DROP_NONE}, {@link DND#DROP_COPY}, {@link DND#DROP_MOVE},
     *             {@link DND#DROP_LINK}
     */
    int getSupportedOperations();

    /**
     * @return The adapter to handle a drop for the given viewer
     */
    IpsViewerDropAdapter getDropAdapter(Viewer viewer);

}
