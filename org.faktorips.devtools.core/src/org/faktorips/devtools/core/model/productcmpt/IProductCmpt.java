/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpt;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsMetaObject;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

/**
 * A part (or component) of a product.
 */
public interface IProductCmpt extends IIpsMetaObject, ITimedIpsObject {

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
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the qualified name of the product component type this product component is based on.
     */
    public String getProductCmptType();

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
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException;

    /**
     * Sorts properties in all generations according to the order defined in the model. If the
     * product component type isn't found, the component remains unchanged.
     * 
     * @param ipsProject The IPS project which search path is used to search the type.
     * 
     * @throws CoreException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public void sortPropertiesAccordingToModel(IIpsProject ipsProject) throws CoreException;

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
     * @param ipsProject The project which IPS object path is used for the searched. This is not
     *            necessarily the project this component is part of.
     * 
     * @throws CycleInProductStructureException If a circle is detected.
     */
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

}
