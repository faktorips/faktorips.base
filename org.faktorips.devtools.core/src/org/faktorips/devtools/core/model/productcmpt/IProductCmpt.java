/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.core.model.IIpsMetaObject;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * A part (or component) of a product.
 */
public interface IProductCmpt extends IIpsMetaObject, ITimedIpsObject, IPropertyValueContainer {

    /**
     * The name of the product component type property
     */
    public final static String PROPERTY_PRODUCT_CMPT_TYPE = "productCmptType"; //$NON-NLS-1$

    public final static String PROPERTY_RUNTIME_ID = "runtimeId"; //$NON-NLS-1$

    public final static String MSGCODE_PREFIX = "PRODUCT_CMPT-"; //$NON-NLS-1$

    /**
     * Validation message code that indicates that the product component type the product component
     * is an instance of is missing.
     */
    public final static String MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE = MSGCODE_PREFIX + "MissingProductCmptType"; //$NON-NLS-1$

    /**
     * Validation message code that indicates that the product component type is abstract. Abstract
     * product component types can't have an instance (=product component)
     */
    public final static String MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE = MSGCODE_PREFIX + "AbstractProductCmptType"; //$NON-NLS-1$

    /**
     * Validation message code that indicates if the type's hierarchy the product component is based
     * on is inconsistent.
     */
    public final static String MSGCODE_INCONSISTENT_TYPE_HIERARCHY = MSGCODE_PREFIX + "InconsistTypeHierarchy"; //$NON-NLS-1$

    /**
     * Returns the product component's generation at the specified index.
     * 
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public IProductCmptGeneration getProductCmptGeneration(int index);

    /**
     * @return Returns the latest generation of this product component or <code>null</code>, if no
     *         generation exists.
     */
    public IProductCmptGeneration getLatestProductCmptGeneration();

    @Override
    public IProductCmptGeneration getGenerationByEffectiveDate(GregorianCalendar date);

    /**
     * Returns the product component's kind or <code>null</code> if the kind can't be found.
     * 
     * @throws CoreException if an error occurs while searching for the kind.
     */
    public IProductCmptKind findProductCmptKind() throws CoreException;

    /**
     * Returns the product component's version id. The version id is extracted from the components
     * name with the product component naming strategy defined in the project.
     * 
     * @throws CoreException if an exception occurs while accessing the project properties to get
     *             the naming strategy or the version id can't be derived from the component's name.
     */
    public String getVersionId() throws CoreException;

    /**
     * Searches the policy component type that is configured by this product component.
     * 
     * @return The policy component type this product component configures or <code>null</code> if
     *         the policy component type can't be found or this product component does not configure
     *         a product component.
     * 
     * @throws CoreException if an exception occurs while searching for the type.
     */
    @Override
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the qualified name of the product component type this product component is based on.
     * 
     * @throws NullPointerException if newType is <code>null</code>.
     */
    public void setProductCmptType(String newType);

    /**
     * Searches the product component type this product component is based on.
     * 
     * @param ipsProject The project which IPS object path is used for the searched. This is not
     *            necessarily the project this component is part of.
     * 
     * @return The product component type this product component is based on or <code>null</code> if
     *         the product component type can't be found.
     * 
     * @throws CoreException if an exception occurs while searching for the type.
     */
    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns <code>true</code> if any of the generations contain at least one formula. Returns
     * <code>false</code> otherwise.
     */
    public boolean containsFormula();

    /**
     * Returns <code>true</code> if any of the generations contain at least one formula with at
     * least one formula test case. Returns <code>false</code> otherwise.
     */
    public boolean containsFormulaTest();

    /**
     * Returns the product component tree that is defined by this component as root and following
     * it's links to other components. This method uses the latest adjustment instead of specific
     * date.
     * 
     * @deprecated use {@link #getStructure(GregorianCalendar, IIpsProject)} instead. Have a look at
     *             {@link ProductCmptTreeStructure#ProductCmptTreeStructure(IProductCmpt, IIpsProject)}
     * 
     * 
     * @param ipsProject The project which IPS object path is used for the searched. This is not
     *            necessarily the project this component is part of.
     * 
     * @throws CycleInProductStructureException If a circle is detected.
     */
    @Deprecated
    public IProductCmptTreeStructure getStructure(IIpsProject ipsProject) throws CycleInProductStructureException;

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
    public IProductCmptTreeStructure getStructure(GregorianCalendar date, IIpsProject ipsProject)
            throws CycleInProductStructureException;

    /**
     * Returns the id this object is identified by at runtime.
     */
    public String getRuntimeId();

    /**
     * Sets the given runtimeId for this product component.
     * 
     * Be aware of the problems that can be caused by setting a new runtime id to an object where
     * already data with references to the old runtime id exists...
     */
    public void setRuntimeId(String runtimeId);

    /**
     * Returns <code>true</code> if the given policy component type is used as target in at least
     * one generation of this product component.
     * 
     * @since FIPS 3.0.0
     */
    public boolean isReferencingProductCmpt(IIpsProject ipsProjectToSearch, IProductCmpt productCmptCandidate);

    /**
     * Returns <code>true</code> if the given policy component type is used as target in at least
     * one generation of this product component. Deprecated since FIPS 3.0.0.
     * 
     * @deprecated use {@link #isReferencingProductCmpt(IIpsProject, IProductCmpt)} instead
     */
    @Deprecated
    boolean isUsedAsTargetProductCmpt(IIpsProject ipsProjectToSearch, IProductCmpt productCmptCandidate);

    /**
     * @return all {@link IAttributeValue}s in this component.
     */
    public List<IAttributeValue> getAttributeValues();

    /**
     * Returns the attribute value for the given attribute name. Returns <code>null</code> if this
     * container has no value for the given attribute. Returns <code>null</code> if attribute is
     * <code>null</code>.
     */
    public IAttributeValue getAttributeValue(String attribute);

    /**
     * Overrides the original {@link IFixDifferencesToModelSupport#computeDeltaToModel(IIpsProject)}
     * method for using covariant return type {@link IPropertyValueContainerToTypeDelta}
     * 
     * {@inheritDoc}
     */
    @Override
    public IPropertyValueContainerToTypeDelta computeDeltaToModel(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns this {@link IProductCmpt}'s generations in a type safe list.
     * 
     * @return this {@link IProductCmpt}'s generations as a type safe list.
     */
    public List<IProductCmptGeneration> getProductCmptGenerations();

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
     * @throws CoreException if an error occurs while searching for the {@link IProductCmptType
     *             product component type} this {@link IProductCmpt product component} is an
     *             instance of, or while searching for the {@link IProductCmptProperty product
     *             component properties} belonging to the {@link IProductCmptType product component
     *             type}
     */
    public List<IPropertyValue> findPropertyValues(IProductCmptCategory category,
            GregorianCalendar effectiveDate,
            IIpsProject ipsProject) throws CoreException;

}
