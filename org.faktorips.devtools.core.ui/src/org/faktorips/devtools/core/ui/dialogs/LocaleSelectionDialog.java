/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Dialog that enables the user to select a {@link Locale} from a list of available locales.
 * 
 * @author Alexander Weickmann
 */
public class LocaleSelectionDialog extends ElementListSelectionDialog {

    public LocaleSelectionDialog(Shell parent, Set<Locale> excludedLocales) {
        super(parent, new LocaleLabelProvider());

        setTitle(Messages.LocaleSelectionDialog_title);
        setMessage(Messages.LocaleSelectionDialog_message);

        String[] isoLanguageCodes = Locale.getISOLanguages();
        List<Locale> localeList = new ArrayList<>(isoLanguageCodes.length);
        for (String code : isoLanguageCodes) {
            Locale locale = new Locale(code);
            if (!(excludedLocales.contains(locale))) {
                localeList.add(locale);
            }
        }
        setElements(localeList.toArray());
    }

    @Override
    public Locale getFirstResult() {
        return (Locale)super.getFirstResult();
    }

    private static class LocaleLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            Locale locale = (Locale)element;
            return locale.getDisplayLanguage();
        }

    }

}
