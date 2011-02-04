/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt.treestructure;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;

/**
 * A reference to a <code>IProductCmpt</code>. Used by <code>ProductCmptStructure</code>.
 * 
 * Note: This is in fact a Reference to ProductCmptLink not to the ProductCmpt. We might refactor
 * the name once
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptReference extends ProductCmptStructureReference implements IProductCmptReference {

    private final IProductCmpt cmpt;
    private final IProductCmptLink link;

    public ProductCmptReference(IProductCmptTreeStructure structure, ProductCmptStructureReference parent,
            IProductCmpt cmpt, IProductCmptLink link) throws CycleInProductStructureException {

        super(structure, parent);
        this.cmpt = cmpt;
        this.link = link;
    }

    @Override
    public IProductCmpt getProductCmpt() {
        return cmpt;
    }

    @Override
    public IIpsObjectPartContainer getWrapped() {
        return link;
    }

    @Override
    public IIpsObject getWrappedIpsObject() {
        return cmpt;
    }

    @Override
    public IProductCmptLink getLink() {
        return link;
    }

}
