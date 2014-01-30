/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.treestructure;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;

/**
 * A reference to a <code>ITableContentUsage</code>. Used by <code>ProductCmptStructure</code>.
 * 
 * @author Joerg Ortmann
 */
public class ProductCmptStructureTblUsageReference extends ProductCmptStructureReference implements
        IProductCmptStructureTblUsageReference {

    private final ITableContentUsage tableContentUsage;

    /**
     * Not final for lazy load
     */
    private ITableContents tableContent;

    public ProductCmptStructureTblUsageReference(IProductCmptTreeStructure structure,
            ProductCmptStructureReference parent, ITableContentUsage tableContentUsage)
            throws CycleInProductStructureException {

        super(structure, parent);
        this.tableContentUsage = tableContentUsage;
    }

    @Override
    public IIpsObjectPart getWrapped() {
        return tableContentUsage;
    }

    @Override
    public ITableContentUsage getTableContentUsage() {
        return tableContentUsage;
    }

    @Override
    public IIpsObject getWrappedIpsObject() {
        if (tableContent != null) {
            return tableContent;
        }
        if (tableContentUsage == null) {
            return null;
        }
        /*
         * This lazy-load is not thread safe. But it doesn't matter when we have to find it twice.
         * However there is nearly no multi-threading access to this method.
         */
        try {
            tableContent = tableContentUsage.findTableContents(tableContentUsage.getIpsProject());
            return tableContent;
        } catch (CoreException e) {
            // will be handled as validation error
            IpsPlugin.log(e);
        }
        return null;
    }

}
