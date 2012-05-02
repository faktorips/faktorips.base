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

package org.faktorips.devtools.core.ui.dialogs;

import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.ui.controls.tableedit.IElementModifier;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;

/**
 * Allows to access and modify {@link SingleValueHolder} instances.
 * 
 * @author Stefan Widmaier
 */
public class MultiValueElementModifier implements IElementModifier {

    /**
     * Assumes the given element is a {@link SingleValueHolder}. Returns its string value.
     * {@inheritDoc}
     */
    @Override
    public String getValue(Object element) {
        SingleValueViewItem item = (SingleValueViewItem)element;
        return item.getSingleValueHolder().getStringValue();
    }

    /**
     * Assumes the given element is a {@link SingleValueHolder} and the given value is a
     * {@link String}. Sets the given string as new value of the given element. {@inheritDoc}
     */
    @Override
    public void setValue(Object element, Object value) {
        SingleValueViewItem item = (SingleValueViewItem)element;
        item.getSingleValueHolder().setValue((String)value);
    }

}