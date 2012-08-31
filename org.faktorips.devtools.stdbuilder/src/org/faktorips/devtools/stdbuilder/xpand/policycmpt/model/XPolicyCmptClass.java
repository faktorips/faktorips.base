/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XDerivedUnionAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.XType;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XTableUsage;
import org.faktorips.runtime.internal.AbstractConfigurableModelObject;
import org.faktorips.runtime.internal.AbstractModelObject;

public class XPolicyCmptClass extends XType {

    private volatile Set<XPolicyAttribute> attributes;

    private volatile Set<XProductAttribute> productAttributes;

    private volatile Set<XPolicyAssociation> associations;

    private volatile Set<XDerivedUnionAssociation> subsettedDerivedUnions;

    private volatile Set<XDetailToMasterDerivedUnionAssociation> detailToMasterDerivedUnionAssociations;

    private volatile Set<XValidationRule> validationRules;

    private volatile Set<XTableUsage> productTables;

    public XPolicyCmptClass(IPolicyCmptType policyCmptType, GeneratorModelContext context, ModelService modelService) {
        super(policyCmptType, context, modelService);
    }

    @Override
    public IPolicyCmptType getIpsObjectPartContainer() {
        return (IPolicyCmptType)super.getIpsObjectPartContainer();
    }

    @Override
    public IPolicyCmptType getType() {
        return (IPolicyCmptType)super.getType();
    }

    /**
     * Returns <code>true</code> if this policy component type is configurable
     */
    public boolean isConfigured() {
        return getType().isConfigurableByProductCmptType();
    }

    public boolean isAggregateRoot() {
        try {
            return getType().isAggregateRoot();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public List<String> getImplementedInterfaces() {
        List<String> list = super.getImplementedInterfaces();
        if (getContext().isGenerateChangeSupport() && !hasSupertype()) {
            // list.add(addImport(INotificationSupport.class));
        }
        return list;
    }

    @Override
    protected String getBaseSuperclassName() {
        if (isConfigured()) {
            return addImport(AbstractConfigurableModelObject.class);
        } else {
            return addImport(AbstractModelObject.class);
        }
    }

    @Override
    public Set<XPolicyCmptClass> getClassHierarchy() {
        return getClassHierarchy(XPolicyCmptClass.class);
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        attributes = null;
        productAttributes = null;
        associations = null;
        subsettedDerivedUnions = null;
        detailToMasterDerivedUnionAssociations = null;
        validationRules = null;
    }

    @Override
    public Set<XPolicyAttribute> getAttributes() {
        checkForUpdate();
        if (attributes == null) {
            synchronized (this) {
                if (attributes == null) {
                    attributes = initNodesForParts(getType().getPolicyCmptTypeAttributes(), XPolicyAttribute.class);
                }
            }
        }
        return new CopyOnWriteArraySet<XPolicyAttribute>(attributes);
    }

    public boolean isConfiguredBy(String qualifiedName) {
        if (!isConfigured()) {
            return false;
        } else {
            return getType().getProductCmptType().equals(qualifiedName);
        }
    }

    public Set<XProductAttribute> getProductAttributes() {
        checkForUpdate();
        if (productAttributes == null) {
            synchronized (this) {
                if (productAttributes == null) {
                    if (isConfigured()) {
                        productAttributes = initNodesForParts(getProductCmptType().getProductCmptTypeAttributes(),
                                XProductAttribute.class);
                    } else {
                        productAttributes = new HashSet<XProductAttribute>();
                    }
                }
            }
        }
        return new CopyOnWriteArraySet<XProductAttribute>(productAttributes);
    }

    @Override
    public Set<XPolicyAssociation> getAssociations() {
        checkForUpdate();
        if (associations == null) {
            synchronized (this) {
                if (associations == null) {
                    List<IPolicyCmptTypeAssociation> associationsNeedToGenerate = getType()
                            .getPolicyCmptTypeAssociations();
                    associations = initNodesForParts(associationsNeedToGenerate, XPolicyAssociation.class);
                }
            }
        }
        return new CopyOnWriteArraySet<XPolicyAssociation>(associations);
    }

    @Override
    public Set<XDerivedUnionAssociation> getSubsettedDerivedUnions() {
        checkForUpdate();
        if (subsettedDerivedUnions == null) {
            synchronized (this) {
                if (subsettedDerivedUnions == null) {
                    subsettedDerivedUnions = findSubsettedDerivedUnions(getAssociations());
                }
            }
        }
        return new CopyOnWriteArraySet<XDerivedUnionAssociation>(subsettedDerivedUnions);
    }

    public Set<XDetailToMasterDerivedUnionAssociation> getDetailToMasterDerivedUnionAssociations() {
        checkForUpdate();
        if (detailToMasterDerivedUnionAssociations == null) {
            synchronized (this) {
                if (detailToMasterDerivedUnionAssociations == null) {
                    detailToMasterDerivedUnionAssociations = findDetailToMasterDerivedUnionAssociations(getAssociations());
                }
            }
        }
        return new CopyOnWriteArraySet<XDetailToMasterDerivedUnionAssociation>(detailToMasterDerivedUnionAssociations);
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
        checkForUpdate();
        if (validationRules == null) {
            synchronized (this) {
                if (validationRules == null) {
                    validationRules = initNodesForParts(getType().getValidationRules(), XValidationRule.class);
                }
            }
        }
        return new CopyOnWriteArraySet<XValidationRule>(validationRules);
    }

    public Set<XTableUsage> getProductTables() {
        checkForUpdate();
        if (productTables == null) {
            synchronized (this) {
                if (productTables == null) {
                    if (isConfigured()) {
                        productTables = initNodesForParts(getProductCmptType().getTableStructureUsages(),
                                XTableUsage.class);
                    } else {
                        productTables = new HashSet<XTableUsage>();
                    }
                }
            }
        }
        return new CopyOnWriteArraySet<XTableUsage>(productTables);
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

    //
    // public String getMethodNameGetProductCmptGeneration() {
    // return getJavaNamingConvention().getGetterMethodName(getProductGenerationClassName());
    // }
    //
    // public String getMethodNameGetProductComponent() {
    // return getJavaNamingConvention().getGetterMethodName(getProductComponentClassName());
    // }
    //
    // public String getMethodNameSetProductComponent() {
    // return getJavaNamingConvention().getSetterMethodName(getProductComponentClassName());
    // }
    // public String getProductComponentArgumentName() {
    // return getJavaNamingConvention().getMemberVarName(getProductComponentClassName());
    // }

    // private XProductCmptClass getProductCmptClass() {
    // IProductCmptType productCmptType = getProductCmptType();
    // if (productCmptType != null) {
    // XProductCmptClass xProductCmptClass = getModelNode(productCmptType, XProductCmptClass.class);
    // return xProductCmptClass;
    // }
    // return null;
    // }
    //
    // public String getProductCmptClassName() {
    // XProductCmptClass productCmptClass = getProductCmptClass();
    // if (productCmptClass != null) {
    // return productCmptClass.getSimpleName(BuilderAspect.IMPLEMENTATION);
    // } else {
    // return StringUtils.EMPTY;
    // }
    // }
    //
    // /**
    // * Returns the simple name for the product component generation class associated with this
    // * policy component class. An import will be added automatically.
    // *
    // */
    // public String getProductGenerationClassName() {
    // return getProductGenerationClassName(BuilderAspect.IMPLEMENTATION);
    // }
    //
    // /**
    // * Returns the simple name for the product component generation class or interface associated
    // * with this policy component class. An import will be added automatically.
    // * <p>
    // * This method lets the {@link GeneratorModelContext} and its {@link JavaClassNaming} decide
    // * whether the name of the published interface or the name of the implementing class - in case
    // * no published interface is generated - is returned.
    // *
    // * TODO FIPS-1059
    // */
    // public String getProductGenerationClassOrInterfaceName() {
    // return
    // getProductGenerationClassName(BuilderAspect.getValue(isGeneratingPublishedInterfaces()));
    // }
    //
    // /**
    // * Returns the simple name for the product component generation class or interface associated
    // * with this policy component class. An import will be added automatically.
    // *
    // */
    // protected String getProductGenerationClassName(BuilderAspect aspect) {
    // IProductCmptType prodType = getProductCmptType();
    // XProductCmptGenerationClass xProductCmptGenClass = getModelNode(prodType,
    // XProductCmptGenerationClass.class);
    // String simpleName = xProductCmptGenClass.getSimpleName(aspect);
    // return simpleName;
    // }
    //
    // public String getProductGenerationArgumentName() {
    // return getJavaNamingConvention().getMemberVarName(getProductGenerationClassName());
    // }
    //
    // /**
    // * Returns the simple name for the product component class or interface associated with this
    // * policy component class. An import will be added automatically.
    // *
    // */
    // public String getProductComponentClassName(BuilderAspect aspect) {
    // XProductCmptClass xProductCmptClass = getProductCmptClass();
    // if (xProductCmptClass != null) {
    // String simpleName = xProductCmptClass.getSimpleName(aspect);
    // return simpleName;
    // } else {
    // return null;
    // }
    // }
    //
    // /**
    // * Returns the simple name for the product component class associated with this policy
    // component
    // * class. An import will be added automatically.
    // *
    // */
    // protected String getProductComponentClassName() {
    // return getProductComponentClassName(BuilderAspect.IMPLEMENTATION);
    // }
    //
    // /**
    // * Returns the simple name for the product component class or interface associated with this
    // * policy component class. An import will be added automatically.
    // * <p>
    // * This method lets the {@link GeneratorModelContext} and its {@link JavaClassNaming} decide
    // * whether the name of the published interface or the name of the implementing class - in case
    // * no published interface is generated - is returned .
    // *
    // * TODO FIPS-1059
    // *
    // */
    // public String getProductComponentClassOrInterfaceName() {
    // return
    // getProductComponentClassName(BuilderAspect.getValue(isGeneratingPublishedInterfaces()));
    // }
    //
    // /**
    // * Finds the product component type associated with this policy component type and returns it.
    // * Throws a {@link CoreRuntimeException} in case of a {@link CoreException}.
    // */
    /**
     * The method create... is generated in the product component no in the policy component type.
     * However we have this method to get the name of the create method here because we use the
     * policy generator model to create these create method in the product component.
     * 
     * @return The name of the create policy component method, for example createCoverage for a
     *         policy component called 'Coverage'
     */
    public String getMethodNameCreatePolicyCmpt() {
        return "create" + getImplClassName();
    }

    public String getLocalVarNameDeltaSupportOtherObject() {
        return getJavaNamingConvention().getMemberVarName("other" + getImplClassName());
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

    public boolean isGenerateMethodCreateUnresolvedReference() {
        return hasAssociationsWithTypeAssociation();
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
            if (assoc.isCompositionDetailToMaster() && !assoc.isDerived()
                    && !assoc.isSharedAssociationImplementedInSuperclass()) {
                return true;
            }
        }
        return false;
    }

    public XPolicyCmptClass getSuperclass() {
        try {
            IType superType = getType().findSupertype(getIpsProject());
            if (superType == null) {
                throw new NullPointerException("Found no supertype for " + getName());
            }
            return getModelNode(superType, XPolicyCmptClass.class);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
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
        return hasAssociationsWithTypeAssociation();
    }

    /**
     * Returns <code>true</code> if this class contains at least on association with type
     * {@link AssociationType#ASSOCIATION}, <code>false</code> else.
     */
    private boolean hasAssociationsWithTypeAssociation() {
        for (XPolicyAssociation assoc : getAssociations()) {
            if (assoc.isTypeAssociation()) {
                return true;
            }
        }
        return false;
    }
}
