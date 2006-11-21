/*******************************************************************************
  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
  *
  * Alle Rechte vorbehalten.
  *
  * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
  * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
  * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
  * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
  *   http://www.faktorips.org/legal/cl-v01.html
  * eingesehen werden kann.
  *
  * Mitwirkende:
  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
  *
  *******************************************************************************/

package org.faktorips.devtools.core.model.product;

import java.util.GregorianCalendar;

import org.faktorips.devtools.core.model.CycleException;

/**
 * A product component structure provides navigation between a product component
 * at a given working date, the relations and the targets of these product
 * component relations.
 * 
 * Because a specific product component can be referenced by more than one other
 * product component, it is not possible to establis a simple "one parent - many
 * children"-relation based on the product component. This leads to the
 * <code>IProductCmptStructureReference</code> and its subclasses which are
 * references to product components or product component type relations. One
 * reference wraps one product component, but it is possible that more than one
 * reference wraps the same product component.
 * 
 * @author Thorsten Guenther
 */
public interface IProductCmptStructure {

	/**
	 * Returns the reference wrapping the root product component of this
	 * structure.
	 */
	public IProductCmptReference getRoot();

	/**
	 * Refreshes the structure to reflect changes to the underlying objects.
	 * 
	 * @throws CycleException
	 *             If a circle is detected.
	 */
	public void refresh() throws CycleException;

	/**
	 * @return Returns the date this structure was created for. That means, all
	 *         relations represented by this structure are valid at the returned
	 *         date.
	 */
	public GregorianCalendar getValidAt();

	/**
	 * Returns all references contained in this structure as plain array. The
	 * order of the nodes is unspecified.
	 * 
	 * @param productCmptOnly
	 *            <code>true</code> to get only references to
	 *            <code>IProductCmpt</code>s.
	 */
	public IProductCmptStructureReference[] toArray(boolean productCmptOnly);

	/**
	 * Returns the parent reference to the given one which refers to a
	 * <code>IProductCmpt</code>.
	 * 
	 * @param child
	 *            The child reference to get the parent from.
	 * @return The found reference to the parent <code>IProductCmpt</code> or
	 *         <code>null</code> if no parent was found (if the given child is
	 *         the root of the structure, for example).
	 */
	public IProductCmptReference getParentProductCmptReference(
			IProductCmptStructureReference child);

	/**
	 * Returns the parent reference to the given one which refers to a
	 * <code>IProductCmptTypeRelation</code>.
	 * 
	 * @param child
	 *            The child reference to get the parent from.
	 * @return The found references to the parent
	 *         <code>IProductCmptTypeRelation</code> or <code>null</code> if
	 *         no parent was found (if the given child is the root of the
	 *         structure, for example).
	 */
	public IProductCmptTypeRelationReference getParentProductCmptTypeRelationReference(
			IProductCmptStructureReference child);

	/**
	 * @param parent
	 *            The parent-reference to start the search for child-references.
	 * @return The found references from the given parent to
	 *         <code>IProductCmpt</code>s.
	 */
	public IProductCmptReference[] getChildProductCmptReferences(
			IProductCmptStructureReference parent);

	/**
	 * @param parent
	 *            The parent-reference to start the search for child-references.
	 * @return The found references from the given parent to
	 *         <code>IProductCmptTypeRelation</code>s.
	 */
	public IProductCmptTypeRelationReference[] getChildProductCmptTypeRelationReferences(
			IProductCmptStructureReference parent);

    /**
     * @param parent
     *            The parent-reference to start the search for child-references.
     * @return The found references from the given parent to
     *         <code>IProductCmptReference</code>s.
     */
    public IProductCmptStructureTblUsageReference[] getChildProductCmptStructureTblUsageReference(
            IProductCmptStructureReference parent);

}
