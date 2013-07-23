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

package org.faktorips.devtools.core.ui.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.values.LocalizedString;

public class InternationalStringTraversalStrategy extends LinkedColumnsTraversalStrategy<LocalizedString> {

    private final List<Locale> supportedLocales;
    private final int columnIndex;
    private final IInternationalString internationalString;

    public InternationalStringTraversalStrategy(CellTrackingEditingSupport<LocalizedString> editingSupport,
            IIpsProject ipsProject, int columnIndex, IInternationalString internationalString) {
        super(editingSupport);
        supportedLocales = new ArrayList<Locale>();
        this.columnIndex = columnIndex;
        for (ISupportedLanguage language : ipsProject.getReadOnlyProperties().getSupportedLanguages()) {
            supportedLocales.add(language.getLocale());
        }
        this.internationalString = internationalString;
    }

    @Override
    protected int getColumnIndex() {
        return columnIndex;
    }

    @Override
    protected boolean canEdit(LocalizedString currentViewItem) {
        return true;
    }

    @Override
    protected LocalizedString getPreviousVisibleViewItem(LocalizedString currentViewItem) {
        Locale currentLocale = currentViewItem.getLocale();
        for (int i = 0, n = supportedLocales.size(); i < n; i++) {
            if (supportedLocales.get(i).equals(currentLocale) && (i > 0)) {
                LocalizedString result = internationalString.get(supportedLocales.get(i - 1));
                return result;
            }
        }
        return null;
    }

    @Override
    protected LocalizedString getNextVisibleViewItem(LocalizedString currentViewItem) {
        Locale currentLocale = currentViewItem.getLocale();
        for (int i = 0, n = supportedLocales.size(); i < n; i++) {
            if (supportedLocales.get(i).equals(currentLocale) && (i < n - 1)) {
                LocalizedString result = internationalString.get(supportedLocales.get(i + 1));
                return result;
            }
        }
        return null;
    }

}
