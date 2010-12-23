/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Section to edit supertype, abstract flag and configured <tt>IPolicyCmptType</tt>.
 * 
 * @author Jan Ortmann
 */
public class PersistenceOptionsSection extends IpsSection {

    private IIpsProjectProperties iIpsProjectProperties;
    private IPersistenceOptions persistenceOptions;
    private Text maxColumnNameLengthText;
    private Text maxTableNameLengthText;
    private Checkbox allowLazyFetchForSingleValuedAssociationsCheckboxTrue;
    private Checkbox persistenceSupportEnabledRadiobuttonTrue;

    public PersistenceOptionsSection(IIpsProjectProperties iIpsProjectProperties, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);

        this.iIpsProjectProperties = iIpsProjectProperties;
        this.persistenceOptions = iIpsProjectProperties.getPersistenceOptions();
        initControls();
        setText(Messages.PersistenceOptions_title);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {

        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);
        // Composite composite2 = toolkit.createGridComposite(client, 2, true, true);

        toolkit.createFormLabel(composite, Messages.Overview_persistenceSupportEnabled);
        persistenceSupportEnabledRadiobuttonTrue = toolkit.createCheckbox(composite);
        if (iIpsProjectProperties.isPersistenceSupportEnabled()) {
            persistenceSupportEnabledRadiobuttonTrue.setChecked(true);
        }
        toolkit.createLabel(composite, Messages.PersistenceOptions_allowLazyFetchForSingleValuedAssociations);
        allowLazyFetchForSingleValuedAssociationsCheckboxTrue = toolkit.createCheckbox(composite);
        if (persistenceOptions.isAllowLazyFetchForSingleValuedAssociations()) {
            allowLazyFetchForSingleValuedAssociationsCheckboxTrue.setChecked(true);
        }
        // Checkbox abstractCheckbox = toolkit.createCheckbox(composite);
        // bindingContext.bindContent(abstractCheckbox, productCmptType, IType.PROPERTY_ABSTRACT);

        toolkit.createFormLabel(composite, Messages.PersistenceOptions_maxColumnNameLength);
        maxColumnNameLengthText = toolkit.createText(composite);
        maxColumnNameLengthText.setText("" + persistenceOptions.getMaxColumnNameLenght());
        // Checkbox configuratedCheckbox = toolkit.createCheckbox(composite);
        // bindingContext.bindContent(configuratedCheckbox, productCmptType,
        // IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE);
        toolkit.createFormLabel(composite, Messages.PersistenceOptions_maxTableNameLength);
        maxTableNameLengthText = toolkit.createText(composite);
        maxTableNameLengthText.setText("" + persistenceOptions.getMaxColumnNameLenght());

        toolkit.createFormLabel(composite, Messages.PersistenceOptions_tableNamingStrategy);
        Text tableNamingStrategyText = toolkit.createText(composite);
        // tableNamingStrategyText.setText(IpsPackageContentProvider.getTableNamingStrategy().getId());

        toolkit.createFormLabel(composite, Messages.PersistenceOptions_tableColumnNamingStrategy);
        Text tableColumnNamingStrategyText = toolkit.createText(composite);
        // tableColumnNamingStrategyText.setText(IpsPackageContentProvider.getTableColumnNamingStrategy());
        // PcTypeRefControl control =
        // toolkit.createPcTypeRefControl(productCmptType.getIpsProject(), composite);
        // bindingContext.bindContent(control, productCmptType,
        // IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);
        // bindingContext.bindEnabled(control, productCmptType,
        // IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE);
        //
        // extFactory.createControls(composite, toolkit, productCmptType);
        // extFactory.bind(bindingContext);
        savePersistenceOptions();
    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    private void savePersistenceOptions() {
        // persistenceOptions.setAllowLazyFetchForSingleValuedAssociations(true);
        // ISelection selected = allowLazyFetchForSingleValueAssociationsComboViewer.getSelection();
        // if (selected instanceof StructuredSelection) {
        // StructuredSelection selection = (StructuredSelection)selected;
        // persistenceOptions.setAllowLazyFetchForSingleValuedAssociations((Boolean)selection.getFirstElement());
        // }
        // persistenceOptions.setMaxColumnNameLength(Integer.parseInt(maxColumnNameLengthText.getText()));
        // persistenceOptions.setMaxTableNameLength(Integer.parseInt(maxTableNameLengthText.getText()));
    }

}
