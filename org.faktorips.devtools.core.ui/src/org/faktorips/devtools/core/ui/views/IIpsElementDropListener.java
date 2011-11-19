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

package org.faktorips.devtools.core.ui.views;

import java.util.List;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Interface for {@link DropTargetListener} capable to provide {@link IIpsElement} from the dragged
 * data.
 * 
 * @author Thorsten Günther
 */
public interface IIpsElementDropListener extends DropTargetListener {

    /**
     * Returns all {@link IIpsElement} items transfered during this drag.
     * <p>
     * It might be the case that calling this method causes the implementation to first create the
     * {@link IIpsElement} items (for example during a drag from another product system).
     */
    public List<IIpsElement> getDraggedElements(TransferData transferData);

    /**
     * Returns all the supported {@link Transfer} types.
     */
    public Transfer[] getSupportedTransfers();

    /**
     * @return The bitwise OR'ing of allowed operations; this may be a combination of any of
     *         {@link DND#DROP_NONE}, {@link DND#DROP_COPY}, {@link DND#DROP_MOVE},
     *         {@link DND#DROP_LINK}
     */
    public int getSupportedOperations();

}
