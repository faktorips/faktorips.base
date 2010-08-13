/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A composite that allows to edit the {@link IDescription}s attached to an
 * {@link IDescribedElement}. It consists of a drop down box and a text area. The drop down box
 * allows to select the language of the description to edit in the text area.
 * 
 * @since 3.1
 * 
 * @author Alexander Weickmann
 */
public class DescriptionEditComposite extends Composite {

    private final IDescribedElement describedElement;

    private final UIToolkit uiToolkit;

    private final Combo languageCombo;

    private final Text textArea;

    private final Set<ISupportedLanguage> supportedLanguages;

    /** A map that associates language names to their language codes. */
    private final Map<String, String> languageCodes;

    public DescriptionEditComposite(Composite parent, IDescribedElement describedElement, IIpsProject ipsProject,
            UIToolkit uiToolkit) {

        super(parent, SWT.NONE);

        languageCodes = new HashMap<String, String>();

        this.uiToolkit = uiToolkit;
        this.describedElement = describedElement;

        supportedLanguages = ipsProject.getProperties().getSupportedLanguages();
        for (ISupportedLanguage language : supportedLanguages) {
            Locale locale = language.getLocale();
            if (locale == null) {
                continue;
            }
            languageCodes.put(language.getLanguageName(), locale.getLanguage());
        }

        createLayout();
        languageCombo = createLanguageCombo();
        textArea = createTextArea();

        updateTextArea();
    }

    private void createLayout() {
        Layout layout = new GridLayout();
        setLayout(layout);
        setLayoutData(new GridData(1, 2));
    }

    private Combo createLanguageCombo() {
        Combo combo = uiToolkit.createCombo(this);

        int defaultIndex = -1;
        for (int i = 0; i < supportedLanguages.size(); i++) {
            ISupportedLanguage language = supportedLanguages.toArray(new ISupportedLanguage[supportedLanguages.size()])[i];
            String languageName = language.getLanguageName();
            String languageCode = languageCodes.get(languageName);
            if (languageCode != null) {
                combo.add(languageName);
            }
            if (language.isDefaultLanguage()) {
                defaultIndex = i;
            }
        }

        // Preselect the default language if available
        if (defaultIndex != -1) {
            combo.select(defaultIndex);
        }

        return combo;
    }

    private Text createTextArea() {
        return uiToolkit.createMultilineText(this);
    }

    private void updateTextArea() {
        int currentSelectionIndex = languageCombo.getSelectionIndex();
        if (currentSelectionIndex == -1) {
            textArea.setEnabled(false);
            return;
        }

        if (!(textArea.isEnabled())) {
            textArea.setEnabled(true);
        }

        String currentLanguage = languageCombo.getItem(currentSelectionIndex);
        String currentLanguageCode = languageCodes.get(currentLanguage);
        Locale currentLocale = new Locale(currentLanguageCode);

        IDescription description = describedElement.getDescription(currentLocale);
        if (description == null) {
            return;
        }

        textArea.setText(description.getText());
    }

}
