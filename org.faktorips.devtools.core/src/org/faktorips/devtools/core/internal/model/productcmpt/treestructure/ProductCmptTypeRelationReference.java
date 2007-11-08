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
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * A reference to a <code>IProductCmptTypeRelation</code>. Used by <code>ProductCmptStructure</code>.
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptTypeRelationReference extends
		ProductCmptStructureReference implements
		IProductCmptTypeRelationReference {

	private IProductCmptTypeAssociation association;
	
	/**
	 * @param structure
	 * @param parent
	 * @throws CycleInProductStructureException 
	 */
	public ProductCmptTypeRelationReference(IProductCmptTreeStructure structure, ProductCmptStructureReference parent, IProductCmptTypeAssociation association) throws CycleInProductStructureException {
		super(structure, parent);
		this.association = association;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTypeAssociation getRelation() {
		return association;
	}

	/**
	 * {@inheritDoc}
	 */
	IIpsElement getWrapped() {
		return association;
	}

}
