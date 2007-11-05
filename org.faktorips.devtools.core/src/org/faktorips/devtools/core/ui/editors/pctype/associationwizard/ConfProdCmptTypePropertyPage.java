/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.productcmpttype.AggregationKind;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.type.DerivedUnionCompletionProcessor;
import org.faktorips.values.EnumValue;

public class ConfProdCmptTypePropertyPage extends WizardPage implements IBlockedValidationWizardPage, IHiddenWizardPage {

    private NewPcTypeAssociationWizard wizard;
    private IProductCmptTypeAssociation association;
    private UIToolkit toolkit;
    private BindingContext bindingContext;

    private ArrayList visibleProperties = new ArrayList(10);
    
    private Text targetRoleSingularTextProdCmptType;
    private Text targetRolePluralTextProdCmptType;
    private CardinalityField cardinalityFieldMinProdCmptType;
    private CardinalityField cardinalityFieldMaxProdCmptType;
    private Text descriptionText;
    private Text targetText;
    private Combo typeCombo;
    private org.faktorips.devtools.core.ui.editors.pctype.associationwizard.ConfProdCmptTypePropertyPage.PmoAssociation pmoAssociation;
    private Text unionText;
    private Checkbox derivedUnion;
    private Checkbox subsetCheckbox;

    private Composite groupGeneral;
    private Composite dynamicComposite;
    private Composite mainComposite;

    protected ConfProdCmptTypePropertyPage(NewPcTypeAssociationWizard wizard, UIToolkit toolkit, BindingContext bindingContext) {
        
        super("Product Cmpt Type Association Page", "Product cmpt type association properties", null);
        super.setDescription("Define product component type association properties");
        this.wizard = wizard;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;
        
        setPageComplete(true);
    }
    
    public List getProperties() {
        return visibleProperties;
    }

    public void createControl(Composite parent) {
        mainComposite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(1, false);
        mainComposite.setLayout(layout);
        
        Composite top = toolkit.createLabelEditColumnComposite(mainComposite);
        
        // source 
        toolkit.createFormLabel(top, "Source");
        Text sourceText = toolkit.createText(top);
        sourceText.setEnabled(false);
        IProductCmptType productCmptType = wizard.findProductCmptType();
        if (productCmptType != null){
            sourceText.setText(productCmptType.getQualifiedName());
        }
        
        // target
        toolkit.createFormLabel(top, "Target");
        targetText = toolkit.createText(top);
        targetText.setEnabled(false);
        
        groupGeneral = toolkit.createGroup(mainComposite, "Properties");
        dynamicComposite = createGeneralControls(groupGeneral);
        
        createDerivedUnionGroup(toolkit.createGroup(mainComposite, "Derived union"));
        
        // description
        descriptionText = wizard.createDescriptionText(mainComposite);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_DESCRIPTION);
        
        setControl(mainComposite);
    }
    
    private Composite createGeneralControls(Composite c) {
        Composite workArea = toolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // top extensions
        wizard.getExtFactoryProductCmptTypeAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_TOP);
        
        // aggregation kind
        toolkit.createFormLabel(workArea, "Aggregation kind:");
        typeCombo = toolkit.createCombo(workArea, new EnumValue[]{AggregationKind.NONE, AggregationKind.SHARED});
        
        // role singular
        toolkit.createFormLabel(workArea, "Target role (singular):");
        targetRoleSingularTextProdCmptType = toolkit.createText(workArea);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularTextProdCmptType.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                updateDefaultTargetRoleSingular();
            }
        });

        // role plural
        toolkit.createFormLabel(workArea, "Target role (plural):");
        targetRolePluralTextProdCmptType = toolkit.createText(workArea);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralTextProdCmptType.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                updateDefaultTargetRolePlural();
            }
        });
        
        // min cardinality
        toolkit.createFormLabel(workArea, "Minimum cardinality:");
        Text minCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMinProdCmptType = new CardinalityField(minCardinalityText);
        cardinalityFieldMinProdCmptType.setSupportsNull(false);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        
        // max cardinality
        toolkit.createFormLabel(workArea, "Maximum cardinality:");
        Text maxCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMaxProdCmptType = new CardinalityField(maxCardinalityText);
        cardinalityFieldMaxProdCmptType.setSupportsNull(false);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        
        // bottom extensions
        wizard.getExtFactoryProductCmptTypeAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_BOTTOM);
        
        return workArea;
    }
    
    public void setProductCmptTypeAssociationAndUpdatePage(IProductCmptTypeAssociation productCmptAssociation) {
        association = productCmptAssociation;
        
        bindingContext.removeBindings(targetText);
        bindingContext.removeBindings(typeCombo);
        bindingContext.removeBindings(targetRoleSingularTextProdCmptType);
        bindingContext.removeBindings(targetRolePluralTextProdCmptType);
        bindingContext.removeBindings(cardinalityFieldMinProdCmptType.getControl());
        bindingContext.removeBindings(cardinalityFieldMaxProdCmptType.getControl());
        bindingContext.removeBindings(descriptionText);

        bindingContext.removeBindings(derivedUnion);
        bindingContext.removeBindings(subsetCheckbox);
        bindingContext.removeBindings(unionText);

        wizard.getExtFactoryAssociation().removeBinding(bindingContext);
        
        if (productCmptAssociation != null){
            this.pmoAssociation = new PmoAssociation(association);
            
            dynamicComposite.dispose();
            dynamicComposite = createGeneralControls(groupGeneral);
            
            bindingContext.bindContent(targetText, association, IProductCmptTypeAssociation.PROPERTY_TARGET);
            bindingContext.bindContent(typeCombo, association, IProductCmptTypeAssociation.PROPERTY_AGGREGATION_KIND, AggregationKind.getEnumType());
            bindingContext.bindContent(targetRoleSingularTextProdCmptType, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
            bindingContext.bindContent(targetRolePluralTextProdCmptType, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
            bindingContext.bindContent(cardinalityFieldMinProdCmptType, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
            bindingContext.bindContent(cardinalityFieldMaxProdCmptType, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
            bindingContext.bindContent(descriptionText, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_DESCRIPTION);

            bindingContext.bindContent(derivedUnion, association, IProductCmptTypeAssociation.PROPERTY_DERIVED_UNION);        
            bindingContext.bindContent(subsetCheckbox, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
            bindingContext.bindContent(unionText, association, IProductCmptTypeAssociation.PROPERTY_SUBSETTED_DERIVED_UNION);
            bindingContext.bindEnabled(unionText, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
            
            DerivedUnionCompletionProcessor completionProcessor = new DerivedUnionCompletionProcessor(association);
            completionProcessor.setComputeProposalForEmptyPrefix(true);
            ContentAssistHandler.createHandlerForText(unionText, CompletionUtil.createContentAssistant(completionProcessor));
            
            wizard.getExtFactoryProductCmptTypeAssociation().bind(bindingContext);
            
            bindingContext.updateUI();

            groupGeneral.pack(true);
            mainComposite.pack(true);
            mainComposite.getParent().pack(true);
            mainComposite.getParent().layout();
        } else {
            targetRoleSingularTextProdCmptType.setText("");
            targetRolePluralTextProdCmptType.setText("");
            cardinalityFieldMinProdCmptType.setText("");
            cardinalityFieldMaxProdCmptType.setText(""); 
            descriptionText.setText("");
        }
    }
    
    /**
     * @return <code>true</code> if the product cmpt type is available.
     */
    public boolean isPageVisible(){
        return wizard.isProductCmptTypeAvailable() && wizard.isConfigureProductCmptType();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean canFlipToNextPage() {
        setErrorMessage(null);
        boolean valid = wizard.validatePage(this, false);
        
        if (getNextPage() == null){
            return false;
        }
        
        return valid;
    }
    
    private void createDerivedUnionGroup(Composite workArea) {
        derivedUnion = toolkit.createCheckbox(workArea, "This association is a derived union");
        subsetCheckbox = toolkit.createCheckbox(workArea, "This association defines a subset of a derived union");

        Composite temp = toolkit.createLabelEditColumnComposite(workArea);
        temp.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        toolkit.createFormLabel(temp, "Derived union:");
        unionText = toolkit.createText(temp);
    }    
    
    public class PmoAssociation extends IpsObjectPartPmo {
        public final static String PROPERTY_SUBSET = "subset";

        private IProductCmptTypeAssociation association;
        private boolean subset;
        
        public PmoAssociation(IProductCmptTypeAssociation association) {
            super(association);
            this.association = association;
            subset = association.isSubsetOfADerivedUnion();
        }
        
        public boolean isSubset() {
            return subset;
        }
        
        public void setSubset(boolean newValue) {
            subset = newValue;
            if (!subset) {
                association.setSubsettedDerivedUnion("");
            }
            notifyListeners();
        }
    }  
    
    private void updateDefaultTargetRolePlural() {
        if (StringUtils.isEmpty(association.getTargetRolePlural()) && association.isTargetRolePluralRequired()) {
            association.setTargetRolePlural(association.getDefaultTargetRolePlural());
        }
    }
    private void updateDefaultTargetRoleSingular() {
        if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
            association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void setVisible(boolean visible) {
        if (visible){
            wizard.handleConfProdCmptTypeSelectionState();
        }
        super.setVisible(visible);
    }
}
