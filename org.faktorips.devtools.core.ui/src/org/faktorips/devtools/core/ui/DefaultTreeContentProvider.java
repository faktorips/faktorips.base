/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;

public abstract class DefaultTreeContentProvider implements ITreeContentProvider {

    public DefaultTreeContentProvider() {
        super();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (!(parentElement instanceof IIpsElement)) {
            return new Object[0];
        }
        try {
            return ((IIpsElement)parentElement).getChildren();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return new Object[0];
        }
    }

    @Override
    public Object getParent(Object element) {
        return ((IIpsElement)element).getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        try {
            return ((IIpsElement)element).hasChildren();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

}
