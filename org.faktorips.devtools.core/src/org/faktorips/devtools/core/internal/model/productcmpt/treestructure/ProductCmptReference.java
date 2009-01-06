/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt.treestructure;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
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

	/**
     * {@inheritDoc}
     */
    public IIpsObject getWrappedIpsObject() {
        return cmpt;
    }

}
