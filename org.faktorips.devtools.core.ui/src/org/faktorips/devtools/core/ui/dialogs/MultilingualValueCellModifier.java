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

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.model.IInternationalString;

class MultilingualValueCellModifier implements ICellModifier {
    private final IInternationalString internationalString;

    public MultilingualValueCellModifier(IInternationalString internationalString) {
        this.internationalString = internationalString;
    }

    @Override
    public boolean canModify(Object element, String property) {
        return Messages.InternationalValueDialog_valueColumnTitle.equals(property);
    }

    private int getColumnIndexForProperty(String property) {
        if (Messages.InternationalValueDialog_valueColumnTitle.equals(property)) {
            return 1;
        }
        return -1;
    }

    @Override
    public Object getValue(Object element, String property) {
        if (element instanceof LocalizedString) {
            LocalizedString locString = (LocalizedString)element;
            int colIndex = getColumnIndexForProperty(property);
            if (colIndex == 1) {
                return locString.getValue();
            }
        }
        return null;
    }

    @Override
    public void modify(Object element, String property, Object value) {
        int colIndex = getColumnIndexForProperty(property);
        if (colIndex > 0) {
            LocalizedString locString = null;
            if (element instanceof TableItem) {
                locString = (LocalizedString)((TableItem)element).getData();
            } else if (element instanceof LocalizedString) {
                locString = (LocalizedString)element;
            }
            if (colIndex == 1 && locString != null) {
                internationalString.add(new LocalizedString(locString.getLocale(), (String)value));
            }
        }
    }
}