/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.pctype;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.internal.model.type.Type;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.TypeValidations;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation of IPolicyCmptType.
 * 
 * @author Jan Ortmann
 */
public class PolicyCmptType extends Type implements IPolicyCmptType {

    private boolean configurableByProductCmptType = false;

    private String productCmptType = ""; //$NON-NLS-1$

    private boolean forceExtensionCompilationUnitGeneration = false;

    private IpsObjectPartCollection rules;
    private IIpsObjectPart persistenceTypeInfo;

    public PolicyCmptType(IIpsSrcFile file) {
        super(file);
        rules = new IpsObjectPartCollection(this, ValidationRule.class, IValidationRule.class, ValidationRule.TAG_NAME);
        if (getIpsProject().isPersistenceSupportEnabled()) {
            persistenceTypeInfo = newPart(PersistentTypeInfo.class);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IpsObjectPartCollection createCollectionForMethods() {
        return new IpsObjectPartCollection(this, Method.class, IMethod.class, Method.XML_ELEMENT_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IpsObjectPartCollection createCollectionForAssociations() {
        return new IpsObjectPartCollection(this, PolicyCmptTypeAssociation.class, IPolicyCmptTypeAssociation.class,
                PolicyCmptTypeAssociation.TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IpsObjectPartCollection createCollectionForAttributes() {
        return new IpsObjectPartCollection(this, PolicyCmptTypeAttribute.class, IPolicyCmptTypeAttribute.class,
                PolicyCmptTypeAttribute.TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    public String getProductCmptType() {
        return productCmptType;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConfigurableByProductCmptType() {
        return configurableByProductCmptType;
    }

    /**
     * {@inheritDoc}
     */
    public void setConfigurableByProductCmptType(boolean newValue) {
        boolean oldValue = configurableByProductCmptType;
        configurableByProductCmptType = newValue;
        if (!configurableByProductCmptType) {
            setProductCmptType(""); //$NON-NLS-1$
        }
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findProductCmptType(getProductCmptType());
    }

    public void setProductCmptType(String newName) {
        ArgumentCheck.notNull(newName);
        String oldName = productCmptType;
        productCmptType = newName;
        valueChanged(oldName, newName);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isForceExtensionCompilationUnitGeneration() {
        return forceExtensionCompilationUnitGeneration;
    }

    /**
     * {@inheritDoc}
     */
    public void setForceExtensionCompilationUnitGeneration(boolean flag) {
        boolean oldValue = forceExtensionCompilationUnitGeneration;
        forceExtensionCompilationUnitGeneration = flag;
        valueChanged(oldValue, forceExtensionCompilationUnitGeneration);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExtensionCompilationUnitGenerated() {
        if (forceExtensionCompilationUnitGeneration) {
            return true;
        }
        if (getNumOfRules() > 0) {
            return true;
        }
        for (Iterator it = methods.iterator(); it.hasNext();) {
            IMethod method = (IMethod)it.next();
            if (!method.isAbstract()) {
                return true;
            }
        }
        for (Iterator it = attributes.iterator(); it.hasNext();) {
            IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)it.next();
            if (attribute.getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL) {
                if (!attribute.isProductRelevant()) {
                    return true;
                }
            } else if (attribute.getAttributeType() == AttributeType.DERIVED_ON_THE_FLY) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute[] getPolicyCmptTypeAttributes() {
        IPolicyCmptTypeAttribute[] a = new IPolicyCmptTypeAttribute[attributes.size()];
        attributes.toArray(a);
        return a;
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute getPolicyCmptTypeAttribute(String name) {
        return (IPolicyCmptTypeAttribute)getAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute findPolicyCmptTypeAttribute(String name, IIpsProject ipsProject)
            throws CoreException {
        return (IPolicyCmptTypeAttribute)findAttribute(name, ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute newPolicyCmptTypeAttribute() {
        return (IPolicyCmptTypeAttribute)newAttribute();
    }

    /*
     * Returns the list holding the attributes as a reference. Package private for use in
     * TypeHierarchy.
     */
    List getAttributeList() {
        return attributes.getBackingList();
    }

    /*
     * Returns the list holding the methods as a reference. Package private for use in
     * TypeHierarchy.
     */
    List getMethodList() {
        return methods.getBackingList();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws CoreException
     */
    public boolean isAggregateRoot() throws CoreException {
        IsAggregrateRootVisitor visitor = new IsAggregrateRootVisitor();
        visitor.start(this);
        return visitor.isRoot();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDependantType() throws CoreException {
        return !isAggregateRoot();
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAssociation[] getPolicyCmptTypeAssociations() {
        IPolicyCmptTypeAssociation[] r = new IPolicyCmptTypeAssociation[associations.size()];
        associations.toArray(r);
        return r;
    }

    /*
     * Returns the list holding the associations as a reference. Package private for use in
     * TypeHierarchy.
     */
    List getAssociationList() {
        return associations.getBackingList();
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAssociation newPolicyCmptTypeAssociation() {
        return (IPolicyCmptTypeAssociation)newAssociation();
    }

    /**
     * {@inheritDoc}
     */
    public IValidationRule[] getRules() {
        IValidationRule[] r = new IValidationRule[rules.size()];
        rules.toArray(r);
        return r;
    }

    /*
     * Returns the list holding the rules as a reference. Package private for use in TypeHierarchy.
     */
    List getRulesList() {
        return rules.getBackingList();
    }

    /**
     * {@inheritDoc}
     */
    public IValidationRule newRule() {
        return (IValidationRule)rules.newPart();
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfRules() {
        return rules.size();
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveRules(int[] indexes, boolean up) {
        return rules.moveParts(indexes, up);
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.POLICY_CMPT_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        configurableByProductCmptType = Boolean.valueOf(element.getAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE))
                .booleanValue();
        productCmptType = element.getAttribute(PROPERTY_PRODUCT_CMPT_TYPE);
        forceExtensionCompilationUnitGeneration = Boolean.valueOf(
                element.getAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU)).booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE, "" + configurableByProductCmptType); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_PRODUCT_CMPT_TYPE, productCmptType);
        newElement
                .setAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU, "" + forceExtensionCompilationUnitGeneration); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        validateProductSide(list, ipsProject);
        list.add(TypeValidations.validateOtherTypeWithSameNameTypeInIpsObjectPath(IpsObjectType.PRODUCT_CMPT_TYPE,
                getQualifiedName(), ipsProject, this));
        validateDuplicateRulesNames(list);
    }

    private void validateProductSide(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (isConfigurableByProductCmptType()) {
            if (StringUtils.isEmpty(productCmptType)) {
                String text = Messages.PolicyCmptType_msg_ProductCmptTypeNameMissing;
                list.add(new Message(MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING, text, Message.ERROR, this,
                        IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE));
            } else {
                IProductCmptType productCmptTypeObj = (IProductCmptType)ValidationUtils.checkIpsObjectReference2(
                        productCmptType, IpsObjectType.PRODUCT_CMPT_TYPE, Messages.PolicyCmptType_productCmptType,
                        this, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE,
                        IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_NOT_FOUND, list, ipsProject);
                if (productCmptTypeObj != null) {
                    if (productCmptTypeObj.findPolicyCmptType(ipsProject) != this) {
                        String text = NLS
                                .bind(Messages.PolicyCmptType_TheTypeDoesNotConfigureThisType, productCmptType);
                        list.add(new Message(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_DOES_NOT_CONFIGURE_THIS_TYPE,
                                text, Message.ERROR, this, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE));
                    }
                }
            }
            IPolicyCmptType superPolicyCmptType = (IPolicyCmptType)findSupertype(ipsProject);
            if (superPolicyCmptType != null) {
                if (!superPolicyCmptType.isConfigurableByProductCmptType()) {
                    String msg = Messages.PolicyCmptType_msg_IfTheSupertypeIsNotConfigurableTheTypeCanBeConfigurable;
                    list.add(new Message(MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT, msg,
                            Message.ERROR, this, IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE));
                }
            }
        }
        if (!isConfigurableByProductCmptType()) {
            IPolicyCmptType superPolicyCmptType = (IPolicyCmptType)findSupertype(ipsProject);
            if (superPolicyCmptType != null) {
                if (superPolicyCmptType.isConfigurableByProductCmptType()) {
                    list.add(new Message(MSGCODE_SUPERTYPE_CONFIGURABLE_FORCES_THIS_TYPE_IS_CONFIGURABLE,
                            Messages.PolicyCmptType_msgSubtypeConfigurableWhenSupertypeConfigurable, Message.ERROR,
                            this, IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE));
                }
            }
        }

    }

    public void validateDuplicateRulesNames(MessageList msgList) throws CoreException {
        for (IValidationRule rule : getRules()) {
            CheckValidationRuleVisitor visitor = new CheckValidationRuleVisitor(rule, msgList);
            visitor.start(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ITypeHierarchy getSupertypeHierarchy() throws CoreException {
        return TypeHierarchy.getSupertypeHierarchy(this);
    }

    /**
     * {@inheritDoc}
     */
    public ITypeHierarchy getSubtypeHierarchy() throws CoreException {
        return TypeHierarchy.getSubtypeHierarchy(this);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute[] findOverrideAttributeCandidates(IIpsProject ipsProject) throws CoreException {
        IPolicyCmptType supertype = (IPolicyCmptType)findSupertype(ipsProject);

        if (supertype == null) {
            // no supertype, no candidates :-)
            return new IPolicyCmptTypeAttribute[0];
        }

        // for easy finding attributes by name put them in a map with the name as key
        Map toExclude = new HashMap();
        for (Iterator iter = attributes.iterator(); iter.hasNext();) {
            IPolicyCmptTypeAttribute attr = (IPolicyCmptTypeAttribute)iter.next();
            if (attr.isOverwrite()) {
                toExclude.put(attr.getName(), attr);
            }
        }

        // find all overwrite-candidates
        IPolicyCmptTypeAttribute[] candidates = getSupertypeHierarchy().getAllAttributes(supertype);
        List result = new ArrayList();
        for (int i = 0; i < candidates.length; i++) {
            if (!toExclude.containsKey(candidates[i].getName())) {
                result.add(candidates[i]);
            }
        }

        return (IPolicyCmptTypeAttribute[])result.toArray(new IPolicyCmptTypeAttribute[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute[] overrideAttributes(IPolicyCmptTypeAttribute[] attributes) {
        IPolicyCmptTypeAttribute[] newAttributes = new IPolicyCmptTypeAttribute[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            IPolicyCmptTypeAttribute override = getPolicyCmptTypeAttribute(attributes[i].getName());

            if (override == null) {
                override = newPolicyCmptTypeAttribute();
                override.setDatatype(attributes[i].getDatatype());
                override.setProductRelevant(attributes[i].isProductRelevant());
                override.setName(attributes[i].getName());
                override.setDefaultValue(attributes[i].getDefaultValue());
                override.setValueSetCopy(attributes[i].getValueSet());
                override.setDescription(attributes[i].getDescription());
            }
            override.setOverwrite(true);
            newAttributes[i] = override;
        }
        return newAttributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IDependency[] dependsOn() throws CoreException {
        Set<IDependency> dependencies = new HashSet<IDependency>();
        if (!StringUtils.isEmpty(getProductCmptType())) {
            dependencies.add(IpsObjectDependency.createReferenceDependency(getQualifiedNameType(),
                    new QualifiedNameType(getProductCmptType(), IpsObjectType.PRODUCT_CMPT_TYPE)));
        }
        // to force a check is a product component type exists with the same qualified name
        dependencies.add(IpsObjectDependency.createReferenceDependency(getQualifiedNameType(), new QualifiedNameType(
                getQualifiedName(), IpsObjectType.PRODUCT_CMPT_TYPE)));
        dependsOn(dependencies);
        return dependencies.toArray(new IDependency[dependencies.size()]);
    }

    private static class IsAggregrateRootVisitor extends PolicyCmptTypeHierarchyVisitor {

        private boolean root = true;

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            IPolicyCmptTypeAssociation[] relations = currentType.getPolicyCmptTypeAssociations();
            for (int i = 0; i < relations.length; i++) {
                IPolicyCmptTypeAssociation each = relations[i];
                if (each.getAssociationType().isCompositionDetailToMaster()) {
                    root = false;
                    return false; // stop the visit, we have the result
                }
            }
            return true;
        }

        public boolean isRoot() {
            return root;
        }

    }

    private static class CheckValidationRuleVisitor extends PolicyCmptTypeHierarchyVisitor {

        private IValidationRule rule;
        private MessageList msgList;

        public CheckValidationRuleVisitor(IValidationRule rule, MessageList msgList) {
            super();
            this.rule = rule;
            this.msgList = msgList;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            for (IValidationRule validationRule : currentType.getRules()) {
                if (validationRule == rule) {
                    continue;
                }
                if (validationRule.getName().equals(rule.getName())) {
                    String text = "There exists another validation rule with the same name in this type or within the supertype hierarchy.";
                    msgList.add(new Message(IValidationRule.MSGCODE_DUPLICATE_RULE_NAME, text, Message.ERROR, rule,
                            IIpsElement.PROPERTY_NAME));
                }
            }
            for (IMethod method : currentType.getMethods()) {
                if (method.getNumOfParameters() == 0 && method.getName().equals(rule.getName())) {
                    String text = NLS
                            .bind(
                                    "The name of this validation rule: {0} collides with the name of a method within this type or within the supertype hierarchy.",
                                    rule.getName());
                    msgList.add(new Message(IValidationRule.MSGCODE_VALIDATION_RULE_METHOD_NAME_COLLISION, text,
                            Message.ERROR, rule, IIpsElement.PROPERTY_NAME));
                }
            }
            return true;
        }

    }

    public IPersistentTypeInfo getPersistenceTypeInfo() {
        return (IPersistentTypeInfo)persistenceTypeInfo;
    }

    // The methods below are overridden to allow a single IPersistenceTypeInfo instance be part of
    // this class. The default implementations handle only the case where the part is a
    // IIpsObjectPartCollection and not a single IIpsObjectPart.

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        ArgumentCheck.notNull(xmlTag);
        if (xmlTag.getTagName().equals(IPersistentTypeInfo.XML_TAG)) {
            return new PersistentTypeInfo(this, id);
        }
        return super.newPart(xmlTag, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IIpsObjectPart newPart(Class partType) {
        try {
            Constructor<? extends IPersistentTypeInfo> constructor = partType.getConstructor(IIpsObject.class,
                    int.class);
            IPersistentTypeInfo result = constructor.newInstance(this, getNextPartId());
            return result;
        } catch (Exception e) {
            IpsPlugin.log(e);
        }
        throw new IllegalArgumentException("Unsupported part type: " + partType);
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        if (IPersistentTypeInfo.class.isAssignableFrom(part.getClass())) {
            persistenceTypeInfo = part;
        } else {
            super.addPart(part);
        }
    }

    @Override
    protected void reinitPartCollections() {
        super.reinitPartCollections();
        newPart(PersistentTypeInfo.class);
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        if (persistenceTypeInfo.equals(part)) {
            newPart(PersistentTypeInfo.class);
        } else {
            super.removePart(part);
        }
    }

    @Override
    public IIpsElement[] getChildren() {
        List<IIpsElement> children = new ArrayList<IIpsElement>(Arrays.asList(super.getChildren()));
        // This is the only time, the model element could be null at instantiation time
        if (persistenceTypeInfo != null) {
            children.add(persistenceTypeInfo);
        }
        return children.toArray(new IIpsElement[children.size()]);
    }

}
