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

import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.model.product.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.product.ITableContentUsage;

/**
 *  A reference to a <code>ITableContentUsage</code>. Used by <code>ProductCmptStructure</code>.
 *  
 * @author Joerg Ortmann
 */
public class ProductCmptStructureTblUsageReference extends ProductCmptStructureReference implements
        IProductCmptStructureTblUsageReference {

    private ITableContentUsage tableContentUsage;

    public ProductCmptStructureTblUsageReference(IProductCmptStructure structure, ProductCmptStructureReference parent,
            ITableContentUsage tableContentUsage) throws CycleException {
        super(structure, parent);
        this.tableContentUsage = tableContentUsage;
    }
    
    /**
     * {@inheritDoc}
     */
    IIpsElement getWrapped() {
        return tableContentUsage;
    }

    /**
     * {@inheritDoc}
     */
    public ITableContentUsage getTableContentUsage() {
        return tableContentUsage;
    }
}
