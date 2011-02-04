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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
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
    public IIpsObjectPartContainer getWrapped() {
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
