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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;
import org.faktorips.devtools.stdbuilder.xpand.model.XDerivedUnionAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.filter.AssociationWithoutDUAndInverseDUFilter;
import org.faktorips.devtools.stdbuilder.xpand.model.filter.DerivedUnionFilter;
import org.faktorips.devtools.stdbuilder.xpand.model.filter.MasterToDetailFilter;
import org.faktorips.devtools.stdbuilder.xpand.model.filter.MasterToDetailWithoutSubsetsFilter;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
import org.faktorips.runtime.internal.AbstractConfigurableModelObject;
import org.faktorips.runtime.internal.AbstractModelObject;

public class XPolicyCmptClass extends XClass {

    private final Set<XPolicyAttribute> attributes;

    private final Set<XProductAttribute> productAttributes;

    private final Set<XPolicyAssociation> associations;

    private final Set<XPolicyAssociation> masterToDetailAssociations;

    private final Set<XPolicyAssociation> masterToDetailAssociationsWithoutSubsets;

    private final Set<XDerivedUnionAssociation> derivedUnions;

    private final Set<XDerivedUnionAssociation> subsettedDerivedUnions;

    private final Set<XDetailToMasterDerivedUnionAssociation> detailToMasterDerivedUnionAssociations;

    private final Set<XValidationRule> validationRules;

    public XPolicyCmptClass(IPolicyCmptType policyCmptType, GeneratorModelContext context, ModelService modelService) {
        super(policyCmptType, context, modelService);
        attributes = initNodesForParts(
                new LinkedHashSet<IPolicyCmptTypeAttribute>(policyCmptType.getPolicyCmptTypeAttributes()),
                XPolicyAttribute.class);
        productAttributes = initNodesForParts(getProductAttributes(policyCmptType), XProductAttribute.class);
        associations = initNodesForParts(
                getAssociations(policyCmptType, IPolicyCmptTypeAssociation.class,
                        new AssociationWithoutDUAndInverseDUFilter()), XPolicyAssociation.class);
        masterToDetailAssociations = initNodesForParts(
                getAssociations(policyCmptType, IPolicyCmptTypeAssociation.class, new MasterToDetailFilter()),
                XPolicyAssociation.class);
        masterToDetailAssociationsWithoutSubsets = initNodesForParts(
                getAssociations(policyCmptType, IPolicyCmptTypeAssociation.class,
                        new MasterToDetailWithoutSubsetsFilter()), XPolicyAssociation.class);
        derivedUnions = initNodesForParts(
                getAssociations(policyCmptType, IPolicyCmptTypeAssociation.class, new DerivedUnionFilter()),
                XDerivedUnionAssociation.class);
        subsettedDerivedUnions = initNodesForParts(
                findSubsettedDerivedUnions(policyCmptType.getPolicyCmptTypeAssociations(),
                        IPolicyCmptTypeAssociation.class), XDerivedUnionAssociation.class);
        detailToMasterDerivedUnionAssociations = initNodesForParts(
                findDetailToMasterDerivedUnionAssociations(policyCmptType.getPolicyCmptTypeAssociations()),
                XDetailToMasterDerivedUnionAssociation.class);
        validationRules = initNodesForParts(new LinkedHashSet<IValidationRule>(policyCmptType.getValidationRules()),
                XValidationRule.class);
    }

    private Set<IProductCmptTypeAttribute> getProductAttributes(IPolicyCmptType policyCmptType) {
        if (policyCmptType.isConfigurableByProductCmptType()) {
            try {
                IProductCmptType productCmptType = policyCmptType.findProductCmptType(policyCmptType.getIpsProject());
                return new LinkedHashSet<IProductCmptTypeAttribute>(productCmptType.getProductCmptTypeAttributes());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        } else {
            Set<IProductCmptTypeAttribute> productAttributes = new LinkedHashSet<IProductCmptTypeAttribute>();
            return productAttributes;
        }
    }

    /**
     * Finds all inverse associations (detail to master associations) in this class.
     */
    protected Set<IPolicyCmptTypeAssociation> findDetailToMasterAssociations(Collection<IPolicyCmptTypeAssociation> associations) {
        Set<IPolicyCmptTypeAssociation> resultingAssociations = new LinkedHashSet<IPolicyCmptTypeAssociation>();
        for (IPolicyCmptTypeAssociation association : associations) {
            if (association.isCompositionDetailToMaster()) {
                resultingAssociations.add(association);
            }
        }
        return resultingAssociations;
    }

    /**
     * Inspects all detail to master associations. If a given association is the inverse of a
     * derived-union-subset, the original detail to master derived union is determined and added to
     * the result.
     */
    protected Set<IPolicyCmptTypeAssociation> findDetailToMasterDerivedUnionAssociations(Collection<IPolicyCmptTypeAssociation> associations) {
        Set<IPolicyCmptTypeAssociation> resultingAssociations = new LinkedHashSet<IPolicyCmptTypeAssociation>();
        for (IPolicyCmptTypeAssociation association : associations) {
            try {
                if (association.isCompositionDetailToMaster()) {
                    IPolicyCmptTypeAssociation inverseAssociation;
                    if (association.isSharedAssociation()) {
                        inverseAssociation = association.findSharedAssociationHost(getIpsProject())
                                .findInverseAssociation(getIpsProject());
                    } else {
                        inverseAssociation = association.findInverseAssociation(getIpsProject());
                    }
                    if (inverseAssociation.isSubsetOfADerivedUnion()) {
                        IPolicyCmptTypeAssociation subsettedDerivedUnion = (IPolicyCmptTypeAssociation)inverseAssociation
                                .findSubsettedDerivedUnion(getIpsProject());
                        if (subsettedDerivedUnion.hasInverseAssociation()) {
                            // it is possible that the derived union does not specify a inverse but
                            // subset does
                            IPolicyCmptTypeAssociation inverseSubsettedAssociation = subsettedDerivedUnion
                                    .findInverseAssociation(getIpsProject());
                            resultingAssociations.add(inverseSubsettedAssociation);
                        }
                    }
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return resultingAssociations;
    }

    @Override
    public IPolicyCmptType getIpsObjectPartContainer() {
        return (IPolicyCmptType)super.getIpsObjectPartContainer();
    }

    /**
     * @return Returns the policyCmptType.
     */
    public IPolicyCmptType getPolicyCmptType() {
        return getIpsObjectPartContainer();
    }

    /**
     * Returns <code>true</code> if this policy component type is configurable
     */
    public boolean isConfigured() {
        return getPolicyCmptType().isConfigurableByProductCmptType();
    }

    public boolean isAggregateRoot() {
        try {
            return getPolicyCmptType().isAggregateRoot();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getClassName() {
        return addImport(getSimpleName(BuilderAspect.IMPLEMENTATION));
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
    public Set<XPolicyAttribute> getAttributes() {
        return new CopyOnWriteArraySet<XPolicyAttribute>(attributes);
    }

    public Set<XProductAttribute> getProductAttributes() {
        return new CopyOnWriteArraySet<XProductAttribute>(productAttributes);
    }

    @Override
    public Set<XPolicyAssociation> getAssociations() {
        return new CopyOnWriteArraySet<XPolicyAssociation>(associations);
    }

    @Override
    public Set<XAssociation> getMasterToDetailAssociations() {
        return new CopyOnWriteArraySet<XAssociation>(masterToDetailAssociations);
    }

    public Set<XPolicyAssociation> getMasterToDetailAssociationsWithoutSubsets() {
        return new CopyOnWriteArraySet<XPolicyAssociation>(masterToDetailAssociationsWithoutSubsets);
    }

    public Set<XDerivedUnionAssociation> getDerivedUnions() {
        return new CopyOnWriteArraySet<XDerivedUnionAssociation>(derivedUnions);
    }

    @Override
    public Set<XDerivedUnionAssociation> getSubsettedDerivedUnions() {
        return new CopyOnWriteArraySet<XDerivedUnionAssociation>(subsettedDerivedUnions);
    }

    public Set<XDetailToMasterDerivedUnionAssociation> getDetailToMasterDerivedUnionAssociations() {
        return new CopyOnWriteArraySet<XDetailToMasterDerivedUnionAssociation>(detailToMasterDerivedUnionAssociations);
    }

    public Set<XValidationRule> getValidationRules() {
        return new CopyOnWriteArraySet<XValidationRule>(validationRules);
    }

    private XProductCmptClass getProductCmptClass() {
        IProductCmptType productCmptType = getProductCmptType();
        if (productCmptType != null) {
            XProductCmptClass xProductCmptClass = getModelNode(productCmptType, XProductCmptClass.class);
            return xProductCmptClass;
        }
        return null;
    }

    public String getProductCmptClassName() {
        XProductCmptClass productCmptClass = getProductCmptClass();
        if (productCmptClass != null) {
            return productCmptClass.getSimpleName(BuilderAspect.IMPLEMENTATION);
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Returns the simple name for the product component generation class associated with this
     * policy component class. An import will be added automatically.
     * 
     */
    protected String getProductGenerationClassName() {
        return getProductGenerationClassName(BuilderAspect.IMPLEMENTATION);
    }

    /**
     * Returns the simple name for the product component generation class or interface associated
     * with this policy component class. An import will be added automatically.
     * <p>
     * This method lets the {@link GeneratorModelContext} and its {@link JavaClassNaming} decide
     * whether the name of the published interface or the name of the implementing class - in case
     * no published interface is generated - is returned.
     * 
     * TODO FIPS-1059
     */
    public String getProductGenerationClassOrInterfaceName() {
        return getProductGenerationClassName(BuilderAspect.getValue(isGeneratingPublishedInterfaces()));
    }

    /**
     * Returns the simple name for the product component generation class or interface associated
     * with this policy component class. An import will be added automatically.
     * 
     */
    protected String getProductGenerationClassName(BuilderAspect aspect) {
        IProductCmptType prodType = getProductCmptType();
        XProductCmptGenerationClass xProductCmptGenClass = getModelNode(prodType, XProductCmptGenerationClass.class);
        String simpleName = xProductCmptGenClass.getSimpleName(aspect);
        return simpleName;
    }

    public String getProductGenerationArgumentName() {
        return getJavaNamingConvention().getMemberVarName(getProductGenerationClassName());
    }

    /**
     * Returns the simple name for the product component class or interface associated with this
     * policy component class. An import will be added automatically.
     * 
     */
    public String getProductComponentClassName(BuilderAspect aspect) {
        XProductCmptClass xProductCmptClass = getProductCmptClass();
        if (xProductCmptClass != null) {
            String simpleName = xProductCmptClass.getSimpleName(aspect);
            return simpleName;
        } else {
            return null;
        }
    }

    /**
     * Returns the simple name for the product component class associated with this policy component
     * class. An import will be added automatically.
     * 
     */
    protected String getProductComponentClassName() {
        return getProductComponentClassName(BuilderAspect.IMPLEMENTATION);
    }

    /**
     * Returns the simple name for the product component class or interface associated with this
     * policy component class. An import will be added automatically.
     * <p>
     * This method lets the {@link GeneratorModelContext} and its {@link JavaClassNaming} decide
     * whether the name of the published interface or the name of the implementing class - in case
     * no published interface is generated - is returned .
     * 
     * TODO FIPS-1059
     * 
     */
    public String getProductComponentClassOrInterfaceName() {
        return getProductComponentClassName(BuilderAspect.getValue(isGeneratingPublishedInterfaces()));
    }

    /**
     * Finds the product component type associated with this policy component type and returns it.
     * Throws a {@link CoreRuntimeException} in case of a {@link CoreException}.
     */
    protected IProductCmptType getProductCmptType() {
        try {
            IProductCmptType prodType = getPolicyCmptType().findProductCmptType(getIpsProject());
            if (prodType == null) {
                throw new NullPointerException(NLS.bind(
                        "The policy component type {0} is not configured by a product component type.",
                        getPolicyCmptType()));
            }
            return prodType;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getMethodNameGetProductCmptGeneration() {
        return getJavaNamingConvention().getGetterMethodName(getProductGenerationClassName());
    }

    public String getMethodNameGetProductComponent() {
        return getJavaNamingConvention().getGetterMethodName(getProductComponentClassName());
    }

    public String getMethodNameSetProductComponent() {
        return getJavaNamingConvention().getSetterMethodName(getProductComponentClassName());
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
        XProductCmptClass productCmptClass = getProductCmptClass();
        if (productCmptClass != null) {
            return productCmptClass.getMethodNameCreatePolicyCmpt();
        } else {
            return null;
        }
    }

    public String getProductComponentArgumentName() {
        return getJavaNamingConvention().getMemberVarName(getProductComponentClassName());
    }

    public String getLocalVarNameDeltaSupportOtherObject() {
        return getJavaNamingConvention().getMemberVarName("other" + getClassName());
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

    public Set<XPolicyAssociation> getInverseCompositions() {
        Set<XPolicyAssociation> resultingSet = new LinkedHashSet<XPolicyAssociation>();
        for (XPolicyAssociation assoc : getAssociations()) {
            if (assoc.isInverseComposition() && (!assoc.isInverseOfADerivedUnion() || assoc.isSharedAssociation())) {
                resultingSet.add(assoc);
            }
        }
        return resultingSet;
    }

    private Set<XPolicyAssociation> getPureAssociations() {
        Set<XPolicyAssociation> resultingSet = new LinkedHashSet<XPolicyAssociation>();
        for (XPolicyAssociation assoc : getAssociations()) {
            if (assoc.isTypeAssociation()) {
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
        return hasInverseCompositionAssociations();
    }

    public boolean isGenerateMethodCreateUnresolvedReference() {
        return hasAssociations();
    }

    public boolean isGenerateNotifyChangeListeners() {
        return isGenerateChangeSupport() && (!hasSupertype() || hasDetailToMasterAssociations());
    }

    private boolean hasDetailToMasterAssociations() {
        for (XPolicyAssociation asso : getAssociations()) {
            if (asso.isCompositionDetailToMaster()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if this policy cmpt class has at least one association that is the
     * inverse of a composition, but not a derived union association.
     */
    private boolean hasInverseCompositionAssociations() {
        return !getInverseCompositions().isEmpty();
    }

    /**
     * Returns <code>true</code> if this policy cmpt class has at least one association (no
     * composition).
     */
    private boolean hasAssociations() {
        return !getPureAssociations().isEmpty();
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
            if (!getPolicyCmptType().isDependantType()) {
                return false;
            }
            IPolicyCmptType supertype = (IPolicyCmptType)getPolicyCmptType().findSupertype(getIpsProject());
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
