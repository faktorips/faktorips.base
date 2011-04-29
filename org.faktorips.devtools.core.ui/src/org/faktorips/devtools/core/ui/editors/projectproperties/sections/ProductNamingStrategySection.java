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

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.internal.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;

public class ProductNamingStrategySection extends IpsSection {
    private IIpsProjectProperties iIpsProjectProperties;
    private ComboViewer productDefinitionProjectComboViewer;
    private ComboViewer persistenceSupportEnabledComboViewer;
    private ComboViewer modelProjectComboViewer;
    private ComboViewer javaProjectContainsClassesForDynamicDatatypesComboViewer;
    private ComboViewer changesOverTimeNamingConventionIdForGeneratedCodeComboViewer;
    private Text productCmptNamingStrategyText;
    private Text runtimeIdPrefixText;

    public ProductNamingStrategySection(IIpsProjectProperties iIpsProjectProperties, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        this.iIpsProjectProperties = iIpsProjectProperties;

        initControls();
        setText(Messages.Overview_title);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);
        IProductCmptNamingStrategy requiredIpsFeatureIds = iIpsProjectProperties.getProductCmptNamingStrategy();
        if (requiredIpsFeatureIds instanceof DateBasedProductCmptNamingStrategy) {
            DateBasedProductCmptNamingStrategy dataBasedProductCmptNamingStrategy = (DateBasedProductCmptNamingStrategy)requiredIpsFeatureIds;
            String dateFormatPattern = dataBasedProductCmptNamingStrategy.getDateFormatPattern();
            System.out.println("aa");
        }

        String namingConvention = iIpsProjectProperties.getChangesOverTimeNamingConventionIdForGeneratedCode();
        toolkit.createFormLabel(composite, Messages.Overview_changesOverTimeNamingConventionIdForGeneratedCode);
        Combo changesOverTimeNamingConventionIdForGeneratedCodeCombo = toolkit.createCombo(composite);
        changesOverTimeNamingConventionIdForGeneratedCodeComboViewer = new ComboViewer(
                changesOverTimeNamingConventionIdForGeneratedCodeCombo);
        changesOverTimeNamingConventionIdForGeneratedCodeComboViewer.setContentProvider(new ArrayContentProvider());
        String[] namingConventions = new String[] { "VAA", "FIPS", "Produkt-Manager" };
        changesOverTimeNamingConventionIdForGeneratedCodeComboViewer.setInput(namingConventions);
        changesOverTimeNamingConventionIdForGeneratedCodeComboViewer.setSelection(new StructuredSelection(
                namingConvention), true);

        toolkit.createFormLabel(composite, Messages.Overview_javaProjectContainsClassesForDynamicDatatypes);
        Combo javaProjectContainsClassesForDynamicDatatypesCombo = toolkit.createCombo(composite);
        javaProjectContainsClassesForDynamicDatatypesComboViewer = createComboViewer(javaProjectContainsClassesForDynamicDatatypesCombo);
        setComboViewer(javaProjectContainsClassesForDynamicDatatypesComboViewer, iIpsProjectProperties
                .isJavaProjectContainsClassesForDynamicDatatypes());

        // IIpsArtefactBuilderSetConfigModel aaa3 = iIpsProjectProperties.getBuilderSetConfig();
        toolkit.createFormLabel(composite, Messages.Overview_modelProject);
        Combo modelProjectCombo = toolkit.createCombo(composite);
        modelProjectComboViewer = createComboViewer(modelProjectCombo);
        setComboViewer(modelProjectComboViewer, iIpsProjectProperties.isModelProject());

        toolkit.createFormLabel(composite, Messages.Overview_persistenceSupportEnabled);
        Combo persistenceSupportEnabledCombo = toolkit.createCombo(composite);
        persistenceSupportEnabledComboViewer = createComboViewer(persistenceSupportEnabledCombo);
        setComboViewer(persistenceSupportEnabledComboViewer, iIpsProjectProperties.isPersistenceSupportEnabled());

        toolkit.createFormLabel(composite, Messages.Overview_productDefinitionProject);
        Combo productDefinitionProjectCombo = toolkit.createCombo(composite);
        productDefinitionProjectComboViewer = createComboViewer(productDefinitionProjectCombo);
        setComboViewer(productDefinitionProjectComboViewer, iIpsProjectProperties.isProductDefinitionProject());

        String runtimeIdPrefix = iIpsProjectProperties.getRuntimeIdPrefix();
        toolkit.createFormLabel(composite, Messages.Overview_runtimeIdPrefix);
        runtimeIdPrefixText = toolkit.createText(composite);
        runtimeIdPrefixText.setText(runtimeIdPrefix);

        // String runtimeIdPrefix = iIpsProjectProperties;
        // toolkit.createFormLabel(composite, Messages.Overview_minVersion);
        // Text runtimeIdPrefixText = toolkit.createText(composite);
        // runtimeIdPrefixText.setText(runtimeIdPrefix);
        // String runtimeIdPrefix = iIpsProjectProperties.get;
        // toolkit.createFormLabel(composite, Messages.Overview_minVersion);
        // Text runtimeIdPrefixText = toolkit.createText(composite);
        // runtimeIdPrefixText.setText(runtimeIdPrefix);

        // IProductCmptNamingStrategy productCmptNamingStrategy =
        // iIpsProjectProperties.getProductCmptNamingStrategy();
        // toolkit.createFormLabel(composite, Messages.Overview_minVersion);
        // productCmptNamingStrategyText = toolkit.createText(composite);
        // productCmptNamingStrategyText.setText(productCmptNamingStrategy.getExtensionId());

    }

    @Override
    protected void performRefresh() {
        // TODO Auto-generated method stub

    }

    private void setComboViewer(ComboViewer cv, boolean selected) {
        if (selected) {
            cv.setSelection(new StructuredSelection(true), true);
        } else {
            cv.setSelection(new StructuredSelection(false), true);
        }
    }

    public ComboViewer createComboViewer(Combo combo) {
        ComboViewer comboViewer = new ComboViewer(combo);
        comboViewer.setContentProvider(new ArrayContentProvider());
        Boolean[] input = new Boolean[] { true, false };
        comboViewer.setInput(input);
        return comboViewer;
    }

    public void saveOverview() {
        StructuredSelection bbb = (StructuredSelection)productDefinitionProjectComboViewer.getSelection();
        Boolean qqq = (Boolean)bbb.getFirstElement();
        iIpsProjectProperties.setProductDefinitionProject(qqq);

        StructuredSelection bbbbb = (StructuredSelection)persistenceSupportEnabledComboViewer.getSelection();
        Boolean qqqqq = (Boolean)bbbbb.getFirstElement();
        iIpsProjectProperties.setPersistenceSupport(qqqqq);

        StructuredSelection bbbb = (StructuredSelection)modelProjectComboViewer.getSelection();
        Boolean qqqq = (Boolean)bbbb.getFirstElement();
        iIpsProjectProperties.setModelProject(qqqq);

        StructuredSelection b = (StructuredSelection)javaProjectContainsClassesForDynamicDatatypesComboViewer
                .getSelection();
        Boolean q = (Boolean)b.getFirstElement();
        iIpsProjectProperties.setJavaProjectContainsClassesForDynamicDatatypes(q);

        StructuredSelection bb = (StructuredSelection)changesOverTimeNamingConventionIdForGeneratedCodeComboViewer
                .getSelection();
        String qq = (String)bb.getFirstElement();
        iIpsProjectProperties.setChangesOverTimeNamingConventionIdForGeneratedCode(qq);

        String runtimeIdPrefixString = runtimeIdPrefixText.getText();
        iIpsProjectProperties.setRuntimeIdPrefix(runtimeIdPrefixString);

        // String text = productCmptNamingStrategyText.getText();
        // iIpsProjectProperties.set(newStrategy(text));
    }
}
