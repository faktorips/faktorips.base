/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * Label provider for the table viewer of a {@link EditTableControlViewer}. Uses a
 * {@link DatatypeEditingSupport} to format each value depending on datatype and current locale.
 * 
 * @author Stefan Widmaier
 */
public class DatatypeEditingSupportLabelProvider extends LabelProvider {

    private final DatatypeEditingSupport editingSupport;

    public DatatypeEditingSupportLabelProvider(DatatypeEditingSupport editingSupport) {
        this.editingSupport = editingSupport;

    }

    @Override
    public String getText(Object element) {
        return editingSupport.getFormattedValue(element);
    }
}
