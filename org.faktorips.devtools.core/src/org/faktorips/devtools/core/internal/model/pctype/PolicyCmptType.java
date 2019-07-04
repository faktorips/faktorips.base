/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.internal.model.method.BaseMethod;
import org.faktorips.devtools.core.internal.model.type.DuplicatePropertyNameValidator;
import org.faktorips.devtools.core.internal.model.type.Type;
import org.faktorips.devtools.core.model.DependencyType;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ITableNamingStrategy;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeMethod;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.model.type.TypeValidations;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
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

    private IpsObjectPartCollection<IValidationRule> rules;

    private IPersistentTypeInfo persistenceTypeInfo;

    private IpsObjectPartCollection<IPolicyCmptTypeMethod> methods;
    private IpsObjectPartCollection<IPolicyCmptTypeAssociation> associations;
    private IpsObjectPartCollection<IPolicyCmptTypeAttribute> attributes;

    public PolicyCmptType(IIpsSrcFile file) {
        super(file);
        methods = new IpsObjectPartCollection<IPolicyCmptTypeMethod>(this, PolicyCmptTypeMethod.class,
                IPolicyCmptTypeMethod.class, BaseMethod.XML_ELEMENT_NAME);
        associations = new IpsObjectPartCollection<IPolicyCmptTypeAssociation>(this, PolicyCmptTypeAssociation.class,
                IPolicyCmptTypeAssociation.class, PolicyCmptTypeAssociation.TAG_NAME);
        attributes = new IpsObjectPartCollection<IPolicyCmptTypeAttribute>(this, PolicyCmptTypeAttribute.class,
                IPolicyCmptTypeAttribute.class, PolicyCmptTypeAttribute.TAG_NAME);
        rules = new IpsObjectPartCollection<IValidationRule>(this, ValidationRule.class, IValidationRule.class,
                ValidationRule.TAG_NAME);
        internalInitPersistenceTypeInfo();
    }

    @Override
    protected IpsObjectPartCollection<IPolicyCmptTypeAssociation> getAssociationPartCollection() {
        return associations;
    }

    @Override
    protected IpsObjectPartCollection<IPolicyCmptTypeAttribute> getAttributesPartCollection() {
        return attributes;
    }

    @Override
    protected IpsObjectPartCollection<IPolicyCmptTypeMethod> getMethodPartCollection() {
        return methods;
    }

    @Override
    public IPolicyCmptTypeAssociation getAssociation(String name) {
        return (IPolicyCmptTypeAssociation)super.getAssociation(name);
    }

    @Override
    public IPolicyCmptTypeAssociation getAssociationByRoleNamePlural(String roleNamePlural) {
        return (IPolicyCmptTypeAssociation)super.getAssociationByRoleNamePlural(roleNamePlural);
    }

    @Override
    public String getProductCmptType() {
        return productCmptType;
    }

    @Override
    public boolean isConfigurableByProductCmptType() {
        return configurableByProductCmptType;
    }

    @Override
    public void setConfigurableByProductCmptType(boolean newValue) {
        boolean oldValue = configurableByProductCmptType;
        configurableByProductCmptType = newValue;
        if (!configurableByProductCmptType) {
            setProductCmptType(""); //$NON-NLS-1$
        }
        valueChanged(oldValue, newValue);
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) {
        return ipsProject.findProductCmptType(getProductCmptType());
    }

    @Override
    public void setProductCmptType(String newName) {
        ArgumentCheck.notNull(newName);
        String oldName = productCmptType;
        productCmptType = newName;
        valueChanged(oldName, newName);
    }

    @Override
    public boolean isForceExtensionCompilationUnitGeneration() {
        return forceExtensionCompilationUnitGeneration;
    }

    @Override
    public void setForceExtensionCompilationUnitGeneration(boolean flag) {
        boolean oldValue = forceExtensionCompilationUnitGeneration;
        forceExtensionCompilationUnitGeneration = flag;
        valueChanged(oldValue, forceExtensionCompilationUnitGeneration);
    }

    @Override
    public boolean isExtensionCompilationUnitGenerated() {
        if (forceExtensionCompilationUnitGeneration) {
            return true;
        }
        if (getNumOfRules() > 0) {
            return true;
        }
        for (IMethod method : methods) {
            if (!method.isAbstract()) {
                return true;
            }
        }
        for (IAttribute attr : attributes) {
            IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)attr;
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

    @Override
    public List<IPolicyCmptTypeAttribute> getPolicyCmptTypeAttributes() {
        return attributes.asList();
    }

    @Override
    public List<IProductCmptProperty> getProductCmptProperties(ProductCmptPropertyType propertyType) {
        List<IProductCmptProperty> properties = new ArrayList<IProductCmptProperty>();
        if (propertyType == null || propertyType.equals(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE)) {
            collectAttributeProductCmptProperties(properties);
        }
        if (propertyType == null || propertyType.equals(ProductCmptPropertyType.VALIDATION_RULE)) {
            collectValidationRuleProductCmptProperties(properties);
        }
        return properties;
    }

    private void collectAttributeProductCmptProperties(List<IProductCmptProperty> properties) {
        for (IPolicyCmptTypeAttribute attribute : getPolicyCmptTypeAttributes()) {
            if (attribute.isProductRelevant() && attribute.isChangeable()) {
                properties.add(attribute);
            }
        }
    }

    private void collectValidationRuleProductCmptProperties(List<IProductCmptProperty> properties) {
        for (IValidationRule validationRule : getValidationRules()) {
            if (validationRule.isConfigurableByProductComponent()) {
                properties.add(validationRule);
            }
        }
    }

    @Override
    public IPolicyCmptTypeAttribute getPolicyCmptTypeAttribute(String name) {
        return (IPolicyCmptTypeAttribute)getAttribute(name);
    }

    @Override
    public IPolicyCmptTypeAttribute findPolicyCmptTypeAttribute(String name, IIpsProject ipsProject) {
        return (IPolicyCmptTypeAttribute)findAttribute(name, ipsProject);
    }

    @Override
    public IPolicyCmptTypeAttribute newPolicyCmptTypeAttribute() {
        return (IPolicyCmptTypeAttribute)newAttribute();
    }

    @Override
    public IPolicyCmptTypeAttribute newPolicyCmptTypeAttribute(String name) {
        IPolicyCmptTypeAttribute attribute = newPolicyCmptTypeAttribute();
        attribute.setName(name);
        return attribute;
    }

    @Override
    public boolean isAggregateRoot() throws CoreException {
        IsAggregrateRootVisitor visitor = new IsAggregrateRootVisitor(getIpsProject());
        visitor.start(this);
        return visitor.isRoot();
    }

    @Override
    public boolean isDependantType() throws CoreException {
        return !isAggregateRoot();
    }

    @Override
    public List<IPolicyCmptTypeAssociation> getPolicyCmptTypeAssociations() {
        return associations.asList();
    }

    @Override
    public IPolicyCmptTypeAssociation newPolicyCmptTypeAssociation() {
        return (IPolicyCmptTypeAssociation)newAssociation();
    }

    @Override
    public List<IValidationRule> getValidationRules() {
        return rules.asList();
    }

    @Override
    public IValidationRule getValidationRule(String ruleName) {
        return rules.getPartByName(ruleName);
    }

    @Override
    public IValidationRule newRule() {
        return rules.newPart();
    }

    @Override
    public int getNumOfRules() {
        return rules.size();
    }

    @Override
    public int[] moveRules(int[] indexes, boolean up) {
        return rules.moveParts(indexes, up);
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.POLICY_CMPT_TYPE;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        configurableByProductCmptType = Boolean.valueOf(element.getAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE))
                .booleanValue();
        productCmptType = element.getAttribute(PROPERTY_PRODUCT_CMPT_TYPE);
        forceExtensionCompilationUnitGeneration = Boolean
                .valueOf(element.getAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU)).booleanValue();
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE, "" + configurableByProductCmptType); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_PRODUCT_CMPT_TYPE, productCmptType);
        newElement.setAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU,
                "" + forceExtensionCompilationUnitGeneration); //$NON-NLS-1$
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        validateProductSide(list, ipsProject);
        list.add(TypeValidations.validateOtherTypeWithSameNameTypeInIpsObjectPath(IpsObjectType.PRODUCT_CMPT_TYPE,
                getQualifiedName(), ipsProject, this));
        validateDuplicateRulesNames(list);
    }

    private void validateProductSide(MessageList list, IIpsProject ipsProject) {
        if (isConfigurableByProductCmptType()) {
            if (StringUtils.isEmpty(productCmptType)) {
                String text = Messages.PolicyCmptType_msg_ProductCmptTypeNameMissing;
                list.add(new Message(MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING, text, Message.ERROR, this,
                        IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE));
            } else {
                IProductCmptType productCmptTypeObj = (IProductCmptType)ValidationUtils.checkAndGetIpsObjectReference(
                        productCmptType, IpsObjectType.PRODUCT_CMPT_TYPE, Messages.PolicyCmptType_productCmptType, this,
                        IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE, IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_NOT_FOUND,
                        list, ipsProject);
                if (productCmptTypeObj != null) {
                    if (productCmptTypeObj.findPolicyCmptType(ipsProject) != this) {
                        String text = NLS.bind(Messages.PolicyCmptType_TheTypeDoesNotConfigureThisType,
                                productCmptType);
                        list.add(new Message(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_DOES_NOT_CONFIGURE_THIS_TYPE,
                                text, Message.ERROR, this, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE));
                    }
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

    public void validateDuplicateRulesNames(MessageList msgList) {
        for (IValidationRule rule : getValidationRules()) {
            CheckValidationRuleVisitor visitor = new CheckValidationRuleVisitor(getIpsProject(), rule, msgList);
            visitor.start(this);
        }
    }

    @Override
    protected List<IAssociation> findAssociationsForTargetAndAssociationTypeInternal(String target,
            AssociationType associationType,
            IIpsProject project) throws CoreException {
        List<IAssociation> result = super.findAssociationsForTargetAndAssociationTypeInternal(target, associationType,
                project);
        if (getIpsProject().getReadOnlyProperties().isSharedDetailToMasterAssociations()) {
            IType targetType = project.findPolicyCmptType(target);
            for (IPolicyCmptTypeAssociation association : getAssociationPartCollection()) {
                if (association.isSharedAssociation()) {
                    IType sharedTarget = association.findTarget(getIpsProject());
                    if (targetType.isSubtypeOf(sharedTarget, getIpsProject())) {
                        result.add(association);
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) {
        Set<IDependency> dependencies = new HashSet<IDependency>();
        if (!StringUtils.isEmpty(getProductCmptType())) {
            IDependency dependency = IpsObjectDependency.createConfiguredByDependency(getQualifiedNameType(),
                    new QualifiedNameType(getProductCmptType(), IpsObjectType.PRODUCT_CMPT_TYPE));
            dependencies.add(dependency);
            addDetails(details, dependency, this, PROPERTY_PRODUCT_CMPT_TYPE);
        }
        dependsOnAddValidationDependency(dependencies);

        if (!isConfigurableByProductCmptType()) {
            /*
             * Adding dependency for explicitly specified matching associations for differing policy
             * and product structure. @see FIPS-563
             */
            for (IPolicyCmptTypeAssociation association : getPolicyCmptTypeAssociations()) {
                if (association.isConstrainedByProductStructure(getIpsProject())) {
                    IpsObjectDependency dependency = IpsObjectDependency.createReferenceDependency(
                            getQualifiedNameType(), new QualifiedNameType(association.getMatchingAssociationSource(),
                                    IpsObjectType.PRODUCT_CMPT_TYPE));
                    dependencies.add(dependency);
                    addDetails(details, dependency, association,
                            IPolicyCmptTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE);
                }
            }
        }

        super.dependsOn(dependencies, details);
        return dependencies.toArray(new IDependency[dependencies.size()]);
    }

    /**
     * Adding a validation dependency to force a check if a product component type exists with the
     * same qualified name.
     * 
     * @param dependencies is the result set which will contain all dependencies
     */
    private void dependsOnAddValidationDependency(Set<IDependency> dependencies) {
        dependencies.add(IpsObjectDependency.create(getQualifiedNameType(),
                new QualifiedNameType(getQualifiedName(), IpsObjectType.PRODUCT_CMPT_TYPE), DependencyType.VALIDATION));
    }

    @Override
    public IPersistentTypeInfo getPersistenceTypeInfo() {
        return persistenceTypeInfo;
    }

    @Override
    public boolean isPersistentEnabled() {
        return getPersistenceTypeInfo() != null && getPersistenceTypeInfo().isEnabled();
    }

    // The methods below are overridden to allow a single IPersistenceTypeInfo instance be part of
    // this class. The default implementations handle only the case where the part is a
    // IIpsObjectPartCollection and not a single IIpsObjectPart.

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        if (xmlTag.getTagName().equals(IPersistentTypeInfo.XML_TAG)) {
            return newPersistentTypeInfoInternal(id);
        }
        return super.newPartThis(xmlTag, id);
    }

    @Override
    public IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        if (partType == PersistentTypeInfo.class) {
            return newPersistentTypeInfoInternal(getNextPartId());
        }
        return super.newPartThis(partType);
    }

    /**
     * Creates a new persistent type info for this policy component type
     */
    private IIpsObjectPart newPersistentTypeInfoInternal(String id) {
        persistenceTypeInfo = new PersistentTypeInfo(this, id);
        return persistenceTypeInfo;
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (IPersistentTypeInfo.class.isAssignableFrom(part.getClass())) {
            persistenceTypeInfo = (IPersistentTypeInfo)part;
            return true;
        }
        return super.addPartThis(part);
    }

    @Override
    protected void reinitPartCollectionsThis() {
        super.reinitPartCollectionsThis();
        internalInitPersistenceTypeInfo();
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (PersistentTypeInfo.class.isAssignableFrom(part.getClass())) {
            persistenceTypeInfo = newPart(PersistentTypeInfo.class);
            return true;
        }
        return super.removePartThis(part);
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        IIpsElement[] superChildren = super.getChildrenThis();
        if (persistenceTypeInfo != null) {
            List<IIpsElement> children = new ArrayList<IIpsElement>(Arrays.asList(superChildren));
            children.add(persistenceTypeInfo);
            return children.toArray(new IIpsElement[children.size()]);
        }
        return superChildren;
    }

    @Override
    public void initPersistentTypeInfo() throws CoreException {
        if (!getIpsProject().isPersistenceSupportEnabled()) {
            throw new CoreException(new IpsStatus(
                    "Cannot initialize persistence information because the IPS Project is not persistent.")); //$NON-NLS-1$
        }

        ITableNamingStrategy tableNamingStrategy = getIpsProject().getTableNamingStrategy();

        IPolicyCmptType rootEntity = persistenceTypeInfo.findRootEntity();
        if (rootEntity != null && rootEntity != this) {
            IPolicyCmptType pcSupertype = (IPolicyCmptType)findSupertype(getIpsProject());
            if (pcSupertype != null && pcSupertype.isPersistentEnabled()) {
                IPersistentTypeInfo pcSupertypeInfo = pcSupertype.getPersistenceTypeInfo();
                persistenceTypeInfo.setInheritanceStrategy(pcSupertypeInfo.getInheritanceStrategy());
                if (pcSupertypeInfo.getInheritanceStrategy() == InheritanceStrategy.JOINED_SUBCLASS) {
                    persistenceTypeInfo.setTableName(tableNamingStrategy.getTableName(getName()));
                } else {
                    persistenceTypeInfo.setTableName(tableNamingStrategy.getTableName(pcSupertype.getName()));
                }
                persistenceTypeInfo.setDiscriminatorDatatype(pcSupertypeInfo.getDiscriminatorDatatype());
            } else {
                persistenceTypeInfo.setTableName(tableNamingStrategy.getTableName(getName()));
                persistenceTypeInfo.setDiscriminatorValue(getName());
            }
        } else {
            persistenceTypeInfo.setDefinesDiscriminatorColumn(true);
            persistenceTypeInfo.setTableName(tableNamingStrategy.getTableName(getName()));
            persistenceTypeInfo.setDiscriminatorValue(getName());
        }
    }

    private void internalInitPersistenceTypeInfo() {
        IIpsProject ipsProject = getIpsProject();
        if (ipsProject == null || !ipsProject.isPersistenceSupportEnabled()) {
            persistenceTypeInfo = null;
            return;
        }
        newPart(PersistentTypeInfo.class);
    }

    @Override
    protected void checkDerivedUnionIsImplemented(IAssociation association,
            List<IAssociation> candidateSubsets,
            MessageList msgList) {
        super.checkDerivedUnionIsImplemented(association, candidateSubsets, msgList);
        if (!association.isDerivedUnion()) {
            /*
             * special check for policy component type associations with type detail to master, if
             * this association is a inverse of a derived union then we need to check either the
             * class is abstract or an inverse implementation of the derived union exists
             */
            IPolicyCmptTypeAssociation policyCmptTypeAssociation = (IPolicyCmptTypeAssociation)association;
            try {
                if (!policyCmptTypeAssociation.isInverseOfDerivedUnion()) {
                    return;
                }

                /*
                 * now check if there is another detail to master which is the inverse of a subset
                 * derived union
                 */
                if (!isInverseSubsetted(policyCmptTypeAssociation, candidateSubsets)) {
                    String text = NLS.bind(Messages.PolicyCmptType_msgInverseDerivedUnionNotSepcified,
                            association.getName(), association.getType().getQualifiedName());
                    msgList.add(new Message(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION, text, Message.ERROR,
                            this, IType.PROPERTY_ABSTRACT));
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    private boolean isInverseSubsetted(IPolicyCmptTypeAssociation inverseOfDerivedUnion,
            List<IAssociation> candidateSubsets) throws CoreException {
        IPolicyCmptTypeAssociation derivedUnion = inverseOfDerivedUnion.findInverseAssociation(getIpsProject());
        if (derivedUnion == null) {
            // must be an error
            return false;
        }
        /*
         * now check if one of the candidate is the inverse of the implementation of the derived
         * union
         */
        for (IAssociation candidate : candidateSubsets) {
            if (!(candidate instanceof IPolicyCmptTypeAssociation)) {
                continue;
            }
            IPolicyCmptTypeAssociation policyCmptTypeAssociation = (IPolicyCmptTypeAssociation)candidate;
            if (!policyCmptTypeAssociation.isCompositionDetailToMaster()) {
                continue;
            }
            IPolicyCmptTypeAssociation inverseAssociationOfCandidate = policyCmptTypeAssociation
                    .findInverseAssociation(getIpsProject());
            if (inverseAssociationOfCandidate == null) {
                continue;
            }
            // test if the inverse is the subset of the derived union
            if (inverseAssociationOfCandidate.getSubsettedDerivedUnion().equals(derivedUnion.getName())) {
                return true;
            }
            // FIPS-85
            if (getIpsProject().getReadOnlyProperties().isSharedDetailToMasterAssociations()) {
                if (inverseAssociationOfCandidate.equals(derivedUnion)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<IValidationRule> findAllValidationRules(IIpsProject ipsProject) {
        AllValidationRulesFinder finder = new AllValidationRulesFinder(ipsProject, true);
        finder.start(this);
        return finder.getValidationRules();
    }

    @Override
    public IValidationRule findValidationRule(String ruleName, IIpsProject ipsProject) {
        ValidationRuleForNameFinder finder = new ValidationRuleForNameFinder(ruleName, ipsProject);
        finder.start(this);
        return finder.getValidationRule();
    }

    @Override
    public String getCaption(Locale locale) throws CoreException {
        return Messages.PolicyCmptType_caption;
    }

    @Override
    public DuplicatePropertyNameValidator createDuplicatePropertyNameValidator(IIpsProject ipsProject) {
        return new PolicyCmptTypeDuplicatePropertyNameValidator(ipsProject);
    }

    private static class IsAggregrateRootVisitor extends TypeHierarchyVisitor<IPolicyCmptType> {

        private boolean root = true;

        public IsAggregrateRootVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            List<IPolicyCmptTypeAssociation> relations = currentType.getPolicyCmptTypeAssociations();
            for (IPolicyCmptTypeAssociation each : relations) {
                if (each.getAssociationType().isCompositionDetailToMaster()) {
                    root = false;
                    // stop the visit, we have the result
                    return false;
                }
            }
            return true;
        }

        public boolean isRoot() {
            return root;
        }

    }

    private static class CheckValidationRuleVisitor extends TypeHierarchyVisitor<IPolicyCmptType> {

        private IValidationRule rule;
        private MessageList msgList;

        public CheckValidationRuleVisitor(IIpsProject ipsProject, IValidationRule rule, MessageList msgList) {
            super(ipsProject);
            this.rule = rule;
            this.msgList = msgList;
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            for (IValidationRule validationRule : currentType.getValidationRules()) {
                if (validationRule == rule) {
                    continue;
                }
                if (validationRule.getName().equals(rule.getName())) {
                    String text = Messages.PolicyCmptType_msgDuplicateRuleName;
                    msgList.add(new Message(IValidationRule.MSGCODE_DUPLICATE_RULE_NAME, text, Message.ERROR, rule,
                            IIpsElement.PROPERTY_NAME));
                }
            }
            for (IMethod method : currentType.getMethods()) {
                if (method.getNumOfParameters() == 0 && method.getName().equals(rule.getName())) {
                    String text = NLS.bind(Messages.PolicyCmptType_msgRuleMethodNameConflict, rule.getName());
                    msgList.add(new Message(IValidationRule.MSGCODE_VALIDATION_RULE_METHOD_NAME_CONFLICT, text,
                            Message.ERROR, rule, IIpsElement.PROPERTY_NAME));
                }
            }
            return true;
        }

    }

    private static class AllValidationRulesFinder extends TypeHierarchyVisitor<IPolicyCmptType> {

        private final List<IValidationRule> rules;
        private final boolean superTypeFirst;

        public AllValidationRulesFinder(IIpsProject ipsProject, boolean superTypeFirst) {
            super(ipsProject);
            this.superTypeFirst = superTypeFirst;
            rules = new ArrayList<IValidationRule>();
        }

        public List<IValidationRule> getValidationRules() {
            return rules;
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            List<IValidationRule> definedRules = currentType.getValidationRules();
            if (superTypeFirst) {
                // Place supertype rules before subtype rules.
                rules.addAll(0, definedRules);
            } else {
                rules.addAll(definedRules);
            }
            return true;
        }

    }

    private static class ValidationRuleForNameFinder extends TypeHierarchyVisitor<IPolicyCmptType> {

        private IValidationRule foundRule = null;
        private final String ruleName;

        public ValidationRuleForNameFinder(String ruleName, IIpsProject ipsProject) {
            super(ipsProject);
            this.ruleName = ruleName;
        }

        public IValidationRule getValidationRule() {
            return foundRule;
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            List<IValidationRule> definedRules = currentType.getValidationRules();
            for (IValidationRule rule : definedRules) {
                if (rule.getName().equals(ruleName)) {
                    foundRule = rule;
                    return false;
                }
            }
            return true;
        }

    }

    private static class PolicyCmptTypeDuplicatePropertyNameValidator extends DuplicatePropertyNameValidator {

        public PolicyCmptTypeDuplicatePropertyNameValidator(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IType currentType) {
            super.visit(currentType);
            IProductCmptType matchingType = getMatchingType(currentType);
            if (matchingType != null) {
                String name = matchingType.getUnqualifiedName();
                add(name, new ObjectProperty(currentType, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE));
            }
            return true;
        }

        @Override
        protected IProductCmptType getMatchingType(IType currentType) {
            return ((IPolicyCmptType)currentType).findProductCmptType(getIpsProject());
        }

        @Override
        protected String getObjectKindNamePlural(ObjectProperty invalidObjProperty) {
            IIpsObjectPartContainer objectPartContainer = ((IIpsObjectPartContainer)invalidObjProperty.getObject());
            if (objectPartContainer instanceof IPolicyCmptType
                    && invalidObjProperty.getProperty().equals(IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE)) {
                return org.faktorips.devtools.core.internal.model.productcmpttype.Messages.ProductCmptType_pluralCaption;
            }
            return super.getObjectKindNamePlural(invalidObjProperty);
        }

    }

}
