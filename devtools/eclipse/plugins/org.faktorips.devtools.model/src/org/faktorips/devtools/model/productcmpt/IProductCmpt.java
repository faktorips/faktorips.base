/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import java.util.GregorianCalendar;
import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsMetaObject;
import org.faktorips.devtools.model.ipsobject.IDeprecation;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IProductCmptProperty;

/**
 * A part (or component) of a product.
 */
public interface IProductCmpt extends IIpsMetaObject, ITimedIpsObject, IProductCmptLinkContainer,
        IPropertyValueContainer, IValidationRuleConfigContainer {

    /** The name of the product component type property */
    String PROPERTY_PRODUCT_CMPT_TYPE = "productCmptType"; //$NON-NLS-1$

    String PROPERTY_RUNTIME_ID = "runtimeId"; //$NON-NLS-1$

    String PROPERTY_TEMPLATE = "template"; //$NON-NLS-1$

    String MSGCODE_PREFIX = "PRODUCT_CMPT-"; //$NON-NLS-1$

    /**
     * Validation message code that indicates that the product component type the product component
     * is an instance of is missing.
     */
    String MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE = MSGCODE_PREFIX + "MissingProductCmptType"; //$NON-NLS-1$

    /**
     * Validation message code that indicates that the product component type is abstract. Abstract
     * product component types can't have an instance (=product component)
     */
    String MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE = MSGCODE_PREFIX + "AbstractProductCmptType"; //$NON-NLS-1$

    /**
     * Validation message code that indicates that the product component type is deprecated. A
     * replacement/migration strategy should be documented in its
     * {@link IDeprecation#getDescriptions() deprecation descriptions}.
     */
    String MSGCODE_DEPRECATED_PRODUCT_CMPT_TYPE = MSGCODE_PREFIX + "DeprecatedProductCmptType"; //$NON-NLS-1$

    /**
     * Validation message code that indicates if the type's hierarchy the product component is based
     * on is inconsistent.
     */
    String MSGCODE_INCONSISTENT_TYPE_HIERARCHY = MSGCODE_PREFIX + "InconsistTypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code that indicates if the specified template could not be found
     */
    String MSGCODE_INVALID_TEMPLATE = MSGCODE_PREFIX + "InvalidTemplate"; //$NON-NLS-1$

    /**
     * Validation message code that indicates if the template's type is inconsistent to this
     * product's type
     */
    String MSGCODE_INCONSISTENT_TEMPLATE_TYPE = MSGCODE_PREFIX + "InconsistTemplateType"; //$NON-NLS-1$

    /**
     * Validation message code that indicates if the template's type is inconsistent to this
     * product's type
     */
    String MSGCODE_INCONSISTENT_TEMPLATE_VALID_FROM = MSGCODE_PREFIX + "InconsistTemplateValidFrom"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the template hierarchy for this template contains a
     * cycle.
     */
    String MSGCODE_TEMPLATE_CYCLE = MSGCODE_PREFIX + "TemplateCycle"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the template for the product this generation is for
     * could not be found.
     */
    String MSGCODE_DIFFERENCES_TO_MODEL = MSGCODE_PREFIX + "DifferencesToModel"; //$NON-NLS-1$

    String MSGCODE_DUPLICATE_KINDID_VERSIONID = MSGCODE_PREFIX + "IdsNotUnique"; //$NON-NLS-1$

    /**
     * Returns the product component's generation at the specified index.
     *
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    IProductCmptGeneration getProductCmptGeneration(int index);

    /**
     * @return Returns the latest generation of this product component or <code>null</code>, if no
     *             generation exists.
     */
    IProductCmptGeneration getLatestProductCmptGeneration();

    @Override
    IProductCmptGeneration getGenerationByEffectiveDate(GregorianCalendar date);

    /**
     * Returns the product component's kind or <code>null</code> if the kind can't be found.
     */
    IProductCmptKind getKindId();

    /**
     * Returns the product component's version id. The version id is extracted from the components
     * name with the product component naming strategy defined in the project.
     *
     * @throws IpsException if an exception occurs while accessing the project properties to get the
     *             naming strategy or the version id can't be derived from the component's name.
     */
    String getVersionId() throws IpsException;

    /**
     * Searches the policy component type that is configured by this product component.
     *
     * @return The policy component type this product component configures or <code>null</code> if
     *             the policy component type can't be found or this product component does not
     *             configure a product component.
     */
    @Override
    IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject);

    /**
     * Sets the qualified name of the product component type this product component is based on.
     *
     * @throws NullPointerException if newType is <code>null</code>.
     */
    void setProductCmptType(String newType);

    /**
     * Searches the product component type this product component is based on.
     *
     * @param ipsProject The project which IPS object path is used for the searched. This is not
     *            necessarily the project this component is part of.
     *
     * @return The product component type this product component is based on or <code>null</code> if
     *             the product component type can't be found.
     *
     */
    @Override
    IProductCmptType findProductCmptType(IIpsProject ipsProject);

    /**
     * Returns <code>true</code> if any of the generations contain at least one formula. Returns
     * <code>false</code> otherwise.
     */
    boolean containsGenerationFormula();

    /**
     * Returns the product component structure representing the structure defined by relations. The
     * relations are evaluated for the given data. The structure is rooted at this product.
     *
     * @param date The date the structure has to be valid for. That means that the relations between
     *            the product components represented by this structure are valid for the given date.
     *
     * @param ipsProject The project which IPS object path is used for the searched. This is not
     *            necessarily the project this component is part of.
     *
     * @throws CycleInProductStructureException If a circle is detected.
     */
    IProductCmptTreeStructure getStructure(GregorianCalendar date, IIpsProject ipsProject)
            throws CycleInProductStructureException;

    /**
     * Returns the id this object is identified by at runtime.
     */
    String getRuntimeId();

    /**
     * Sets the given runtimeId for this product component.
     *
     * Be aware of the problems that can be caused by setting a new runtime id to an object where
     * already data with references to the old runtime id exists...
     */
    void setRuntimeId(String runtimeId);

    /**
     * Returns <code>true</code> if the given policy component type is used as target in at least
     * one generation of this product component.
     *
     * @since FIPS 3.0.0
     */
    boolean isReferencingProductCmpt(IIpsProject ipsProjectToSearch, IProductCmpt productCmptCandidate);

    /**
     * @return all {@link IAttributeValue IAttributeValues} in this component.
     */
    List<IAttributeValue> getAttributeValues();

    /**
     * Returns the attribute value for the given attribute name. Returns <code>null</code> if this
     * container has no value for the given attribute. Returns <code>null</code> if attribute is
     * <code>null</code>.
     */
    IAttributeValue getAttributeValue(String attribute);

    /**
     * Overrides the original {@link IFixDifferencesToModelSupport#computeDeltaToModel(IIpsProject)}
     * method for using covariant return type {@link IPropertyValueContainerToTypeDelta}
     *
     * {@inheritDoc}
     */
    @Override
    IPropertyValueContainerToTypeDelta computeDeltaToModel(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns this {@link IProductCmpt}'s generations in a type safe list.
     *
     * @return this {@link IProductCmpt}'s generations as a type safe list.
     */
    List<IProductCmptGeneration> getProductCmptGenerations();

    /**
     * Returns all {@link IPropertyValue property values} of this {@link IProductCmpt product
     * component} that belong to the indicated {@link IProductCmptCategory category}.
     * <p>
     * Furthermore, all {@link IPropertyValue property values} of the {@link IProductCmptGeneration
     * generation} corresponding to the given effective date, belonging to the indicated
     * {@link IProductCmptCategory category} are returned as well.
     *
     * @param category the {@link IProductCmptCategory category} for which to retrieve the
     *            {@link IPropertyValue property values} or {@code null} if the
     *            {@link IProductCmptCategory category} is of no relevance
     * @param effectiveDate the effective date for which to retrieve the {@link IPropertyValue
     *            property values}
     *
     * @throws IpsException if an error occurs while searching for the {@link IProductCmptType
     *             product component type} this {@link IProductCmpt product component} is an
     *             instance of, or while searching for the {@link IProductCmptProperty product
     *             component properties} belonging to the {@link IProductCmptType product component
     *             type}
     */
    List<IPropertyValue> findPropertyValues(IProductCmptCategory category,
            GregorianCalendar effectiveDate,
            IIpsProject ipsProject) throws IpsException;

    @Override
    IProductCmptGeneration getGenerationEffectiveOn(GregorianCalendar date);

    @Override
    IProductCmptGeneration getBestMatchingGenerationEffectiveOn(GregorianCalendar date);

    @Override
    IProductCmptGeneration getFirstGeneration();

    /**
     * Returns a list containing all links defined in this product component including all links of
     * every generation.
     *
     * @return A list containing every link of this product component and its generations
     */
    List<IProductCmptLink> getLinksIncludingGenerations();

    /**
     * @param rolename The role name for the required content usage.
     * @return The table content usage for the table structure usage with the given role name.
     */
    ITableContentUsage getTableContentUsage(String rolename);

    /**
     * Returns all table content usages defined by this product component. Returns an empty array if
     * the product component hasn't got a table content.
     */
    ITableContentUsage[] getTableContentUsages();

    /**
     * Returns the formulas defined in this product component. Returns an empty array if the product
     * component hasn't got a formula.
     */
    IFormula[] getFormulas();

    /**
     * Returns the formula with given name or <code>null</code> if no such formula is found. Returns
     * <code>null</code> if formulaName is <code>null</code>.
     */
    IFormula getFormula(String formulaName);

    /**
     * Returns <code>true</code> if this object represents a product template.
     *
     * @return <code>true</code> if this is a template, <code>false</code> if not
     */
    @Override
    boolean isProductTemplate();

    /**
     * Set the name of the template that should be used by this product component. Set
     * <code>null</code> to disable the template and tell the product component to specify every
     * value by its own.
     *
     * @param template The name of the template that should be used by this product component or
     *            <code>null</code> to not use any template.
     */
    void setTemplate(String template);

    /**
     * Returns the template object that is used by this product component if this product component
     * has specified a template. Returns <code>null</code> if no template is specified or the
     * specified template was not found.
     *
     * @see #getTemplate()
     * @see #setTemplate(String)
     *
     * @param ipsProject The project that should be used to search for the template
     * @return The product component that is specified as the template of this product component
     */
    @Override
    IProductCmpt findTemplate(IIpsProject ipsProject);

    /**
     * Resets the {@link TemplateValueStatus} for all {@link IPropertyValue} in this product
     * component. Resetting means that the {@link TemplateValueStatus} is set to
     * {@link TemplateValueStatus#DEFINED} if it was {@link TemplateValueStatus#INHERITED}. The
     * status {@link TemplateValueStatus#UNDEFINED} remains unchanged.
     */
    void resetTemplateStatus();

}
