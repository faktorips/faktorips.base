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

package org.faktorips.devtools.core.ui.editors;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;

/**
 * A composite that allows to edit the {@link IDescription}s attached to an
 * {@link IDescribedElement}. It consists of a drop down box and a text area. The drop down box
 * allows to select the language of the description to edit in the text area.
 * 
 * @since 3.1
 * 
 * @author Alexander Weickmann
 */
public final class DescriptionEditComposite extends Composite {

    private final IDescribedElement describedElement;

    private final UIToolkit uiToolkit;

    private final Combo languageCombo;

    private final Text textArea;

    private final BindingContext bindingContext;

    /** A map that associates language names to their language codes. */
    private Map<String, String> languageCodes;

    private EditField<?> textEditField;

    /** The description that is currently being edited. */
    private IDescription currentDescription;

    private boolean viewOnly;

    public DescriptionEditComposite(Composite parent, IDescribedElement describedElement, UIToolkit uiToolkit) {
        super(parent, SWT.NONE);

        this.uiToolkit = uiToolkit;
        this.describedElement = describedElement;
        languageCodes = new LinkedHashMap<String, String>();
        bindingContext = new BindingContext();
        addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (bindingContext != null) {
                    bindingContext.dispose();
                }
            }
        });

        createLayout();

        languageCombo = createLanguageCombo();
        textArea = createTextArea();

        refresh();
        updateDescription();
    }

    private void createLayout() {
        Layout layout = new GridLayout(1, true);
        setLayout(layout);
        setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    private Combo createLanguageCombo() {
        Combo combo = uiToolkit.createCombo(this);
        combo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateDescription();
            }

        });

        return combo;
    }

    private Text createTextArea() {
        Text text = uiToolkit.createMultilineText(this);
        textEditField = new TextField(text);
        return text;
    }

    public void refresh() {
        if (isLanguageComboRefreshRequired()) {
            refreshLanguageCombo();
        }
    }

    /**
     * Checks whether the language combo needs to be refreshed. This has to be done because it might
     * happen that the editor is opened while the supported languages change.
     */
    private boolean isLanguageComboRefreshRequired() {
        Map<String, String> newLanguageCodes = new LinkedHashMap<String, String>();
        for (IDescription description : describedElement.getDescriptions()) {
            Locale locale = description.getLocale();
            if (locale != null) {
                newLanguageCodes.put(locale.getDisplayLanguage(), locale.getLanguage());
            }
        }
        if (!languageCodes.equals(newLanguageCodes)) {
            languageCodes = newLanguageCodes;
            return true;
        }
        return false;
    }

    /**
     * Ensures that the language combo box contains all languages the element has descriptions for.
     * The user's selection is preserved if possible (e.g. it's not possible if the selected
     * language has been removed). In case the selection could not be preserved, the default
     * description will be selected if existent. If not existent the first description will be
     * selected (if at least one exists).
     */
    private void refreshLanguageCombo() {
        int selectionIndex = languageCombo.getSelectionIndex();
        String selectedItem = null;
        if (selectionIndex != -1) {
            selectedItem = languageCombo.getItem(selectionIndex);
        }
        languageCombo.removeAll();
        for (String languageName : languageCodes.keySet()) {
            String languageCode = languageCodes.get(languageName);
            String item = languageName + " (" + languageCode + ")";//$NON-NLS-1$ //$NON-NLS-2$
            languageCombo.add(item);
            if (item.equals(selectedItem)) {
                languageCombo.select(languageCombo.getItemCount() - 1);
            }
        }
        if (selectionIndex == -1) {
            boolean selectionSuccess = selectDefaultDescription();
            if (!(selectionSuccess) && languageCombo.getItemCount() > 0) {
                languageCombo.select(0);
            }
        }
    }

    /**
     * Tries to select the default description and returns if successful (e.g. it's not successful
     * if there is no default language).
     */
    private boolean selectDefaultDescription() {
        int defaultIndex = -1;
        int i = 0;
        for (String languageName : languageCodes.keySet()) {
            String languageCode = languageCodes.get(languageName);
            IIpsProjectProperties properties = describedElement.getIpsProject().getProperties();
            ISupportedLanguage language = properties.getSupportedLanguage(new Locale(languageCode));
            if (language != null) {
                if (language.isDefaultLanguage()) {
                    defaultIndex = i;
                    break;
                }
            }
            i++;
        }

        if (defaultIndex != -1) {
            languageCombo.select(defaultIndex);
            return true;
        }
        return false;
    }

    /**
     * Ensures that the description text area shows the currently selected description.
     */
    private void updateDescription() {
        bindingContext.removeBindings(textArea);

        IDescription descriptionBeforeUpdate = currentDescription;
        updateCurrentDescription();
        if (currentDescription != null) {
            bindingContext.bindContent(textEditField, currentDescription, IDescription.PROPERTY_TEXT);
        }
        if (descriptionBeforeUpdate == null || currentDescription == null
                || !(currentDescription.equals(descriptionBeforeUpdate))) {
            updateTextArea();
        }
    }

    /**
     * Updates the current active {@link IDescription} based on the user selection in the language
     * combo box.
     */
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
        textEditField.setText(currentDescription.getText());
    }

    void setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
        textArea.setEnabled(!(viewOnly));
    }

}
