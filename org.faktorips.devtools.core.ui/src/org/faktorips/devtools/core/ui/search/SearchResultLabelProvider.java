/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class SearchResultLabelProvider implements ILabelProvider {

    private List listeners = new ArrayList();

    /**
     * Overridden
     */
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    /**
     * Overridden
     */
    public void dispose() {
        listeners = null;
    }

    /**
     * Overridden
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * Overridden
     */
    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }

    /**
     * Overridden
     */
    public Image getImage(Object element) {
        if (element instanceof Object[]) {
            return IpsUIPlugin.getImageHandling().getImage((IIpsElement)((Object[])element)[0]);
        } else {
            return IpsUIPlugin.getImageHandling().getImage((IIpsElement)element);
        }
    }

    /**
     * Overridden
     */
    public String getText(Object element) {
        if (element instanceof Object[]) {
            return ((IIpsElement)((Object[])element)[0]).getName();
        } else {
            return ((IIpsElement)element).getName();
        }
    }

}
