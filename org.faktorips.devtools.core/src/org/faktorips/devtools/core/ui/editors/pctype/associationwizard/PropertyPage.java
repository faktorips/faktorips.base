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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.ButtonTextBinding;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.util.QNameUtil;

public class PropertyPage extends WizardPage implements IBlockedValidationWizardPage {

    private NewPcTypeAssociationWizard wizard;
    private IPolicyCmptTypeAssociation association;
    private UIToolkit toolkit;
    private BindingContext bindingContext;

    private ArrayList visibleProperties = new ArrayList(10);
    
    private Text targetRoleSingularTextProdCmptType;
    private Text targetRolePluralTextProdCmptType;
    private CardinalityField cardinalityFieldMinProdCmptType;
    private CardinalityField cardinalityFieldMaxProdCmptType;
    private Checkbox checkboxProdCmtType;
    private PmoAssociation pmoAssociation;
    private PmoProductCmptTypeAssociation pmoProductCmptTypeAssociation;
    private IIpsProject ipsProject;
    
    protected PropertyPage(NewPcTypeAssociationWizard wizard, IPolicyCmptTypeAssociation association, UIToolkit toolkit, BindingContext bindingContext) {
        super("PropertyPage", "Association properties", null);
        super.setDescription("Define relation properties");
        this.wizard = wizard;
        this.association = association;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;
        
        this.ipsProject = association.getIpsProject();
        
        pmoAssociation = new PmoAssociation(association);
        pmoProductCmptTypeAssociation = new PmoProductCmptTypeAssociation(association);
        
        setPageComplete(false);
    }
    
    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        Composite workArea = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(1, false);
        workArea.setLayout(layout);
        
        Group groupGeneral = toolkit.createGroup(workArea, "Properties");
        createGeneralControls(groupGeneral);
        
        createQualificationGroup(toolkit.createGroup(workArea, "Qualification"));
        
        checkboxProdCmtType = toolkit.createCheckbox(workArea, "Create association on product cmpt type");
        bindingContext.bindEnabled(checkboxProdCmtType, pmoProductCmptTypeAssociation, PmoProductCmptTypeAssociation.PROPERTY_PRODUCTCMPTTYPE_AVAILABLE);
        bindingContext.bindContent(checkboxProdCmtType, pmoProductCmptTypeAssociation, PmoProductCmptTypeAssociation.PROPERTY_CREATEPRODUCTCMPTTYPEASSOCIATION);
        
        Group groupProduct = toolkit.createGroup(workArea, "Properties product side");
        createProductSideControls(groupProduct);
        setProductCmptTypeAssControlChangeable(false);
        
        setControl(workArea);
    }
    
    private void createQualificationGroup(Composite c) {
        Composite workArea = toolkit.createGridComposite(c, 1, true, true);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Checkbox qualifiedCheckbox = toolkit.createCheckbox(workArea);
        bindingContext.bindContent(qualifiedCheckbox, association, IAssociation.PROPERTY_QUALIFIED);
        bindingContext.bindEnabled(qualifiedCheckbox, pmoAssociation, PmoAssociation.PROPERTY_QUALIFICATION_POSSIBLE);
        Label note = toolkit.createFormLabel(workArea, StringUtils.rightPad("", 120));
        bindingContext.bindContent(note, pmoAssociation, PmoAssociation.PROPERTY_QUALIFICATION_NOTE);
        bindingContext.add(new ButtonTextBinding(qualifiedCheckbox, pmoAssociation, PmoAssociation.PROPERTY_QUALIFICATION_LABEL));
    }
    
    private void setProductCmptTypeAssControlChangeable(boolean changeable) {
        toolkit.setDataChangeable(targetRoleSingularTextProdCmptType, changeable);
        toolkit.setDataChangeable(targetRolePluralTextProdCmptType, changeable);
        toolkit.setDataChangeable(cardinalityFieldMinProdCmptType.getControl(), changeable);
        toolkit.setDataChangeable(cardinalityFieldMaxProdCmptType.getControl(), changeable);
    }

    private void createGeneralControls(Composite c) {
        Composite workArea = toolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // top extensions
        wizard.getExtFactoryAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_TOP);
        
        // role singular
        toolkit.createFormLabel(workArea, "Target role (singular):");
        final Text targetRoleSingularText = toolkit.createText(workArea);
        bindingContext.bindContent(targetRoleSingularText, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
                    association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
                }
            }
        });
        
        // role plural
        toolkit.createFormLabel(workArea, "Target role (plural):");
        final Text targetRolePluralText = toolkit.createText(workArea);
        bindingContext.bindContent(targetRolePluralText, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralText.getText()) && association.isTargetRolePluralRequired()) {
                    association.setTargetRolePlural(association.getDefaultTargetRolePlural());
                }
            }
        });
        
        // min cardinality
        toolkit.createFormLabel(workArea, "Minimum cardinality:");
        Text minCardinalityText = toolkit.createText(workArea);
        CardinalityField cardinalityField = new CardinalityField(minCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IPolicyCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        
        // max cardinality
        toolkit.createFormLabel(workArea, "Maximum cardinality:");
        Text maxCardinalityText = toolkit.createText(workArea);
        cardinalityField = new CardinalityField(maxCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IPolicyCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        
        // bottom extensions
        wizard.getExtFactoryAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_BOTTOM);
        wizard.getExtFactoryAssociation().bind(bindingContext);
    }
    
    private void createProductSideControls(Composite c) {
        Composite workArea = toolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // role singular
        toolkit.createFormLabel(workArea, "Target role (singular):");
        targetRoleSingularTextProdCmptType = toolkit.createText(workArea);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
//        targetRoleSingularText.addFocusListener(new FocusAdapter() {
//            public void focusGained(FocusEvent e) {
//                if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
//                    association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
//                }
//            }
//        });
        
        // role plural
        toolkit.createFormLabel(workArea, "Target role (plural):");
        targetRolePluralTextProdCmptType = toolkit.createText(workArea);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
//        targetRolePluralText.addFocusListener(new FocusAdapter() {
//            public void focusGained(FocusEvent e) {
//                if (StringUtils.isEmpty(targetRolePluralText.getText()) && association.isTargetRolePluralRequired()) {
//                    association.setTargetRolePlural(association.getDefaultTargetRolePlural());
//                }
//            }
//        });
        
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
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean canFlipToNextPage() {
        setErrorMessage(null);
        boolean valid = wizard.isValidPage(this, false);
        
        if (getNextPage() == null){
            return false;
        }
        
        return valid;
    }

    public List getProperties() {
        return visibleProperties;
    }

    public void setProductCmptTypeAssociation(IAssociation productCmptAssociation) {
        if (productCmptAssociation != null){
            bindingContext.bindContent(targetRoleSingularTextProdCmptType, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
            bindingContext.bindContent(targetRolePluralTextProdCmptType, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
            bindingContext.bindContent(cardinalityFieldMinProdCmptType, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
            bindingContext.bindContent(cardinalityFieldMaxProdCmptType, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        } else {
            bindingContext.removeBindings(targetRoleSingularTextProdCmptType);
            bindingContext.removeBindings(targetRolePluralTextProdCmptType);
            bindingContext.removeBindings(cardinalityFieldMinProdCmptType.getControl());
            bindingContext.removeBindings(cardinalityFieldMaxProdCmptType.getControl());
            targetRoleSingularTextProdCmptType.setText("");
            targetRolePluralTextProdCmptType.setText("");
            cardinalityFieldMinProdCmptType.setText("");
            cardinalityFieldMaxProdCmptType.setText("");            
        }
    }
    
    public class PmoProductCmptTypeAssociation extends IpsObjectPartPmo {
        public final static String PROPERTY_PRODUCTCMPTTYPE_AVAILABLE = "productCmptTypeAvailable";
        public final static String PROPERTY_CREATEPRODUCTCMPTTYPEASSOCIATION = "createProductCmptTypeAssociation";
        
        private boolean createProductCmptTypeAssociation;
        
        public PmoProductCmptTypeAssociation(IPolicyCmptTypeAssociation association) {
            super(association);
        }
        
        /**
         * @return Returns the createProductCmptTypeAssociation.
         */
        public boolean isCreateProductCmptTypeAssociation() {
            return createProductCmptTypeAssociation;
        }

        /**
         * @param createProductCmptTypeAssociation The createProductCmptTypeAssociation to set.
         */
        public void setCreateProductCmptTypeAssociation(boolean createProductCmptTypeAssociation) {
            this.createProductCmptTypeAssociation = createProductCmptTypeAssociation;
            if (createProductCmptTypeAssociation) {
                wizard.storeMementoProductCmptTypeBeforeChange();
                wizard.newProductCmptTypeAssociation();
                setProductCmptTypeAssControlChangeable(true);
            } else {
                wizard.restoreMementoProductCmptTypeBeforeChange();
                setProductCmptTypeAssociation(null);
                setProductCmptTypeAssControlChangeable(false);
            }
            bindingContext.updateUI();
            notifyListeners();
        }

        public boolean isProductCmptTypeAvailable(){
            IPolicyCmptType targetPolicyCmptType = wizard.getTargetPolicyCmptType();
            if (targetPolicyCmptType == null){
                return false;
            }
            boolean confByProdCmptTypeEnabled = wizard.findProductCmptType() != null;
            if (confByProdCmptTypeEnabled){
                IProductCmptType productCmptTypeTarget;
                try {
                    productCmptTypeTarget = targetPolicyCmptType.findProductCmptType(ipsProject);
                } catch (CoreException e) {
                    wizard.showAndLogError(e);
                    return false;
                }
                if (productCmptTypeTarget != null){
                    return true;
                }
            }
            return false;
        }
    }
    
    public class PmoAssociation extends IpsObjectPartPmo {

        public final static String PROPERTY_SUBSET = "subset";
        public final static String PROPERTY_QUALIFICATION_LABEL = "qualificationLabel";
        public final static String PROPERTY_QUALIFICATION_NOTE = "qualificationNote";
        public final static String PROPERTY_QUALIFICATION_POSSIBLE = "qualificationPossible";
        public final static String PROPERTY_CONSTRAINED_NOTE = "constrainedNote";

        private boolean subset;
        
        public PmoAssociation(IPolicyCmptTypeAssociation association) {
            super(association);
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
        
        public String getQualificationLabel() {
            String label = "This association is qualified";
            try {
                String productCmptType = QNameUtil.getUnqualifiedName(association.findQualifierCandidate(ipsProject));
                if (StringUtils.isNotEmpty(productCmptType)) {
                    label = label + " by type '" + productCmptType + "'";
                }
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return StringUtils.rightPad(label, 80);            
        }

        public String getQualificationNote() {
            String note = "Note: ";
            if (!association.isCompositionMasterToDetail()) {
                note = note + "Qualification is only applicable for compositions (master to detail).";
            } else {
                try {
                    if (!association.isQualificationPossible(ipsProject)) {
                        note = note + "Qualification is only applicable, if the target type is configurable by a product.";
                    } else {
                        note = note + "For qualified associations multiplicty is defined per qualified instance.";
                    }
                }
                catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
            return StringUtils.rightPad(note, 90);
        }
        
        public boolean isQualificationPossible() {
            try {
                return association.isQualificationPossible(ipsProject);
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
                return false;
            }
        }

        public String getConstrainedNote() {
            try {
                if (association.isCompositionDetailToMaster()) {
                    return StringUtils.rightPad("", 120) + StringUtils.rightPad("\n", 120) + StringUtils.right("\n", 120);
                }
                IProductCmptTypeAssociation matchingAss = association.findMatchingProductCmptTypeAssociation(ipsProject);
                if (matchingAss!=null) {
                    String type = matchingAss.getProductCmptType().getName();
                    return "Note: This association is constrained by product structure. " 
                    +" The matching \nassociation in type '" + type + "' is '" + matchingAss.getTargetRoleSingular() + "' (rolename)."
                    + StringUtils.rightPad("\n", 120); 
                } else {
                    String note = "Note: This association is not constrained by product structure."; 
                    IProductCmptType sourceProductType = association.getPolicyCmptType().findProductCmptType(ipsProject);
                    IPolicyCmptType targetType = association.findTargetPolicyCmptType(ipsProject);
                    if (sourceProductType!=null && targetType!=null) {
                        IProductCmptType targetProductType = targetType.findProductCmptType(ipsProject);
                        if (targetProductType!=null) {
                            return note + "\nTo constrain the association by product structure, create an association between the "
                                + "\nproduct component types '" + sourceProductType.getName() + "' and '" + targetProductType.getName() + "'.";
                        }
                    }
                    return note + StringUtils.rightPad("\n", 120) + StringUtils.rightPad("\n", 120) ;
                }
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
                return "";
            }
            
        }

        /**
         * {@inheritDoc}
         */
        protected void partHasChanged() {
            if (association.isCompositionDetailToMaster()) {
                subset = false;
            }
        }
    }
    
    
}
