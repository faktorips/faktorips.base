/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.treestructure;

import java.util.ArrayList;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Abstract reference for <code>ProductCmptStructure</code>.
 * 
 * @author Thorsten Guenther
 */
public abstract class ProductCmptStructureReference implements
		IProductCmptStructureReference {

	private IProductCmptTreeStructure structure;

	private ProductCmptStructureReference parent;

	private ProductCmptStructureReference[] children;

	public ProductCmptStructureReference(IProductCmptTreeStructure structure,
			ProductCmptStructureReference parent) throws CycleInProductStructureException {
		this.structure = structure;
		this.parent = parent;
		this.children = new ProductCmptStructureReference[0];
		detectCycle(new ArrayList());
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTreeStructure getStructure() {
		return structure;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptStructureReference getParent() {
		return parent;
	}

	/**
	 * @return The children of this reference
	 */
	ProductCmptStructureReference[] getChildren() {
		return children;
	}

	/**
	 * Set the children for this reference.
	 * 
	 * @param children
	 *            The new children.
	 */
	void setChildren(ProductCmptStructureReference[] children) {
		this.children = children;
	}

	private void detectCycle(ArrayList seenElements) throws CycleInProductStructureException {
		if (!(getWrapped() instanceof IProductCmptTypeAssociation)
				&& seenElements.contains(getWrapped())) {
			seenElements.add(getWrapped());
			throw new CycleInProductStructureException((IIpsElement[]) seenElements
					.toArray(new IIpsElement[seenElements.size()]));
		} else {
			seenElements.add(getWrapped());
			if (parent != null) {
				parent.detectCycle(seenElements);
			}
		}
	}
	
	/**
     * @return The <code>IIpsElement</code> referenced by this object.
     */
	abstract IIpsElement getWrapped();
}
