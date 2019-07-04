/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.model;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.search.ui.ISearchQuery;
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
        readDialogSettings();

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

                getPresentationModel().read(getPreviousSearchData().get(selectionIndex));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });

        cboSearchString.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        txtSearchString = new StringValueComboField(cboSearchString);
        getBindingContext().bindContent(txtSearchString, getPresentationModel(),
                ModelSearchPresentationModel.SEARCH_TERM);

        for (IDialogSettings settings : getPreviousSearchData()) {
            cboSearchString.add(settings.get(ModelSearchPresentationModel.SEARCH_TERM));
        }

        Group searchForGroup = toolkit
                .createGridGroup(composite, Messages.ModelSearchPage_groupLabelSearchFor, 2, true);

        ckbSearchAttributes = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelAttributes);
        getBindingContext().bindContent(ckbSearchAttributes, getPresentationModel(),
                ModelSearchPresentationModel.SEARCH_ATTRIBUTES);

        ckbSearchMethods = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelMethodsAndFormulas);
        getBindingContext().bindContent(ckbSearchMethods, getPresentationModel(),
                ModelSearchPresentationModel.SEARCH_METHODS);

        ckbSearchAssociations = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelAssociations);
        getBindingContext().bindContent(ckbSearchAssociations, getPresentationModel(),
                ModelSearchPresentationModel.SEARCH_ASSOCIATIONS);

        ckbSearchTableStructureUsages = toolkit.createCheckbox(searchForGroup,
                Messages.ModelSearchPage_labelTableStructureUsage);
        getBindingContext().bindContent(ckbSearchTableStructureUsages, getPresentationModel(),
                ModelSearchPresentationModel.SEARCH_TABLE_STRUCTURE_USAGES);

        ckbSearchValidationRules = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelRules);
        getBindingContext().bindContent(ckbSearchValidationRules, getPresentationModel(),
                ModelSearchPresentationModel.SEARCH_VALIDATION_RULES);

        toolkit.createVerticalSpacer(composite, 10);
        createSrcFilePatternText(toolkit, composite, Messages.ModelSearchPage_labelTypeName);

        setControl(composite);

        getBindingContext().updateUI();
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

    @Override
    protected ISearchQuery createSearchQuery() {
        return new ModelSearchQuery(getPresentationModel());
    }
}
