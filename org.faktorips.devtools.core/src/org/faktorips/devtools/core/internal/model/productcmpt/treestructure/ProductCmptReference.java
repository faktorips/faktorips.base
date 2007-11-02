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

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;

/**
 * A reference to a <code>IProductCmpt</code>. Used by <code>ProductCmptStructure</code>.
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptReference extends ProductCmptStructureReference
		implements IProductCmptReference {

	private IProductCmpt cmpt;
	
	/**
	 * @param structure
	 * @param parent
	 * @throws CycleInProductStructureException 
	 */
	public ProductCmptReference(IProductCmptTreeStructure structure, ProductCmptStructureReference parent, IProductCmpt cmpt) throws CycleInProductStructureException {
		super(structure, parent);
		this.cmpt = cmpt;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmpt getProductCmpt() {
		return cmpt;
	}

	/**
	 * {@inheritDoc}
	 */
	IIpsElement getWrapped() {
		return cmpt;
	}

}
