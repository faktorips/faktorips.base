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

package org.faktorips.devtools.core.ui.search.model;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;

public class ModelSearchPage extends DialogPage implements ISearchPage {

    private ISearchPageContainer container;
    private Text txtTypeName;
    private Text txtSearchTerm;

    private Checkbox ckbSearchAttributes;
    private Checkbox ckbSearchMethods;
    private Checkbox ckbSearchAssociations;
    private Checkbox ckbSearchTableStructureUsages;
    private Checkbox ckbSearchValidationRules;

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, true));
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));

        new Label(composite, SWT.NONE).setText("Class Names");
        txtTypeName = toolkit.createText(composite);

        new Label(composite, SWT.NONE).setText("Search Term");
        txtSearchTerm = toolkit.createText(composite);

        ckbSearchAttributes = toolkit.createCheckbox(composite, "Attributes");
        ckbSearchAttributes.setChecked(true);

        ckbSearchMethods = toolkit.createCheckbox(composite, "Methods and Formulas");
        ckbSearchMethods.setChecked(true);

        ckbSearchAssociations = toolkit.createCheckbox(composite, "Associations");
        ckbSearchAssociations.setChecked(true);

        ckbSearchTableStructureUsages = toolkit.createCheckbox(composite, "Table Structure Usages");
        ckbSearchTableStructureUsages.setChecked(true);

        ckbSearchValidationRules = toolkit.createCheckbox(composite, "Rules");
        ckbSearchValidationRules.setChecked(true);

        setControl(composite);

    }

    @Override
    public boolean performAction() {
        ModelSearchPresentationModel model = new ModelSearchPresentationModel();
        model.setTypeName(txtTypeName.getText());
        model.setSearchTerm(txtSearchTerm.getText());

        model.setSearchAttributes(ckbSearchAttributes.isChecked());
        model.setSearchMethods(ckbSearchMethods.isChecked());
        model.setSearchAssociations(ckbSearchAssociations.isChecked());
        model.setSearchTableStructureUsages(ckbSearchTableStructureUsages.isChecked());
        model.setSearchValidationRules(ckbSearchValidationRules.isChecked());

        model.setProject(IpsPlugin.getDefault().getIpsModel().getIpsProject("Produkt.Kfz.Modell"));

        ModelSearchQuery query = new ModelSearchQuery(model);

        NewSearchUI.runQueryInBackground(query);

        return true;
    }

    @Override
    public void setContainer(ISearchPageContainer container) {
        this.container = container;
    }
}
