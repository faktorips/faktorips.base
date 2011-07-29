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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.StringValueComboField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.search.ISearchPresentationModel;
import org.faktorips.devtools.core.ui.search.model.scope.IModelSearchScope;
import org.faktorips.devtools.core.ui.search.model.scope.ModelSearchProjectsScope;
import org.faktorips.devtools.core.ui.search.model.scope.ModelSearchSelectionScope;
import org.faktorips.devtools.core.ui.search.model.scope.ModelSearchWorkingSetScope;
import org.faktorips.devtools.core.ui.search.model.scope.ModelSearchWorkspaceScope;

/**
 * DialogPage for the Faktor-IPS Model Search
 * 
 * @author dicker
 */
public class ModelSearchPage extends DialogPage implements ISearchPage {

    private final BindingContext bindingContext = new BindingContext();
    private final ModelSearchPresentationModel model = new ModelSearchPresentationModel();

    private static final String MODEL_SEARCH_PAGE_NAME = "ModelSearchPage"; //$NON-NLS-1$
    private static final String MODEL_SEARCH_DATA = "ModelSearchData"; //$NON-NLS-1$

    private ISearchPageContainer container;
    private Text txtTypeName;
    private StringValueComboField txtSearchString;
    private Combo cboSearchString;

    private Checkbox ckbSearchAttributes;
    private Checkbox ckbSearchMethods;
    private Checkbox ckbSearchAssociations;
    private Checkbox ckbSearchTableStructureUsages;
    private Checkbox ckbSearchValidationRules;

    private List<IDialogSettings> previousSearchData;
    private IDialogSettings dialogSettings;

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

                model.read(previousSearchData.get(selectionIndex));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });

        cboSearchString.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        txtSearchString = new StringValueComboField(cboSearchString);

        for (IDialogSettings settings : previousSearchData) {
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

        bindingContext.bindContent(txtSearchString, model, ModelSearchPresentationModel.SEARCH_TERM);
        bindingContext.bindContent(txtTypeName, model, ISearchPresentationModel.SRC_FILE_PATTERN);
        bindingContext.bindContent(ckbSearchAttributes, model, ModelSearchPresentationModel.SEARCH_ATTRIBUTES);
        bindingContext.bindContent(ckbSearchMethods, model, ModelSearchPresentationModel.SEARCH_METHODS);
        bindingContext.bindContent(ckbSearchAssociations, model, ModelSearchPresentationModel.SEARCH_ASSOCIATIONS);
        bindingContext.bindContent(ckbSearchTableStructureUsages, model,
                ModelSearchPresentationModel.SEARCH_TABLE_STRUCTURE_USAGES);
        bindingContext.bindContent(ckbSearchValidationRules, model,
                ModelSearchPresentationModel.SEARCH_VALIDATION_RULES);

        bindingContext.updateUI();
    }

    @Override
    public boolean performAction() {
        // it is impossible link the search scope to the model with the context binding, because a
        // changed selection of the scope doesn't throw an event.
        model.setSearchScope(createSearchScope());

        ModelSearchQuery query = new ModelSearchQuery(model);

        writeConfiguration();

        NewSearchUI.runQueryInBackground(query);

        return true;
    }

    private IModelSearchScope createSearchScope() {
        int selectedScope = container.getSelectedScope();

        switch (selectedScope) {
            case ISearchPageContainer.WORKSPACE_SCOPE:
                return new ModelSearchWorkspaceScope();

            case ISearchPageContainer.SELECTION_SCOPE:
                return new ModelSearchSelectionScope(container.getSelection());

            case ISearchPageContainer.WORKING_SET_SCOPE:
                return new ModelSearchWorkingSetScope(container.getSelectedWorkingSets());

            case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
                return new ModelSearchProjectsScope(container.getSelection());

            default:
                break;
        }
        return null;
    }

    @Override
    public void setContainer(ISearchPageContainer container) {
        this.container = container;
    }

    private void readConfiguration() {
        IDialogSettings settings = getDialogSettings();

        IDialogSettings[] sections = settings.getSections();

        previousSearchData = new ArrayList<IDialogSettings>();
        for (IDialogSettings dialogSettings : sections) {
            if (dialogSettings.getName().startsWith(MODEL_SEARCH_DATA)) {
                previousSearchData.add(dialogSettings);
            }
        }

        Comparator<IDialogSettings> comparator = new Comparator<IDialogSettings>() {

            @Override
            public int compare(IDialogSettings arg0, IDialogSettings arg1) {
                return arg1.getName().compareTo(arg0.getName());
            }

        };
        Collections.sort(previousSearchData, comparator);
    }

    private void writeConfiguration() {
        IDialogSettings settings = getDialogSettings();

        // TODO evtl. sortierkriterium optimieren
        IDialogSettings newSection = settings.addNewSection(MODEL_SEARCH_DATA + System.currentTimeMillis());

        model.store(newSection);

    }

    private IDialogSettings getDialogSettings() {
        if (dialogSettings == null) {
            IDialogSettings settings = IpsPlugin.getDefault().getDialogSettings();
            dialogSettings = settings.getSection(MODEL_SEARCH_PAGE_NAME);
            if (dialogSettings == null) {
                dialogSettings = settings.addNewSection(MODEL_SEARCH_PAGE_NAME);
            }
        }
        return dialogSettings;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (bindingContext != null) {
            bindingContext.dispose();
        }
    }
}
