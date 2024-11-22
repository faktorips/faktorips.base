/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt.treestructure;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.internal.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

/**
 * A product component tree structure provides a tree view for product components starting with a
 * product component used as root. Child nodes are created for the links to other components.
 * <p>
 * Because a specific product components can be referenced by more than one other product component,
 * it is not possible to establish a simple "one parent - many children"-relation based on the
 * product component. This leads to the <code>IProductCmptStructureReference</code> and its
 * subclasses which are references to product components or product component type associations. One
 * reference wraps one product component, but it is possible that more than one reference wraps the
 * same product component.
 * <p>
 * It is also not possible to setup the structure for a special generation but for a concrete date.
 * This is because the structure is built for a set of product components which may have different
 * adjustment dates.
 *
 * @author Thorsten Guenther
 */
public interface IProductCmptTreeStructure {

    /**
     * Factory method to create a ProductCmptTreeStructure instance.
     *
     * @param root The product component to create a structure for.
     * @param date The date the structure has to be valid for.
     * @param project The IPS project used as a search path.
     *
     * @return A new instance of IProductCmptTreeStructure.
     *
     * @throws CycleInProductStructureException if a cycle is detected.
     * @throws NullPointerException if root or date is null.
     *
     * @since 25.1
     */
    static IProductCmptTreeStructure of(IProductCmpt root, GregorianCalendar date, IIpsProject project)
            throws CycleInProductStructureException {
        return new ProductCmptTreeStructure(root, date, project);
    }

    /**
     * Returns the reference wrapping the root product component of this structure.
     */
    IProductCmptReference getRoot();

    /**
     * Refreshes the structure to reflect changes to the underlying objects.
     *
     * @throws CycleInProductStructureException If a circle is detected.
     */
    void refresh() throws CycleInProductStructureException;

    /**
     * @return Returns the date this structure was created for. That means, all relations
     *             represented by this structure are valid at the returned date.
     */
    GregorianCalendar getValidAt();

    /**
     * Returns all references contained in this structure as plain list.
     *
     * @param productCmptOnly <code>true</code> to get only references to <code>IProductCmpt</code>
     *            s.
     */
    Set<IProductCmptStructureReference> toSet(boolean productCmptOnly);

    /**
     * Returns the parent reference to the given one which refers to a <code>IProductCmpt</code>.
     *
     * @param child The child reference to get the parent from.
     * @return The found reference to the parent <code>IProductCmpt</code> or <code>null</code> if
     *             no parent was found (if the given child is the root of the structure, for
     *             example).
     */
    IProductCmptReference getParentProductCmptReference(IProductCmptStructureReference child);

    /**
     * Returns the parent reference to the given one which refers to a
     * <code>IProductCmptTypeRelation</code>.
     *
     * @param child The child reference to get the parent from.
     * @return The found references to the parent <code>IProductCmptTypeRelation</code> or
     *             <code>null</code> if no parent was found (if the given child is the root of the
     *             structure, for example).
     */
    IProductCmptTypeAssociationReference getParentProductCmptTypeRelationReference(
            IProductCmptStructureReference child);

    /**
     * @param parent The parent-reference to start the search for child-references.
     * @return The found references from the given parent to <code>IProductCmpt</code>s.
     */
    IProductCmptReference[] getChildProductCmptReferences(IProductCmptStructureReference parent);

    /**
     * Get all product component type association references from the parent structure reference.
     * Empty associations are included.
     *
     *
     * @param parent The parent-reference to start the search for child-references.
     * @return The found references from the given parent to <code>IProductCmptTypeRelation</code>s.
     */
    IProductCmptTypeAssociationReference[] getChildProductCmptTypeAssociationReferences(
            IProductCmptStructureReference parent);

    /**
     * Get all product component type association references from the parent structure reference.
     *
     * @param parent The parent-reference to start the search for child-references.
     * @param includeEmptyAssociations true if empty associations have to be included
     * @return The found references from the given parent to <code>IProductCmptTypeRelation</code>s.
     */
    IProductCmptTypeAssociationReference[] getChildProductCmptTypeAssociationReferences(
            IProductCmptStructureReference parent,
            boolean includeEmptyAssociations);

    /**
     * @param parent The parent-reference to start the search for child-references.
     * @return The found references from the given parent to <code>IProductCmptReference</code>s.
     */
    IProductCmptStructureTblUsageReference[] getChildProductCmptStructureTblUsageReference(
            IProductCmptStructureReference parent);

    /**
     * @param parent The parent-reference to start the search for child-references.
     * @return The found references from the given parent to <code>IProductCmptReference</code>s.
     */
    IProductCmptVRuleReference[] getChildProductCmptVRuleReferences(IProductCmptStructureReference parent);

    /**
     * Searches this structure/tree for {@link IProductCmptReference IProductCmptReferences}
     * containing the given qualified name of a {@link IProductCmpt}.
     *
     * @param string the {@link IProductCmpt}'s qualified name to search for
     * @return <code>true</code> if this structure references the given qualified name,
     *             <code>false</code> otherwise.
     */
    boolean referencesProductCmptQualifiedName(String string);

    /**
     * Searches this structure/tree for {@link IProductCmptReference references} to any of the given
     * {@link IProductCmpt product components}.
     *
     * @return a list of references referring to at least one {@link IProductCmpt} given in the
     *             list.
     */
    List<IProductCmptReference> findReferencesFor(List<IProductCmpt> cmpts);

    /**
     * Returns the validTo property from the {@link IProductCmptReference}
     */
    GregorianCalendar getValidTo();

}
