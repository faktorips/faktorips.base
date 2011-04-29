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

package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.type.AssociationType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;

/**
 * A directed relationship between to product component types.
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptTypeAssociation extends IAssociation {

    /**
     * The list of applicable types. For product component types only aggregations and associations
     * are supported.
     */
    public final static AssociationType[] APPLICABLE_ASSOCIATION_TYPES = new AssociationType[] {
            AssociationType.AGGREGATION, AssociationType.ASSOCIATION };

    public static final int CARDINALITY_ONE = 1;
    public static final int CARDINALITY_MANY = Integer.MAX_VALUE;

    // String constants for the relation class' properties according to the Java beans standard.
    public final static String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET_ROLE_SINGULAR = "targetRoleSingular"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET_ROLE_PLURAL = "targetRolePlural"; //$NON-NLS-1$
    public final static String PROPERTY_MIN_CARDINALITY = "minCardinality"; //$NON-NLS-1$
    public final static String PROPERTY_MAX_CARDINALITY = "maxCardinality"; //$NON-NLS-1$
    public final static String PROPERTY_DERIVED_UNION = "derivedUnion"; //$NON-NLS-1$
    public final static String PROPERTY_SUBSETTED_DERIVED_UNION = "subsettedDerivedUnion"; //$NON-NLS-1$

    /**
     * Returns the product component type this relation belongs to. Never returns <code>null</code>.
     */
    public IProductCmptType getProductCmptType();

    /**
     * Returns the target product component type or <code>null</code> if either this relation hasn't
     * got a target or the target does not exists.
     * 
     * @param project The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */
    public IProductCmptType findTargetProductCmptType(IIpsProject project) throws CoreException;

    /**
     * Returns <code>true</code> if this association constrains a policy component type association,
     * otherwise <code>false</code>. If this method returns <code>true</code>,
     * {@link #findMatchingPolicyCmptTypeAssociation(IIpsProject)} returns the constrained
     * association, otherwise the finder method returns <code>null</code>.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching for the matching association.
     */
    public boolean constrainsPolicyCmptTypeAssociation(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the corresponding policy component type association or <code>null</code> if no such
     * association is found.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching for the matching association.
     */
    public IPolicyCmptTypeAssociation findMatchingPolicyCmptTypeAssociation(IIpsProject ipsProject)
            throws CoreException;

}
