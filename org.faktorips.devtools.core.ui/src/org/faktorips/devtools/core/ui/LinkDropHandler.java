/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

public class LinkDropHandler implements IIpsDropListenerProvider {

    @Override
    public List<Transfer> getSupportedTransferTypes() {
        ArrayList<Transfer> result = new ArrayList<Transfer>();
        result.add(FileTransfer.getInstance());
        result.add(TextTransfer.getInstance());
        return result;
    }

    @Override
    public int getSupportedOperations() {
        return DND.DROP_LINK;
    }

    @Override
    public IIpsDropListener getDropListener(Viewer viewer) {
        return new LinkDropListener(viewer);
    }

}
