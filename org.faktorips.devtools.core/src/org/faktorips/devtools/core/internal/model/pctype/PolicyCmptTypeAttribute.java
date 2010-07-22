/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.internal.model.pctype;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.type.Attribute;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.DatatypeUtil;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAttribute.
 */
public class PolicyCmptTypeAttribute extends Attribute implements IPolicyCmptTypeAttribute {

    final static String TAG_NAME = "Attribute"; //$NON-NLS-1$

    // member variables.
    private boolean productRelevant = false;
    private AttributeType attributeType = AttributeType.CHANGEABLE;
    private IValueSet valueSet;
    private boolean overwrites = false;
    private String computationMethodSignature = ""; //$NON-NLS-1$
    private IIpsObjectPart persistenceAttributeInfo;

    /**
     * Creates a new attribute.
     * 
     * @param pcType The type the attribute belongs to.
     * @param id The attribute's unique id within the type.
     */
    public PolicyCmptTypeAttribute(PolicyCmptType pcType, String id) {
        super(pcType, id);
        valueSet = new UnrestrictedValueSet(this, getNextPartId());
        if (pcType.getIpsProject().isPersistenceSupportEnabled()) {
            persistenceAttributeInfo = newPart(PersistentAttributeInfo.class);
        }
    }

    @Override
    public IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getIpsObject();
    }

    @Override
    public IPolicyCmptTypeAttribute findOverwrittenAttribute(IIpsProject ipsProject) throws CoreException {
        IType supertype = getPolicyCmptType().findSupertype(ipsProject);
        if (supertype == null) {
            return null;
        }
        IAttribute candidate = supertype.findAttribute(name, ipsProject);
        if (candidate == this) {
            return null; // can happen if we have a cycle in the type hierarchy!
        }
        return (IPolicyCmptTypeAttribute)candidate;
    }

    @Override
    public void setAttributeType(AttributeType newType) {
        AttributeType oldType = attributeType;
        attributeType = newType;
        valueChanged(oldType, newType);
    }

    @Override
    public IValidationRule findValueSetRule(IIpsProject ipsProject) {
        IValidationRule[] rules = getPolicyCmptType().getRules();

        for (IValidationRule rule : rules) {
            String[] attributes = rule.getValidatedAttributes();
            for (String attribute : attributes) {
                if (attribute.equals(getName()) && rule.isCheckValueAgainstValueSetRule()) {
                    return rule;
                }
            }
        }
        return null;
    }

    @Override
    public IValidationRule createValueSetRule() {
        IValidationRule rule = findValueSetRule(getIpsProject());
        if (rule != null) {
            return rule;
        }
        rule = getPolicyCmptType().newRule();
        rule.setName(getProposalValueSetRuleName());
        rule.addValidatedAttribute(getName());
        rule.setMessageCode(getProposalMsgCodeForValueSetRule());
        rule.setCheckValueAgainstValueSetRule(true);
        rule.setAppliedForAllBusinessFunctions(true);
        rule.setValidatedAttrSpecifiedInSrc(false);
        return rule;
    }

    @Override
    public void deleteValueSetRule() {
        IValidationRule rule = findValueSetRule(getIpsProject());
        if (rule != null) {
            rule.delete();
        }
    }

    @Override
    public AttributeType getAttributeType() {
        return attributeType;
    }

    @Override
    public boolean isChangeable() {
        return getAttributeType() == AttributeType.CHANGEABLE;
    }

    @Override
    public boolean isDerived() {
        return getAttributeType().isDerived();
    }

    @Override
    public IProductCmptTypeMethod findComputationMethod(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(computationMethodSignature)) {
            return null;
        }
        IProductCmptType productCmptType = getPolicyCmptType().findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return null;
        }
        return (IProductCmptTypeMethod)productCmptType.findMethod(computationMethodSignature, ipsProject);
    }

    @Override
    public boolean isProductRelevant() {
        return productRelevant;
    }

    @Override
    public void setProductRelevant(boolean newValue) {
        boolean oldValue = productRelevant;
        productRelevant = newValue;
        if (oldValue != newValue && !newValue) {
            computationMethodSignature = ""; //$NON-NLS-1$
        }
        valueChanged(oldValue, newValue);
    }

    @Override
    public IValueSet getValueSet() {
        return valueSet;
    }

    @Override
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException {
        List<ValueSetType> types = ipsProject.getValueSetTypes(findDatatype(ipsProject));
        for (Iterator<ValueSetType> it = types.iterator(); it.hasNext();) {
            ValueSetType valueSetType = it.next();
            if (valueSetType.isEnum()) {
                ValueDatatype datatype = findDatatype(ipsProject);
                if (DatatypeUtil.isEnumTypeWithSeparateContent(datatype)) {
                    it.remove();
                }
            }
        }
        return types;
    }

    @Override
    public void setValueSetType(ValueSetType type) {
        if (valueSet != null && type == valueSet.getValueSetType()) {
            return;
        }
        valueSet = type.newValueSet(this, getNextPartId());
        objectHasChanged();
    }

    @Override
    public IValueSet changeValueSetType(ValueSetType newType) {
        setValueSetType(newType);
        return valueSet;
    }

    @Override
    public boolean isValueSetUpdateable() {
        return true;
    }

    @Override
    public String getComputationMethodSignature() {
        return computationMethodSignature;
    }

    @Override
    public void setComputationMethodSignature(String newMethodName) {
        String oldName = computationMethodSignature;
        computationMethodSignature = newMethodName;
        valueChanged(oldName, newMethodName);
    }

    // TODO internationalize messages
    @Override
    protected void validateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        super.validateThis(result, ipsProject);
        if (isProductRelevant() && !getPolicyCmptType().isConfigurableByProductCmptType()) {
            String text = Messages.Attribute_msgAttributeCantBeProductRelevantIfTypeIsNot;
            result.add(new Message(MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT, text, Message.ERROR,
                    this, PROPERTY_PRODUCT_RELEVANT));
        }

        if (isDerived() && isProductRelevant()) {
            if (StringUtils.isEmpty(computationMethodSignature)) {
                String text = NLS.bind(Messages.PolicyCmptTypeAttribute_msg_ComputationMethodSignatureIsMissing,
                        getName());
                result.add(new Message(MSGCODE_COMPUTATION_METHOD_NOT_SPECIFIED, text, Message.ERROR, this,
                        PROPERTY_COMPUTATION_METHOD_SIGNATURE));
            } else {
                IMethod computationMethod = findComputationMethod(ipsProject);
                if (computationMethod == null) {
                    String text = Messages.PolicyCmptTypeAttribute_msg_ComputationMethodSignatureDoesNotExists;
                    result.add(new Message(MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST, text, Message.ERROR, this,
                            PROPERTY_COMPUTATION_METHOD_SIGNATURE));
                } else {
                    ValueDatatype attributeDataype = findDatatype(ipsProject);
                    if (attributeDataype != null
                            && !attributeDataype.equals(computationMethod.findDatatype(ipsProject))) {
                        String text = Messages.PolicyCmptTypeAttribute_msg_ComputationMethodSignatureHasADifferentDatatype;
                        result.add(new Message(MSGCODE_COMPUTATION_MEHTOD_HAS_DIFFERENT_DATATYPE, text, Message.ERROR,
                                this, new String[] { PROPERTY_DATATYPE, PROPERTY_COMPUTATION_METHOD_SIGNATURE }));
                    }
                }
            }
        }

        IPolicyCmptTypeAttribute superAttr = findOverwrittenAttribute(ipsProject);
        if (overwrites) {
            if (superAttr == null) {
                String text = NLS.bind(Messages.Attribute_msgNothingToOverwrite, getName());
                result.add(new Message(MSGCODE_NOTHING_TO_OVERWRITE, text, Message.ERROR, this, new String[] {
                        PROPERTY_OVERWRITES, PROPERTY_NAME }));
            } else {
                if (!valueSet.isDetailedSpecificationOf(superAttr.getValueSet())) {
                    String text = "The value set defined in this attribute must be a more detailed specification of the value set in the overridden attribute (but it is not).";
                    result.add(new Message("", text, Message.ERROR, this)); //$NON-NLS-1$
                }
                if (!attributeType.equals(superAttr.getAttributeType())) {
                    String text = Messages.PolicyCmptTypeAttribute_TypeOfOverwrittenAttributeCantBeChanged;
                    result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_TYPE, text, Message.ERROR, this,
                            new String[] { PROPERTY_ATTRIBUTE_TYPE }));
                }
            }
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        overwrites = Boolean.valueOf(element.getAttribute(PROPERTY_OVERWRITES)).booleanValue();
        productRelevant = Boolean.valueOf(element.getAttribute(PROPERTY_PRODUCT_RELEVANT)).booleanValue();
        attributeType = AttributeType.getAttributeType(element.getAttribute(PROPERTY_ATTRIBUTE_TYPE));
        computationMethodSignature = element.getAttribute(PROPERTY_COMPUTATION_METHOD_SIGNATURE);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_OVERWRITES, "" + overwrites); //$NON-NLS-1$
        element.setAttribute(PROPERTY_PRODUCT_RELEVANT, "" + productRelevant); //$NON-NLS-1$
        element.setAttribute(PROPERTY_ATTRIBUTE_TYPE, attributeType.getId());
        element.setAttribute(PROPERTY_COMPUTATION_METHOD_SIGNATURE, "" + computationMethodSignature); //$NON-NLS-1$
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = (IValueSet)part;
        } else {
            persistenceAttributeInfo = part;
        }
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        if (xmlTag.getNodeName().equals(ValueSet.XML_TAG)) {
            valueSet = ValueSetType.newValueSet(xmlTag, this, id);
            return valueSet;
        }
        if (xmlTag.getTagName().equals(IPersistentAttributeInfo.XML_TAG)) {
            return newPersistentAttributeInfoInternal(this, id);
        }
        return null;
    }

    private IIpsObjectPart newPersistentAttributeInfoInternal(PolicyCmptTypeAttribute policyCmptTypeAttribute, String id) {
        persistenceAttributeInfo = new PersistentAttributeInfo(this, id);
        return persistenceAttributeInfo;
    }

    // TODO Joerg Merge Persistence Branch: warum ueber Reflection und nicht
    // instanceof wie z.B. in
    // org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType.newPart(Class)
    @Override
    public IIpsObjectPart newPart(Class partType) {
        try {
            Constructor<? extends IIpsObjectPart> constructor = partType.getConstructor(IIpsObjectPart.class,
                    String.class);
            IIpsObjectPart result = constructor.newInstance(this, getNextPartId());
            return result;
        } catch (Exception e) {
            // TODO needs to be documented properly or specialized
            IpsPlugin.log(e);
        }
        throw new IllegalArgumentException("Unsupported part type: " + partType); //$NON-NLS-1$
    }

    @Override
    public IIpsElement[] getChildren() {
        List<IIpsElement> children = new ArrayList<IIpsElement>();
        if (valueSet != null) {
            children.add(valueSet);
        }
        if (persistenceAttributeInfo != null) {
            children.add(persistenceAttributeInfo);
        }
        return children.toArray(new IIpsElement[children.size()]);
    }

    public ValueDatatype getValueDatatype() {
        try {
            // TODO v2 - signature getValueDatatype() is wrong
            Datatype type = findDatatype(getIpsProject());
            if (type != null) {
                return (ValueDatatype)type;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    @Override
    public boolean isOverwrite() {
        return overwrites;
    }

    @Override
    public void setOverwrite(boolean overwrites) {
        boolean old = this.overwrites;
        this.overwrites = overwrites;
        valueChanged(old, overwrites);
    }

    @Override
    public void setValueSetCopy(IValueSet source) {
        IValueSet oldset = valueSet;
        valueSet = source.copy(this, getNextPartId());
        valueChanged(oldset, valueSet);
    }

    @Override
    protected void reinitPartCollections() {
        // Nothing to do.
        // TODO Joerg Merge PersistenceBranch: wirklich nicht valueSet neu anlegen?
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        valueSet = new UnrestrictedValueSet(this, getNextPartId());
    }

    @Override
    public String getProposalValueSetRuleName() {
        return NLS.bind(Messages.Attribute_proposalForRuleName, StringUtils.capitalize(getName()));
    }

    @Override
    public String getProposalMsgCodeForValueSetRule() {
        return NLS.bind(Messages.Attribute_proposalForMsgCode, getName().toUpperCase());
    }

    @Override
    public String getPropertyName() {
        if (productRelevant) {
            return name;
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public ProdDefPropertyType getProdDefPropertyType() {
        return ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET;
    }

    @Override
    public String getPropertyDatatype() {
        return getDatatype();
    }

    @Override
    public IPersistentAttributeInfo getPersistenceAttributeInfo() {
        return (IPersistentAttributeInfo)persistenceAttributeInfo;
    }

}
