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

import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

/**
 * A product component structure provides navigation between a product
 * component at a given working date, the relation types defined in the product
 * component type and the targets of instances of these product component type relations.
 * 
 * @author Thorsten Guenther
 */
public interface IProductCmptStructure {

	/**
	 * Returns all product components which are targets of relations of the given relation type.
	 * 
	 * @param relationType The product component relation type for the relations to follow.
	 * @param cmpt The product component to get the relations from.
	 * 
	 * @return All found product componets or an empty array if no relations of the given type are
	 * defined.
	 */
	public IProductCmpt[] getTargets(IProductCmptTypeRelation relationType, IProductCmpt cmpt);
	
	/**
	 * Returns all product component relation types defined for the given product component.
	 */
	public IProductCmptTypeRelation[] getRelationTypes(IProductCmpt cmpt);

	/**
	 * Returns an array of either product components (<code>IProductCmpt</code>) if the 
	 * parent is a product component relation type (<code>IProductCmptTypeRelation</code>)
	 * or product component relation types, if the parent is a product component.
	 * 
	 * @return as described abvoe or an empty array, if no children are available.
	 */
	public IIpsObjectPartContainer[] getChildren(IIpsObjectPartContainer parent);

	/**
	 * Returns the parent in the context of this structure. If the child is a product component,
	 * the result is a product component relation type. If the child ia a product component
	 * relation type, the result is a product component. 

	 * @return as described above or null, if the child is the root.
	 */
	public IIpsObjectPartContainer getParent(IIpsObjectPartContainer child);
	
	/**
	 * Returns the product component this structure is rooted at.
	 */
	public IProductCmpt getRoot();
	
	/**
	 * Refreshes the structure to reflect changes to the underlying objects.
	 */
	public void refresh();
}
