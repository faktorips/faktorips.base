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

import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * A product component structure provides navigation between a product
 * component at a given working date, the relations and the targets of these 
 * product component relations.
 * 
 * Because a specific product component can be referenced by more than one other product component, 
 * it is not possible to establis a simple "one parent - many children"-relation based on the 
 * product component. This leads to the <code>IStructureNode</code> which wraps a product component.
 * One node wraps one product component, but it is possible that more than one node wraps the same 
 * product component.
 *   
 * @author Thorsten Guenther
 */
public interface IProductCmptStructure {

	/**
	 * Returns the product component this structure is rooted at.
	 */
	public IProductCmpt getRoot();
	
	/**
	 * Returns the node wrapping the root product component of this structure.
	 */
	public IStructureNode getRootNode();

	/**
	 * Refreshes the structure to reflect changes to the underlying objects.
	 * @throws CycleException If a circle is detected.
	 */
	public void refresh() throws CycleException;
	
	/**
	 * A node wrapping either an <code>IProductCmpt</code> or an <code>IProductCmptRelation</code> 
	 * 
	 * @author Thorsten Guenther
	 */
	public interface IStructureNode {
		/**
		 * Returns the parent of this node.
		 * 
		 * @return The parent node or null, if this is the root node.
		 */
		public IStructureNode getParent();
		
		/**
		 * Returns the children for this node.
		 * 
		 * @return An array containing all children of this node or an empty
		 *         array if no children are available
		 */
		public IStructureNode[] getChildren();

		/**
		 * Returns the IIpsElement which is wrapped by this node. This can be
		 * either a IProductCmpt or an IProductCmptRelation
		 */
		public IIpsElement getWrappedElement();
	}
}
