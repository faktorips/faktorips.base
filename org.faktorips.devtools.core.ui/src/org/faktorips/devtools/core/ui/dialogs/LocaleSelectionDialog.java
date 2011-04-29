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
        List<Locale> localeList = new ArrayList<Locale>(isoLanguageCodes.length);
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
