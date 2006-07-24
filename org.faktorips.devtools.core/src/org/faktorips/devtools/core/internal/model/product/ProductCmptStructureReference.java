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

package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;

import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.model.product.IProductCmptSturctureReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

/**
 * Abstract reference for <code>ProductCmptStructure</code>.
 * 
 * @author Thorsten Guenther
 */
public abstract class ProductCmptStructureReference implements
		IProductCmptSturctureReference {

	private IProductCmptStructure structure;

	private ProductCmptStructureReference parent;

	private ProductCmptStructureReference[] children;

	public ProductCmptStructureReference(IProductCmptStructure structure,
			ProductCmptStructureReference parent) throws CycleException {
		this.structure = structure;
		this.parent = parent;
		this.children = new ProductCmptStructureReference[0];
		detectCycle(new ArrayList());
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptStructure getStructure() {
		return structure;
	}

	/**
	 * @return The parent of this reference
	 */
	ProductCmptStructureReference getParent() {
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

	private void detectCycle(ArrayList seenElements) throws CycleException {
		if (!(getWrapped() instanceof IProductCmptTypeRelation)
				&& seenElements.contains(getWrapped())) {
			seenElements.add(getWrapped());
			throw new CycleException((IIpsElement[]) seenElements
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
