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

package org.faktorips.devtools.core.ui.search.model;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.StringValueComboField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.search.AbstractIpsSearchPage;
import org.faktorips.devtools.core.ui.search.IIpsSearchPresentationModel;

/**
 * DialogPage for the Faktor-IPS Model Search
 * 
 * @author dicker
 */
public class ModelSearchPage extends AbstractIpsSearchPage<ModelSearchPresentationModel> {

    private static final String MODEL_SEARCH_PAGE_NAME = "ModelSearchPage"; //$NON-NLS-1$
    private static final String MODEL_SEARCH_DATA = "ModelSearchData"; //$NON-NLS-1$

    private Text txtTypeName;
    private StringValueComboField txtSearchString;
    private Combo cboSearchString;

    private Checkbox ckbSearchAttributes;
    private Checkbox ckbSearchMethods;
    private Checkbox ckbSearchAssociations;
    private Checkbox ckbSearchTableStructureUsages;
    private Checkbox ckbSearchValidationRules;

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        readConfiguration();

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

        toolkit.createLabel(composite, Messages.ModelSearchPage_labelSearchTerm);

        cboSearchString = new Combo(composite, SWT.SINGLE | SWT.BORDER);

        cboSearchString.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectionIndex = cboSearchString.getSelectionIndex();
                if (selectionIndex == -1) {
                    return;
                }

                getModel().read(getPreviousSearchData().get(selectionIndex));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });

        cboSearchString.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        txtSearchString = new StringValueComboField(cboSearchString);

        for (IDialogSettings settings : getPreviousSearchData()) {
            cboSearchString.add(settings.get(ModelSearchPresentationModel.SEARCH_TERM));
        }

        Group searchForGroup = toolkit
                .createGridGroup(composite, Messages.ModelSearchPage_groupLabelSearchFor, 2, true);

        ckbSearchAttributes = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelAttributes);

        ckbSearchMethods = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelMethodsAndFormulas);

        ckbSearchAssociations = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelAssociations);

        ckbSearchTableStructureUsages = toolkit.createCheckbox(searchForGroup,
                Messages.ModelSearchPage_labelTableStructureUsage);

        ckbSearchValidationRules = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelRules);

        toolkit.createVerticalSpacer(composite, 10);
        toolkit.createLabel(composite, Messages.ModelSearchPage_labelTypeName);
        txtTypeName = toolkit.createText(composite);

        setControl(composite);

        getBindingContext().bindContent(txtSearchString, getModel(), ModelSearchPresentationModel.SEARCH_TERM);
        getBindingContext().bindContent(txtTypeName, getModel(), IIpsSearchPresentationModel.SRC_FILE_PATTERN);
        getBindingContext().bindContent(ckbSearchAttributes, getModel(), ModelSearchPresentationModel.SEARCH_ATTRIBUTES);
        getBindingContext().bindContent(ckbSearchMethods, getModel(), ModelSearchPresentationModel.SEARCH_METHODS);
        getBindingContext().bindContent(ckbSearchAssociations, getModel(), ModelSearchPresentationModel.SEARCH_ASSOCIATIONS);
        getBindingContext().bindContent(ckbSearchTableStructureUsages, getModel(),
                ModelSearchPresentationModel.SEARCH_TABLE_STRUCTURE_USAGES);
        getBindingContext().bindContent(ckbSearchValidationRules, getModel(),
                ModelSearchPresentationModel.SEARCH_VALIDATION_RULES);

        getBindingContext().updateUI();
    }

    @Override
    public boolean performAction() {
        // it is impossible to link the search scope to the model with the context binding, because
        // a
        // changed selection of the scope doesn't throw an event.
        getModel().setSearchScope(createSearchScope());

        ModelSearchQuery query = new ModelSearchQuery(getModel());

        writeConfiguration();

        NewSearchUI.runQueryInBackground(query);

        return true;
    }

    @Override
    protected String getDialogSettingPrefix() {
        return MODEL_SEARCH_DATA;
    }

    @Override
    protected String getSearchPageName() {
        return MODEL_SEARCH_PAGE_NAME;
    }

    @Override
    protected ModelSearchPresentationModel createPresentationModel() {
        return new ModelSearchPresentationModel();
    }
}
