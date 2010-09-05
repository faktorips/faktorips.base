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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
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

    /** A map that associates language names to their language codes. */
    private final Map<String, String> languageCodes;

    /** The description that is currently being edited. */
    private IDescription currentDescription;

    private boolean viewOnly;

    public DescriptionEditComposite(Composite parent, IDescribedElement describedElement, UIToolkit uiToolkit) {
        super(parent, SWT.NONE);

        this.uiToolkit = uiToolkit;
        this.describedElement = describedElement;

        languageCodes = new HashMap<String, String>();
        for (IDescription description : describedElement.getDescriptions()) {
            Locale locale = description.getLocale();
            if (locale != null) {
                languageCodes.put(locale.getDisplayLanguage(), locale.getLanguage());
            }
        }

        createLayout();
        languageCombo = createLanguageCombo();
        textArea = createTextArea();

        refresh();
    }

    public void refresh() {
        updateCurrentDescription();
        updateTextArea();
    }

    private void createLayout() {
        Layout layout = new GridLayout(1, true);
        setLayout(layout);
        setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    private Combo createLanguageCombo() {
        Combo combo = uiToolkit.createCombo(this);

        int defaultIndex = -1;
        int i = 0;
        for (String languageName : languageCodes.keySet()) {
            String languageCode = languageCodes.get(languageName);
            combo.add(languageName + " (" + languageCode + ")"); //$NON-NLS-1$ //$NON-NLS-2$

            IIpsProjectProperties properties = describedElement.getIpsProject().getProperties();
            ISupportedLanguage language = properties.getSupportedLanguage(new Locale(languageCode));
            if (language != null) {
                if (language.isDefaultLanguage()) {
                    defaultIndex = i;
                }
            }
            i++;
        }

        // Preselect the default language if available
        if (defaultIndex != -1) {
            combo.select(defaultIndex);
        }

        combo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                refresh();
            }

        });

        return combo;
    }

    private Text createTextArea() {
        final Text text = uiToolkit.createMultilineText(this);
        text.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (currentDescription != null) {
                    currentDescription.setText(text.getText());
                }
            }

        });
        return text;
    }

    private void updateCurrentDescription() {
        int currentSelectionIndex = languageCombo.getSelectionIndex();
        if (currentSelectionIndex == -1) {
            currentDescription = null;
            return;
        }

        String currentLanguage = languageCombo.getItem(currentSelectionIndex);
        currentLanguage = currentLanguage.substring(0, currentLanguage.indexOf(" (")); //$NON-NLS-1$
        String currentLanguageCode = languageCodes.get(currentLanguage);
        Locale currentLocale = new Locale(currentLanguageCode);
        currentDescription = describedElement.getDescription(currentLocale);
    }

    private void updateTextArea() {
        if (currentDescription == null) {
            textArea.setEnabled(false);
            return;
        }
        if (!(viewOnly)) {
            textArea.setEnabled(true);
        }
        textArea.setText(currentDescription.getText());
    }

    void setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
        textArea.setEnabled(!(viewOnly));
    }

}
