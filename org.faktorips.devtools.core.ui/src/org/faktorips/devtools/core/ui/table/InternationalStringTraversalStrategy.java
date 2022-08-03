/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.values.LocalizedString;

public class InternationalStringTraversalStrategy extends LinkedColumnsTraversalStrategy<LocalizedString> {

    private final List<Locale> supportedLocales;
    private final int columnIndex;
    private final IInternationalString internationalString;

    public InternationalStringTraversalStrategy(CellTrackingEditingSupport<LocalizedString> editingSupport,
            IIpsProject ipsProject, int columnIndex, IInternationalString internationalString) {
        super(editingSupport);
        supportedLocales = new ArrayList<>();
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
                return internationalString.get(supportedLocales.get(i - 1));
            }
        }
        return null;
    }

    @Override
    protected LocalizedString getNextVisibleViewItem(LocalizedString currentViewItem) {
        Locale currentLocale = currentViewItem.getLocale();
        for (int i = 0, n = supportedLocales.size(); i < n; i++) {
            if (supportedLocales.get(i).equals(currentLocale) && (i < n - 1)) {
                return internationalString.get(supportedLocales.get(i + 1));
            }
        }
        return null;
    }

}
