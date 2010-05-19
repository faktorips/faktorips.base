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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;

/**
 * A reference to a <code>ITableContentUsage</code>. Used by <code>ProductCmptStructure</code>.
 * 
 * @author Joerg Ortmann
 */
public class ProductCmptStructureTblUsageReference extends ProductCmptStructureReference implements
        IProductCmptStructureTblUsageReference {

    private ITableContentUsage tableContentUsage;

    public ProductCmptStructureTblUsageReference(IProductCmptTreeStructure structure,
            ProductCmptStructureReference parent, ITableContentUsage tableContentUsage)
            throws CycleInProductStructureException {
        super(structure, parent);
        this.tableContentUsage = tableContentUsage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IIpsObjectPartContainer getWrapped() {
        return tableContentUsage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITableContentUsage getTableContentUsage() {
        return tableContentUsage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsObject getWrappedIpsObject() {
        try {
            if (tableContentUsage == null) {
                return null;
            }
            return tableContentUsage.findTableContents(tableContentUsage.getIpsProject());
        } catch (CoreException e) {
            // will be handled as validation error
            IpsPlugin.log(e);
        }
        return null;
    }
}
