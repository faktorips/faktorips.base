/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.productcmpt;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.XAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.XDerivedUnionAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.XType;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.runtime.IConfigurableModelObject;

public abstract class XProductClass extends XType {

    public XProductClass(IProductCmptType ipsObjectPartContainer, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(ipsObjectPartContainer, modelContext, modelService);
    }

    @Override
    public boolean isValidForCodeGeneration() {
        try {
            if (!getType().isValid(getIpsProject())) {
                return false;
            } else {
                if (isConfigurationForPolicyCmptType()) {
                    return getPolicyCmptClass().getType().isValid(getPolicyCmptClass().getIpsProject());
                } else {
                    return true;
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public IProductCmptType getIpsObjectPartContainer() {
        return (IProductCmptType)super.getIpsObjectPartContainer();
    }

    @Override
    public IProductCmptType getType() {
        return getIpsObjectPartContainer();
    }

    @Override
    public XProductClass getSupertype() {
        return (XProductClass)super.getSupertype();
    }

    /**
     * Returns true if this class represents a container that handles properties which changes over
     * time, otherwise false.
     * <p>
     * In other words. True for product generations, false for product component class.
     * 
     */
    public abstract boolean isChangeOverTimeClass();

    /**
     * Returns all declared attributes that are applicable for content generation
     * 
     * @implNote For a set of all declared attributes use
     *           {@link #getAttributesIncludingNoContentGeneration()}.
     * 
     * @return a set of all attributes relevant for content generation
     */
    @Override
    public Set<XProductAttribute> getAttributes() {
        return filterAttributes(getAttributesIncludingNoContentGeneration());
    }

    protected Set<XProductAttribute> filterAttributes(Set<XProductAttribute> xAttributes) {
        return xAttributes.stream().filter(XProductAttribute::isGenerateContentCode)
                .collect(Collectors.toCollection(LinkedHashSet<XProductAttribute>::new));
    }

    public Set<XProductAttribute> getAttributesIncludingNoContentGeneration() {
        if (isCached(XProductAttribute.class)) {
            return getCachedObjects(XProductAttribute.class);
        } else {
            Set<XProductAttribute> nodesForParts = initNodesForParts(getAttributesInternal(isChangeOverTimeClass()),
                    XProductAttribute.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    /**
     * Returns the list of attributes. With the parameter you could specify whether you want the
     * attributes that change over time or attributes not changing over time.
     * 
     * @param changableAttributes True to get attributes that change over time, false to get all
     *            other attributes
     * @return the list of attributes defined in this type
     */
    protected Set<IProductCmptTypeAttribute> getAttributesInternal(boolean changableAttributes) {
        Set<IProductCmptTypeAttribute> resultingAttributes = new LinkedHashSet<IProductCmptTypeAttribute>();
        List<IProductCmptTypeAttribute> allAttributes = getType().getProductCmptTypeAttributes();
        for (IProductCmptTypeAttribute attr : allAttributes) {
            if (changableAttributes == attr.isChangingOverTime()) {
                resultingAttributes.add(attr);
            }
        }
        return resultingAttributes;
    }

    public abstract Set<XPolicyAttribute> getConfiguredAttributes();

    public Set<XPolicyAttribute> getConfiguredAttributes(Predicate<XPolicyAttribute> filter) {
        Set<XPolicyAttribute> attributes;
        if (isCached(XPolicyAttribute.class)) {
            attributes = getCachedObjects(XPolicyAttribute.class);
        } else {
            Set<XPolicyAttribute> nodesForParts = getConfiguredAttributesInternal();
            putToCache(nodesForParts);
            attributes = nodesForParts;
        }
        return attributes.stream().filter(filter)
                .collect(Collectors.toCollection(LinkedHashSet<XPolicyAttribute>::new));
    }

    /**
     * Returns the list of configured policy attributes.
     * 
     * @return the list of policy attributes configured by this product class.
     */
    Set<XPolicyAttribute> getConfiguredAttributesInternal() {
        Set<XPolicyAttribute> resultingAttributes = new LinkedHashSet<XPolicyAttribute>();
        if (isConfigurationForPolicyCmptType()) {
            XPolicyCmptClass policyCmptClass = getPolicyCmptClass();
            if (!policyCmptClass.isConfiguredBy(getType().getQualifiedName())) {
                return resultingAttributes;
            }
            return policyCmptClass.getAttributes().stream()
                    .filter(a -> a.isProductRelevant() && a.isGenerateGetAllowedValuesForAndGetDefaultValue())
                    .collect(Collectors.toCollection(LinkedHashSet<XPolicyAttribute>::new));

        }
        return resultingAttributes;
    }

    @Override
    public Set<XProductAttribute> getAllDeclaredAttributes() {
        return initNodesForParts(getType().getProductCmptTypeAttributes(), XProductAttribute.class);
    }

    @Override
    public Set<XProductAssociation> getAssociations() {
        if (isCached(XProductAssociation.class)) {
            return getCachedObjects(XProductAssociation.class);
        } else {
            Set<XProductAssociation> nodesForParts = initNodesForParts(getAssociationsInternal(isChangeOverTimeClass()),
                    XProductAssociation.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    @Override
    public Set<XProductAssociation> getAllDeclaredAssociations() {
        return initNodesForParts(getType().getProductCmptTypeAssociations(), XProductAssociation.class);
    }

    /**
     * Getting the list of associations defined in this type. With the parameter
     * changableAssociations you could specify whether you want the associations that are changeable
     * over time or not changeable (sometimes called static) associations.
     * <p>
     * 
     * @param changableAssociations true if you want only associations changeable over time, false
     *            to get only not changeable over time associations
     * @return The list of associations without derived unions
     */
    protected Set<IProductCmptTypeAssociation> getAssociationsInternal(boolean changableAssociations) {
        Set<IProductCmptTypeAssociation> resultingAssociations = new LinkedHashSet<IProductCmptTypeAssociation>();
        List<IProductCmptTypeAssociation> allAssociations = getType().getProductCmptTypeAssociations();
        for (IProductCmptTypeAssociation assoc : allAssociations) {
            if (changableAssociations == assoc.isChangingOverTime()) {
                resultingAssociations.add(assoc);
            }
        }
        return resultingAssociations;
    }

    @Override
    public Set<XDerivedUnionAssociation> getSubsettedDerivedUnions() {
        return findSubsettedDerivedUnions(getAssociations());
    }

    public Set<XTableUsage> getTables() {
        if (isCached(XTableUsage.class)) {
            return getCachedObjects(XTableUsage.class);
        } else {
            Set<XTableUsage> nodesForParts = initNodesForParts(getTablesInternal(isChangeOverTimeClass()),
                    XTableUsage.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    public Set<XTableUsage> getAllDeclaredTables() {
        return initNodesForParts(getType().getTableStructureUsages(), XTableUsage.class);
    }

    /**
     * Getting the list of {@link ITableStructureUsage} defined in this type. With the parameter
     * changableAssociations you could specify whether you want the {@link ITableStructureUsage}
     * that are changeable over time or not changeable (sometimes called static).
     * <p>
     * 
     * @param changableTableStructureUsage true if you want only {@link ITableStructureUsage}
     *            changeable over time, false to get only not changeable over time
     *            {@link ITableStructureUsage}
     * @return The list of associations without derived unions
     */

    public Set<ITableStructureUsage> getTablesInternal(boolean changableTableStructureUsage) {
        Set<ITableStructureUsage> resultingTableStructureUsages = new LinkedHashSet<ITableStructureUsage>();
        List<ITableStructureUsage> allTableUsages = getType().getTableStructureUsages();
        for (ITableStructureUsage tableUsage : allTableUsages) {
            if (changableTableStructureUsage == tableUsage.isChangingOverTime()) {
                resultingTableStructureUsages.add(tableUsage);
            }
        }
        return resultingTableStructureUsages;
    }

    public boolean isContainsTables() {
        return !getTables().isEmpty();
    }

    /**
     * Returns true if this type is marked as configured and there is a policy component type that
     * could be configured.
     */
    public boolean isConfigurationForPolicyCmptType() {
        return getType().isConfigurationForPolicyCmptType();
    }

    public String getPolicyInterfaceName() {
        return getPolicyClassName(BuilderAspect.INTERFACE);
    }

    public String getPolicyImplClassName() {
        return getPolicyClassName(BuilderAspect.IMPLEMENTATION);
    }

    protected String getPolicyClassName(BuilderAspect aspect) {
        if (isConfigurationForPolicyCmptType()) {
            XPolicyCmptClass xPolicyCmptClass = getPolicyCmptClass();
            return xPolicyCmptClass.getSimpleName(aspect);
        } else {
            return addImport(IConfigurableModelObject.class);
        }
    }

    public XPolicyCmptClass getPolicyCmptClass() {
        IPolicyCmptType policyCmptType = getType().findPolicyCmptType(getIpsProject());
        if (policyCmptType == null) {
            throw new NullPointerException("No policy found for " + getName());
        }
        XPolicyCmptClass xPolicyCmptClass = getModelNode(policyCmptType, XPolicyCmptClass.class);
        return xPolicyCmptClass;
    }

    @Override
    public abstract Set<? extends XProductClass> getClassHierarchy();

    /**
     * Returns true if there is at least one association that is not a derived union or the inverse
     * of a derived union.
     * 
     */
    public boolean isContainsNotDerivedOrConstrainingAssociations() {
        for (XAssociation association : getAssociations()) {
            if (!association.isDerived() && !association.isConstrain()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether we need to generate the cretePolicyCmpt method for the specified policy
     * component class or not
     * 
     * @param policyCmptClass The policy component class for which we want to generate a create
     *            method
     * 
     * @return true if we need to generate the create method
     */
    public boolean isGenerateMethodCreatePolicyCmpt(XPolicyCmptClass policyCmptClass) {
        return isConfigurationForPolicyCmptType() && !getPolicyCmptClass().isAbstract()
                && !policyCmptClass.isAbstract();
    }

    /**
     * Check whether to generate the generic <code>createPolicyComponent</code> method.
     * <p>
     * If this product component class does not configure any policy component and has no super type
     * we generate the method with <code>return null;</code> If it does configure a policy component
     * than this policy component needs to be not abstract and must configure this product
     * component.
     * 
     * @return True if we need to generate the generic <code>createPolicyComponent</code> method
     */
    public boolean isGenerateMethodGenericCreatePolicyComponent() {
        if (!isConfigurationForPolicyCmptType()) {
            return !hasSupertype();
        } else {
            XPolicyCmptClass policyCmptClass = getPolicyCmptClass();
            return !policyCmptClass.isAbstract() && policyCmptClass.isConfiguredBy(getType().getQualifiedName());
        }
    }

    /**
     * Returns the class hierarchy of the corresponding (configured) policy component type. The
     * resulting set contains only policy component types that are configured by a product component
     * type.
     * 
     * As of version 3.13 Faktor-IPS supports configurable policy component types whose super
     * classes are not configurable. These super classes are filtered out.
     * 
     * @return The policy component class hierarchy
     */
    public Set<XPolicyCmptClass> getPolicyTypeClassHierarchy() {
        if (isConfigurationForPolicyCmptType()) {
            XPolicyCmptClass policyCmptClass = getPolicyCmptClass();
            Set<XPolicyCmptClass> result = policyCmptClass.getClassHierarchy();
            for (Iterator<XPolicyCmptClass> iterator = result.iterator(); iterator.hasNext();) {
                XPolicyCmptClass xPolicyCmptClass = iterator.next();
                if (!xPolicyCmptClass.isConfigured()) {
                    iterator.remove();
                }
            }
            return result;
        } else {
            return new LinkedHashSet<XPolicyCmptClass>();
        }
    }

    /**
     * Returns the variable or parameter name for the effetiveDate.
     * 
     */
    public String getVarNameEffectiveDate() {
        IChangesOverTimeNamingConvention convention = getChangesOverTimeNamingConvention();
        Locale locale = getLanguageUsedInGeneratedSourceCode();
        String conceptName = convention.getEffectiveDateConceptName(locale);
        return StringUtils.uncapitalize(conceptName);
    }

    public String getGenerationConceptNameSingular() {
        return getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular(getLanguageUsedInGeneratedSourceCode(), true);
    }

    public String getGenerationConceptNamePlural() {
        return getChangesOverTimeNamingConvention()
                .getGenerationConceptNamePlural(getLanguageUsedInGeneratedSourceCode(), true);
    }

    private IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention() {
        return getGeneratorConfig().getChangesOverTimeNamingConvention();
    }

    /**
     * Returns <code>true</code> if at least one attribute does not overwrite an attribute from the
     * base class. <code>false</code> otherwise.
     */
    public boolean isNonOverwrittenAttributePresent() {
        for (XProductAttribute attr : getAttributes()) {
            if (!attr.isOverwrite()) {
                return true;
            }
        }
        return false;
    }

    public String getBuilderImplClassName() {
        return addImport(getQualifiedName(BuilderAspect.IMPLEMENTATION) + "Builder");
    }

    public String getBuilderClassName() {
        return getIpsObjectPartContainer().getName() + "Builder";
    }

}