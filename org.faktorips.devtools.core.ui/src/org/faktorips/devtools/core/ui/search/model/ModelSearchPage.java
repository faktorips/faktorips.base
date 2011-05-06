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
import java.util.List;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.search.model.scope.ModelSearchProjectsScope;
import org.faktorips.devtools.core.ui.search.model.scope.ModelSearchScope;
import org.faktorips.devtools.core.ui.search.model.scope.ModelSearchSelectionScope;
import org.faktorips.devtools.core.ui.search.model.scope.ModelSearchWorkingSetScope;
import org.faktorips.devtools.core.ui.search.model.scope.ModelSearchWorkspaceScope;

public class ModelSearchPage extends DialogPage implements ISearchPage {

    private class ModelSearchData {
        private String typeName;

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }

        public void store(IDialogSettings settings) {
            settings.put("typeName", getTypeName());
        }

        public void read(IDialogSettings settings) {
            setTypeName(settings.get("typeName"));
            System.out.println(getTypeName());
        }
    }

    private static final String MODEL_SEARCH_PAGE_NAME = "ModelSearchPage";

    private ISearchPageContainer container;
    private Text txtTypeName;
    private Text txtSearchTerm;

    private Checkbox ckbSearchAttributes;
    private Checkbox ckbSearchMethods;
    private Checkbox ckbSearchAssociations;
    private Checkbox ckbSearchTableStructureUsages;
    private Checkbox ckbSearchValidationRules;

    private List<ModelSearchData> previousSearchData;
    private IDialogSettings dialogSettings;

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        readConfiguration();

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

        toolkit.createLabel(composite, Messages.ModelSearchPage_labelSearchTerm);
        txtSearchTerm = toolkit.createText(composite);

        Group searchForGroup = toolkit
                .createGridGroup(composite, Messages.ModelSearchPage_groupLabelSearchFor, 2, true);

        ckbSearchAttributes = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelAttributes);
        ckbSearchAttributes.setChecked(true);

        ckbSearchMethods = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelMethodsAndFormulas);
        ckbSearchMethods.setChecked(true);

        ckbSearchAssociations = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelAssociations);
        ckbSearchAssociations.setChecked(true);

        ckbSearchTableStructureUsages = toolkit.createCheckbox(searchForGroup,
                Messages.ModelSearchPage_labelTableStructureUsage);
        ckbSearchTableStructureUsages.setChecked(true);

        ckbSearchValidationRules = toolkit.createCheckbox(searchForGroup, Messages.ModelSearchPage_labelRules);
        ckbSearchValidationRules.setChecked(true);

        toolkit.createVerticalSpacer(composite, 10);
        toolkit.createLabel(composite, Messages.ModelSearchPage_labelTypeName);
        txtTypeName = toolkit.createText(composite);

        setControl(composite);

    }

    public String getTypeName() {
        return txtTypeName.getText();
    }

    public void setTypeName(String typeName) {
        txtTypeName.setText(typeName);
    }

    @Override
    public boolean performAction() {
        ModelSearchData modelSearchData = new ModelSearchData();
        modelSearchData.setTypeName(getTypeName());

        ModelSearchPresentationModel model = new ModelSearchPresentationModel();
        model.setTypeName(getTypeName());
        model.setSearchTerm(txtSearchTerm.getText());

        model.setSearchAttributes(ckbSearchAttributes.isChecked());
        model.setSearchMethods(ckbSearchMethods.isChecked());
        model.setSearchAssociations(ckbSearchAssociations.isChecked());
        model.setSearchTableStructureUsages(ckbSearchTableStructureUsages.isChecked());
        model.setSearchValidationRules(ckbSearchValidationRules.isChecked());

        model.setSearchScope(createSearchScope());

        ModelSearchQuery query = new ModelSearchQuery(model);

        writeConfiguration();

        NewSearchUI.runQueryInBackground(query);

        return true;
    }

    private ModelSearchScope createSearchScope() {
        int selectedScope = container.getSelectedScope();

        switch (selectedScope) {
            case ISearchPageContainer.WORKSPACE_SCOPE:
                return new ModelSearchWorkspaceScope();

            case ISearchPageContainer.SELECTION_SCOPE:
                ModelSearchSelectionScope modelSearchSelectionScope = new ModelSearchSelectionScope(
                        container.getSelection());
                return modelSearchSelectionScope;

            case ISearchPageContainer.WORKING_SET_SCOPE:
                return new ModelSearchWorkingSetScope(container.getSelectedWorkingSets());

            case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
                ModelSearchProjectsScope modelSearchProjectsScope = new ModelSearchProjectsScope(
                        container.getSelection());
                return modelSearchProjectsScope;

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

        previousSearchData = new ArrayList<ModelSearchPage.ModelSearchData>();
        for (IDialogSettings iDialogSettings : sections) {
            ModelSearchData modelSearchData = new ModelSearchData();
            modelSearchData.read(iDialogSettings);

            previousSearchData.add(modelSearchData);
        }
    }

    private void writeConfiguration() {
        IDialogSettings settings = getDialogSettings();

        ModelSearchData modelSearchData = new ModelSearchData();
        modelSearchData.setTypeName(getTypeName());

        IDialogSettings addNewSection = settings.addNewSection("" + System.currentTimeMillis()); //$NON-NLS-1$

        modelSearchData.store(addNewSection);

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
}
