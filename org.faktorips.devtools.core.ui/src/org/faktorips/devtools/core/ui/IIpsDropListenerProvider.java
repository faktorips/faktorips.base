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

package org.faktorips.devtools.core.ui;

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;

/**
 * Provider of
 * 
 * @author guenther
 */
public interface IIpsDropListenerProvider {

    /**
     * @return The list of supported {@link Transfer} types.
     */
    public List<Transfer> getSupportedTransferTypes();

    /**
     * @return The bitwise OR'ing of allowed operations; this may be a combination of any of
     *         {@link DND#DROP_NONE}, {@link DND#DROP_COPY}, {@link DND#DROP_MOVE},
     *         {@link DND#DROP_LINK}
     */
    public int getSupportedOperations();

    /**
     * @return The adapter to handle a drop for the given viewer
     */
    public IIpsDropListener getDropListener(Viewer viewer);

}
