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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Section to edit supertype, abstract flag and configured <tt>IPolicyCmptType</tt>.
 * 
 * @author Jan Ortmann
 */
public class OptionalConstraintsSection extends IpsSection {

    private IIpsProjectProperties iIpsProjectProperties;
    private ComboViewer derivedUnionIsImplementedComboViewer;
    private ComboViewer referencedProductComponentsAreValidOnThisGenerationsValidFromDateComboViewer;
    private ComboViewer rulesWithoutReferencesAllowedComboViewer;

    public OptionalConstraintsSection(IIpsProjectProperties iIpsProjectProperties, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);

        this.iIpsProjectProperties = iIpsProjectProperties;

        initControls();
        setText(Messages.OptionalConstraints_title);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {

        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        // ProductCmptType2RefControl supertypeRefControl = new ProductCmptType2RefControl(
        // productCmptType.getIpsProject(), composite, toolkit, false);
        // bindingContext.bindContent(supertypeRefControl, productCmptType,
        // IType.PROPERTY_SUPERTYPE);

        toolkit.createLabel(composite, Messages.OptionalConstraints_derivedUnionIsImplemented);
        Combo derivedUnionIsImplementedCombo = toolkit.createCombo(composite);
        derivedUnionIsImplementedComboViewer = createComboViewer(derivedUnionIsImplementedCombo);
        if (iIpsProjectProperties.isDerivedUnionIsImplementedRuleEnabled()) {
            derivedUnionIsImplementedComboViewer.setSelection(new StructuredSelection(true), true);
        } else {
            derivedUnionIsImplementedComboViewer.setSelection(new StructuredSelection(false), true);
        }
        // Checkbox abstractCheckbox = toolkit.createCheckbox(composite);
        // bindingContext.bindContent(abstractCheckbox, productCmptType, IType.PROPERTY_ABSTRACT);

        toolkit.createFormLabel(composite,
                Messages.OptionalConstraints_referencedProductComponentsAreValidOnThisGenerationsValidFromDate);
        Combo referencedProductComponentsAreValidOnThisGenerationsValidFromDateCombo = toolkit.createCombo(composite);
        referencedProductComponentsAreValidOnThisGenerationsValidFromDateComboViewer = createComboViewer(referencedProductComponentsAreValidOnThisGenerationsValidFromDateCombo);
        if (iIpsProjectProperties.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled()) {
            referencedProductComponentsAreValidOnThisGenerationsValidFromDateComboViewer.setSelection(
                    new StructuredSelection(true), true);
        } else {
            referencedProductComponentsAreValidOnThisGenerationsValidFromDateComboViewer.setSelection(
                    new StructuredSelection(false), true);
        }
        // Checkbox configuratedCheckbox = toolkit.createCheckbox(composite);
        // bindingContext.bindContent(configuratedCheckbox, productCmptType,
        // IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE);
        toolkit.createFormLabel(composite, Messages.OptionalConstraints_rulesWithoutReferencesAllowed);
        Combo rulesWithoutReferencesAllowedCombo = toolkit.createCombo(composite);
        rulesWithoutReferencesAllowedComboViewer = createComboViewer(rulesWithoutReferencesAllowedCombo);
        if (iIpsProjectProperties.isRulesWithoutReferencesAllowedEnabled()) {
            rulesWithoutReferencesAllowedComboViewer.setSelection(new StructuredSelection(true), true);
        } else {
            rulesWithoutReferencesAllowedComboViewer.setSelection(new StructuredSelection(false), true);
        }
    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    public ComboViewer createComboViewer(Combo combo) {
        ComboViewer comboViewer = new ComboViewer(combo);
        comboViewer.setContentProvider(new ArrayContentProvider());
        Boolean[] input = new Boolean[] { true, false };
        comboViewer.setInput(input);
        return comboViewer;
    }

    public void saveOptionalConstraints() {
        Boolean ttt = aaab(derivedUnionIsImplementedComboViewer.getSelection());
        if (ttt != null) {
            iIpsProjectProperties.setDerivedUnionIsImplementedRuleEnabled(ttt);
        }
        Boolean vvv = aaab(referencedProductComponentsAreValidOnThisGenerationsValidFromDateComboViewer.getSelection());
        if (vvv != null) {
            iIpsProjectProperties.setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(vvv);
        }
        Boolean eee = aaab(rulesWithoutReferencesAllowedComboViewer.getSelection());
        if (eee != null) {
            iIpsProjectProperties.setRulesWithoutReferencesAllowedEnabled(eee);
        }

    }

    public Boolean aaab(ISelection selected) {
        if (selected instanceof StructuredSelection) {
            StructuredSelection selection = (StructuredSelection)selected;
            return (Boolean)selection.getFirstElement();
        }
        return null;
    }
}
