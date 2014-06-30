/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XDerivedUnionAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.XType;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XTableUsage;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.IDependantObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.INotificationSupport;
import org.faktorips.runtime.IVisitorSupport;
import org.faktorips.runtime.internal.AbstractModelObject;

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
        if (!hasSupertype() && isGenerateSerializablePolicyCmptsSupport()) {
            list.add(addImport(Serializable.class));
        }
        return list;
    }

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        LinkedHashSet<String> extendedInterfaces = new LinkedHashSet<String>();
        if (isFirstConfigurableInHierarchy()) {
            extendedInterfaces.add(addImport(IConfigurableModelObject.class));
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
            if (isGenerateDeltaSupport()) {
                extendedInterfaces.add(addImport(IDeltaSupport.class));
            }
            if (isGenerateCopySupport()) {
                extendedInterfaces.add(addImport(ICopySupport.class));
            }
            if (isGenerateVisitorSupport()) {
                extendedInterfaces.add(addImport(IVisitorSupport.class));
            }
            if (isGenerateChangeSupport()) {
                extendedInterfaces.add(addImport(INotificationSupport.class));
            }
        }
        if (isDependantType() && (!hasSupertype() || !isSupertypeDependantType())) {
            extendedInterfaces.add(addImport(IDependantObject.class));
        }
        return extendedInterfaces;
    }

    @Override
    protected String getBaseSuperclassName() {
        return addImport(AbstractModelObject.class);
    }

    @Override
    public Set<XPolicyCmptClass> getClassHierarchy() {
        return getClassHierarchy(XPolicyCmptClass.class);
    }

    @Override
    public Set<XPolicyAttribute> getAttributes() {
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
    protected Set<XDetailToMasterDerivedUnionAssociation> findDetailToMasterDerivedUnionAssociations(Collection<XPolicyAssociation> associations) {
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
        try {
            IProductCmptType prodType = getType().findProductCmptType(getIpsProject());
            if (prodType == null) {
                throw new NullPointerException(NLS.bind(
                        "The policy component type {0} is not configured by a product component type.", getType()));
            }
            return prodType;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
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

    public Set<XPolicyAttribute> getAttributesToInitWithProductData() {
        Set<XPolicyAttribute> resultingSet = new LinkedHashSet<XPolicyAttribute>();
        for (XPolicyAttribute attribute : getAttributes()) {
            if (attribute.isGenerateInitWithProductData()) {
                resultingSet.add(attribute);
            }
        }
        return resultingSet;
    }

    public Set<XPolicyAttribute> getAttributesToInitWithoutProductDataAndOverwritten() {
        Set<XPolicyAttribute> resultingSet = new LinkedHashSet<XPolicyAttribute>();
        for (XPolicyAttribute attribute : getAttributes()) {
            if (attribute.isGenerateInitWithoutProductData() && attribute.isOverwrite()) {
                resultingSet.add(attribute);
            }
        }
        return resultingSet;
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
        return isGenerateChangeSupport() && (!hasSupertype() || hasCompositionDetailToMaster());
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
}
