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
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.StringValueComboField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.search.AbstractIpsSearchPage;

/**
 * DialogPage for the Faktor-IPS Model Search
 * 
 * @author dicker
 */
public class ModelSearchPage extends AbstractIpsSearchPage<ModelSearchPresentationModel> {

    private static final String MODEL_SEARCH_PAGE_NAME = "ModelSearchPage"; //$NON-NLS-1$
    private static final String MODEL_SEARCH_DATA = "ModelSearchData"; //$NON-NLS-1$

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
        getBindingContext().bindContent(txtSearchString, getModel(), ModelSearchPresentationModel.SEARCH_TERM);

        for (IDialogSettings settings : getPreviousSearchData()) {
            cboSearchString.add(settings.get(ModelSearchPresentationModel.SEARCH_TERM));
        }

        Group searchForGroup = toolkit
                .createGridGroup(composite, Messages.ModelSearchPage_groupLabelSearchFor, 2, true);

        ckbSearchAttributes = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelAttributes);
        getBindingContext()
                .bindContent(ckbSearchAttributes, getModel(), ModelSearchPresentationModel.SEARCH_ATTRIBUTES);

        ckbSearchMethods = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelMethodsAndFormulas);
        getBindingContext().bindContent(ckbSearchMethods, getModel(), ModelSearchPresentationModel.SEARCH_METHODS);

        ckbSearchAssociations = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelAssociations);
        getBindingContext().bindContent(ckbSearchAssociations, getModel(),
                ModelSearchPresentationModel.SEARCH_ASSOCIATIONS);

        ckbSearchTableStructureUsages = toolkit.createCheckbox(searchForGroup,
                Messages.ModelSearchPage_labelTableStructureUsage);
        getBindingContext().bindContent(ckbSearchTableStructureUsages, getModel(),
                ModelSearchPresentationModel.SEARCH_TABLE_STRUCTURE_USAGES);

        ckbSearchValidationRules = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelRules);
        getBindingContext().bindContent(ckbSearchValidationRules, getModel(),
                ModelSearchPresentationModel.SEARCH_VALIDATION_RULES);

        toolkit.createVerticalSpacer(composite, 10);
        createSrcFilePatternText(toolkit, composite);

        setControl(composite);

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
        ModelSearchPresentationModel model = new ModelSearchPresentationModel();
        model.initDefaultSearchValues();
        return model;
    }
}
