/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.policycmpt;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.XDerivedUnionAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.XType;
import org.faktorips.devtools.stdbuilder.xmodel.policycmptbuilder.XPolicyBuilder;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptGenerationClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XTableUsage;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.policycmpt.ValidatorJavaClassNameProvider;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.IDependantObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.INotificationSupport;
import org.faktorips.runtime.ITimedConfigurableModelObject;
import org.faktorips.runtime.IVisitorSupport;

public class XPolicyCmptClass extends XType {

    public XPolicyCmptClass(IPolicyCmptType policyCmptType, GeneratorModelContext context, ModelService modelService) {
        super(policyCmptType, context, modelService);
    }

    @Override
    public boolean isValidForCodeGeneration() {
        try {
            if (!getType().isValid(getIpsProject())) {
                return false;
            } else {
                if (isConfigured()) {
                    return getProductCmptType().isValid(getIpsProject());
                } else {
                    return true;
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public IPolicyCmptType getIpsObjectPartContainer() {
        return (IPolicyCmptType)super.getIpsObjectPartContainer();
    }

    @Override
    public IPolicyCmptType getType() {
        return (IPolicyCmptType)super.getType();
    }

    @Override
    public XPolicyCmptClass getSupertype() {
        return (XPolicyCmptClass)super.getSupertype();
    }

    /**
     * Returns <code>true</code> if this policy component type is configured by a product component
     * type.
     */
    public boolean isConfigured() {
        return getType().isConfigurableByProductCmptType();
    }

    /**
     * Returns <code>true</code> if this policy component type has super type that is configured by
     * a product component type.
     */
    public boolean hasConfiguredSupertype() {
        if (!hasSupertype()) {
            return false;
        }
        return getSupertype().isConfigured();
    }

    /**
     * Returns <code>true</code> if this policy component type has no super type that is configured
     * by a product component type and is self configured by a product component type.
     */
    public boolean isFirstConfigurableInHierarchy() {
        if (!hasConfiguredSupertype() && isConfigured()) {
            return true;
        }
        return false;

    }

    public boolean isAggregateRoot() {
        try {
            return getType().isAggregateRoot();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns true if any super type (transitive) is an aggregate root
     * 
     */
    public boolean isSupertypeAggregateRoot() {
        if (!hasSupertype()) {
            return false;
        } else {
            return getSupertype().isAggregateRoot() || getSupertype().isSupertypeAggregateRoot();
        }
    }

    public boolean isDependantType() {
        try {
            return getType().isDependantType();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns true if any super type (transitive) is an dependant type
     * 
     */
    public boolean isSupertypeDependantType() {
        if (!hasSupertype()) {
            return false;
        } else {
            return getSupertype().isDependantType() || getSupertype().isSupertypeDependantType();
        }
    }

    @Override
    public LinkedHashSet<String> getImplementedInterfaces() {
        LinkedHashSet<String> list = super.getImplementedInterfaces();
        if (!hasSupertype() && getGeneratorConfig().isGenerateSerializablePolicyCmptSupport()) {
            list.add(addImport(Serializable.class));
        }
        return list;
    }

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        LinkedHashSet<String> extendedInterfaces = new LinkedHashSet<String>();
        if (isFirstConfigurableInHierarchy()) {
            extendedInterfaces.add(getConfigurationClassName());
        } else if (!hasSupertype()) {
            extendedInterfaces.add(addImport(IModelObject.class));
        }
        extendedInterfaces.addAll(super.getExtendedInterfaces());
        return extendedInterfaces;
    }

    @Override
    protected LinkedHashSet<String> getExtendedOrImplementedInterfaces() {
        LinkedHashSet<String> extendedInterfaces = super.getExtendedOrImplementedInterfaces();
        if (!hasSupertype()) {
            if (getGeneratorConfig().isGenerateDeltaSupport()) {
                extendedInterfaces.add(addImport(IDeltaSupport.class));
            }
            if (getGeneratorConfig().isGenerateCopySupport()) {
                extendedInterfaces.add(addImport(ICopySupport.class));
            }
            if (getGeneratorConfig().isGenerateVisitorSupport()) {
                extendedInterfaces.add(addImport(IVisitorSupport.class));
            }
            if (getGeneratorConfig().isGenerateChangeSupport()) {
                extendedInterfaces.add(addImport(INotificationSupport.class));
            }
        }
        if (isDependantType() && (!hasSupertype() || !isSupertypeDependantType())) {
            extendedInterfaces.add(addImport(IDependantObject.class));
        }
        if (isFirstConfigurableInHierarchy()) {
            extendedInterfaces.add(getConfigurationClassName());
        }
        return extendedInterfaces;
    }

    private String getConfigurationClassName() {
        if (getProductCmptType().isChangingOverTime()) {
            return addImport(ITimedConfigurableModelObject.class);
        } else {
            return addImport(IConfigurableModelObject.class);
        }
    }

    @Override
    protected String getBaseSuperclassName() {
        return addImport(getContext().getGeneratorConfig(getType()).getBaseClassPolicyCmptType());
    }

    @Override
    public Set<XPolicyCmptClass> getClassHierarchy() {
        return getClassHierarchy(XPolicyCmptClass.class);
    }

    /**
     * Returns all declared attributes without abstract attributes
     * 
     * @implNote For a set of all declared attributes use {@link #getAttributesIncludingAbstract()}.
     * 
     * @return a set of all concrete attributes
     */
    @Override
    public Set<XPolicyAttribute> getAttributes() {
        return filterAbstractAttributes(getAllDeclaredAttributes());
    }

    protected Set<XPolicyAttribute> filterAbstractAttributes(Set<XPolicyAttribute> xAttributes) {
        return xAttributes.stream().filter(a -> !a.isAbstract())
                .collect(Collectors.toCollection(LinkedHashSet<XPolicyAttribute>::new));
    }

    public Set<XPolicyAttribute> getAttributesIncludingAbstract() {
        return getAllDeclaredAttributes();
    }

    @Override
    public Set<XPolicyAttribute> getAllDeclaredAttributes() {
        if (isCached(XPolicyAttribute.class)) {
            return getCachedObjects(XPolicyAttribute.class);
        } else {
            Set<XPolicyAttribute> nodesForParts = initNodesForParts(getType().getPolicyCmptTypeAttributes(),
                    XPolicyAttribute.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    public boolean isConfiguredBy(String qualifiedName) {
        if (!isConfigured()) {
            return false;
        } else {
            return getType().getProductCmptType().equals(qualifiedName);
        }
    }

    public Set<XProductAttribute> getProductAttributes() {
        if (isCached(XProductAttribute.class)) {
            return getCachedObjects(XProductAttribute.class);
        } else {
            Set<XProductAttribute> nodesForParts;
            if (isConfigured()) {
                List<IProductCmptTypeAttribute> productCmptTypeAttributes = getProductCmptType()
                        .getProductCmptTypeAttributes();
                nodesForParts = initNodesForParts(productCmptTypeAttributes, XProductAttribute.class);
            } else {
                nodesForParts = new LinkedHashSet<XProductAttribute>();
            }
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    @Override
    public Set<XPolicyAssociation> getAssociations() {
        return getAllDeclaredAssociations();
    }

    @Override
    public Set<XPolicyAssociation> getAllDeclaredAssociations() {
        if (isCached(XPolicyAssociation.class)) {
            return getCachedObjects(XPolicyAssociation.class);
        } else {
            List<IPolicyCmptTypeAssociation> associationsNeedToGenerate = getType().getPolicyCmptTypeAssociations();
            Set<XPolicyAssociation> nodesForParts = initNodesForParts(associationsNeedToGenerate,
                    XPolicyAssociation.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    @Override
    public Set<XDerivedUnionAssociation> getSubsettedDerivedUnions() {
        return findSubsettedDerivedUnions(getAssociations());
    }

    public Set<XDetailToMasterDerivedUnionAssociation> getDetailToMasterDerivedUnionAssociations() {
        return findDetailToMasterDerivedUnionAssociations(getAssociations());
    }

    /**
     * Inspects all detail to master associations. If a given association is the inverse of a
     * derived-union-subset, the original detail to master derived union is determined and added to
     * the result.
     */
    protected Set<XDetailToMasterDerivedUnionAssociation> findDetailToMasterDerivedUnionAssociations(
            Collection<? extends XPolicyAssociation> associations) {
        Set<XDetailToMasterDerivedUnionAssociation> resultingAssociations = new LinkedHashSet<XDetailToMasterDerivedUnionAssociation>();
        for (XPolicyAssociation association : associations) {
            if (!association.isDerived()) {
                resultingAssociations.addAll(association.getSubsettedDetailToMasterAssociations());
            }
        }
        return resultingAssociations;
    }

    public Set<XValidationRule> getValidationRules() {
        if (isCached(XValidationRule.class)) {
            return getCachedObjects(XValidationRule.class);
        } else {
            Set<XValidationRule> nodesForParts = initNodesForParts(getType().getValidationRules(),
                    XValidationRule.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    public Set<XTableUsage> getProductTables() {
        if (isConfigured()) {
            return initNodesForParts(getProductCmptType().getTableStructureUsages(), XTableUsage.class);
        } else {
            return new LinkedHashSet<XTableUsage>();
        }
    }

    protected IProductCmptType getProductCmptType() {
        IProductCmptType prodType = getType().findProductCmptType(getIpsProject());
        if (prodType == null) {
            throw new NullPointerException(NLS
                    .bind("The policy component type {0} is not configured by a product component type.", getType()));
        }
        return prodType;
    }

    public XProductCmptClass getProductCmptNode() {
        IProductCmptType productCmptType = getProductCmptType();
        return getModelNode(productCmptType, XProductCmptClass.class);
    }

    public XProductCmptGenerationClass getProductCmptGenerationNode() {
        IProductCmptType productCmptType = getProductCmptType();
        return getModelNode(productCmptType, XProductCmptGenerationClass.class);
    }

    /**
     * Returns the class name of the product component. The class name is normally the published
     * interface. If there is not publish interface generated, this method returns the
     * implementation name.
     * 
     * @return The class name used for product component classes
     */
    public String getProductCmptClassName() {
        return getProductCmptNode().getInterfaceName();
    }

    /**
     * Returns the class name of the product component generation. The class name is normally the
     * published interface. If there is not publish interface generated, this method returns the
     * implementation name.
     * 
     * @return The class name used for product component generation classes
     */
    public String getProductCmptGenerationClassName() {
        return getProductCmptGenerationNode().getInterfaceName();
    }

    /**
     * Returns the getter name for the product component. Use this method only when you call this
     * method. To generate the method you should operate in the context of {@link XProductCmptClass}
     * 
     * @return the name of the getProductComponentGeneration method
     */
    public String getMethodNameGetProductCmpt() {
        return getProductCmptNode().getMethodNameGetProductCmpt();
    }

    /**
     * Returns the getter name for the product component generation. Use this method only when you
     * call this method. To generate the method you should operate in the context of
     * {@link XProductCmptGenerationClass}
     * 
     * @return the name of the getProductComponentGeneration method
     */
    public String getMethodNameGetProductCmptGeneration() {
        return getProductCmptGenerationNode().getMethodNameGetProductComponentGeneration();
    }

    public String getMethodNameSetProductCmptGeneration() {
        return getProductCmptGenerationNode().getMethodNameSetProductComponentGeneration();
    }

    /**
     * The method create... is generated in the product component no in the policy component type.
     * However we have this method to get the name of the create method here because we use the
     * policy generator model to create these create method in the product component.
     * 
     * @return The name of the create policy component method, for example createCoverage for a
     *         policy component called 'Coverage'
     */
    public String getMethodNameCreatePolicyCmpt() {
        return "create" + StringUtils.capitalize(getName());
    }

    public String getLocalVarNameDeltaSupportOtherObject() {
        return getJavaNamingConvention().getMemberVarName("other" + StringUtils.capitalize(getName()));
    }

    public Set<XPolicyAttribute> getAttributesToCopy() {
        Set<XPolicyAttribute> resultingSet = new LinkedHashSet<XPolicyAttribute>();
        for (XPolicyAttribute attribute : getAttributes()) {
            if (attribute.isConsiderInCopySupport()) {
                resultingSet.add(attribute);
            }
        }
        return resultingSet;
    }

    /**
     * Returns whether initialization code should be generated for changing over time attributes or
     * static attributes respectively. This is the case if there are attributes with the matching
     * changing over time property.
     * 
     * @param changingOverTime the changing over time property of attributes. <code>true</code> if
     *            attributes that change over time should be considered. <code>false</code> if
     *            static attributes should be considered.
     * @return <code>true</code> if initialization code is required, <code>false</code> otherwise.
     */
    public boolean isGenerateAttributeInitCode(boolean changingOverTime) {
        return !getAttributesToInit(true, changingOverTime).isEmpty()
                || !getAttributesToInit(false, changingOverTime).isEmpty();
    }

    /**
     * Returns this policy component class' attributes matching the given properties.
     * 
     * @param initWithProductData <code>true</code> to consider attributes that return
     *            <code>true</code> for {@link XPolicyAttribute#isGenerateInitWithProductData()},
     *            <code>false</code> for all other attributes.
     * @param changingOverTime <code>true</code> to consider attributes that change over time,
     *            <code>false</code> for static attributes.
     */
    public Set<XPolicyAttribute> getAttributesToInit(boolean initWithProductData, boolean changingOverTime) {
        Set<XPolicyAttribute> resultingSet = new LinkedHashSet<XPolicyAttribute>();
        for (XPolicyAttribute attribute : getAttributes()) {
            if (changingOverTime == attribute.isChangingOverTime()
                    && matchesInitWithProductData(initWithProductData, attribute)) {
                resultingSet.add(attribute);
            }
        }
        return resultingSet;
    }

    private boolean matchesInitWithProductData(boolean initWithProductData, XPolicyAttribute attribute) {
        if (initWithProductData) {
            return attribute.isGenerateInitWithProductData();
        } else {
            return attribute.isGenerateInitWithoutProductData() && attribute.isOverwrite();
        }
    }

    public Set<XPolicyAttribute> getAttributesForDeltaComputation() {
        Set<XPolicyAttribute> resultingSet = new LinkedHashSet<XPolicyAttribute>();
        for (XPolicyAttribute attribute : getAttributes()) {
            if (attribute.isConsiderInDeltaComputation()) {
                resultingSet.add(attribute);
            }
        }
        return resultingSet;
    }

    public Set<XPolicyAssociation> getAssociationsForDeltaComputation() {
        Set<XPolicyAssociation> resultingSet = new LinkedHashSet<XPolicyAssociation>();
        for (XPolicyAssociation assoc : getAssociations()) {
            if (assoc.isConsiderInDeltaComputation()) {
                resultingSet.add(assoc);
            }
        }
        return resultingSet;
    }

    public Set<XPolicyAssociation> getAssociationsToCopy() {
        Set<XPolicyAssociation> resultingSet = new LinkedHashSet<XPolicyAssociation>();
        for (XPolicyAssociation assoc : getAssociations()) {
            if (assoc.isConsiderInCopySupport()) {
                resultingSet.add(assoc);
            }
        }
        return resultingSet;
    }

    /**
     * Returns <code>true</code> if at least one attribute would generate code in the
     * initPropertiesFromXML-Method. <code>false</code> otherwise.
     */
    public boolean isGenerateInitPropertiesFromXML() {
        for (XPolicyAttribute attr : getAttributes()) {
            if (attr.isGenerateInitPropertiesFromXML()) {
                return true;
            }
        }
        return false;
    }

    public boolean isGenerateGetParentModelObject() {
        return hasCompositionDetailToMaster();
    }

    public boolean isSupertypeGenerateGetParentModelObject() {
        if (!hasSupertype()) {
            return false;
        } else {
            return getSupertype().isGenerateGetParentModelObject()
                    || getSupertype().isSupertypeGenerateGetParentModelObject();
        }
    }

    public boolean isGenerateMethodCreateUnresolvedReference() {
        return hasNotConstrainedAssociationsWithTypeAssociation();
    }

    public boolean isGenerateNotifyChangeListeners() {
        return getGeneratorConfig().isGenerateChangeSupport() && (!hasSupertype() || hasCompositionDetailToMaster());
    }

    /**
     * Returns <code>true</code> if this policy cmpt class has at least one association that is the
     * inverse of a composition, but not a derived union association.
     */
    private boolean hasCompositionDetailToMaster() {
        for (XPolicyAssociation assoc : getAssociations()) {
            if (assoc.isCompositionDetailToMaster() && !assoc.isDerived() && !assoc.isConstrain()
                    && !assoc.isSharedAssociationImplementedInSuperclass()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>false</code> if this is no dependent type. If this is a dependent type this
     * method returns <code>true</code> if
     * <ul>
     * <li>no super type is defined,</li>
     * <li>a super type is defined and it is NOT a dependent type</li>
     * </ul>
     * <code>false</code> otherwise.
     */
    public boolean isFirstDependantTypeInHierarchy() {
        try {
            if (!getType().isDependantType()) {
                return false;
            }
            IPolicyCmptType supertype = (IPolicyCmptType)getType().findSupertype(getIpsProject());
            if (supertype == null) {
                return true;
            }
            return !supertype.isDependantType();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns <code>true</code> if this class contains at least on association with type
     * {@link AssociationType#ASSOCIATION}, <code>false</code> else.
     */
    public boolean isRequiresLocalVariableInCopyAssocsInternal() {
        return hasNotConstrainedAssociationsWithTypeAssociation();
    }

    /**
     * Returns <code>true</code> if this class contains at least on association with type
     * {@link AssociationType#ASSOCIATION}, <code>false</code> else.
     */
    private boolean hasNotConstrainedAssociationsWithTypeAssociation() {
        for (XPolicyAssociation assoc : getAssociations()) {
            if (assoc.isTypeAssociation() && !assoc.isConstrain()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the changing over time flag of this product component type is enabled.
     */
    public boolean isGenerateGenerationAccessMethods() {
        return getProductCmptType().isChangingOverTime();
    }

    public XPolicyBuilder getPolicyBuilderModelNode() {
        return getModelNode(getIpsObjectPartContainer(), XPolicyBuilder.class);
    }

    public String getValidatorClassName() {
        ValidatorJavaClassNameProvider provider = new ValidatorJavaClassNameProvider(false);
        return provider.getImplClassName(getIpsObjectPartContainer().getIpsSrcFile());
    }
}
