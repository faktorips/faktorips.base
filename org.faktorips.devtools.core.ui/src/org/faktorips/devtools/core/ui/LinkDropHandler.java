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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

public class LinkDropHandler implements IIpsDropAdapterProvider {

    @Override
    public List<Transfer> getSupportedTransferTypes() {
        ArrayList<Transfer> result = new ArrayList<>();
        result.add(FileTransfer.getInstance());
        result.add(TextTransfer.getInstance());
        return result;
    }

    @Override
    public int getSupportedOperations() {
        return DND.DROP_LINK;
    }

    @Override
    public IpsViewerDropAdapter getDropAdapter(Viewer viewer) {
        return new LinkDropListener(viewer);
    }

}
