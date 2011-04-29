/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;

/**
 * Find references to a table contents. This query searches for product cmpt's which contains an
 * reference to the given table contents.
 * 
 * @author Joerg Ortmann
 */
public class ReferencesToTableContentsSearchQuery extends ReferenceSearchQuery {

    public ReferencesToTableContentsSearchQuery(ITableContents referenced) {
        super(referenced);
    }

    @Override
    protected IIpsElement[] findReferences() throws CoreException {
        return referenced.getIpsProject().findReferencingProductCmptGenerations(referenced.getQualifiedNameType());
    }

    @Override
    protected Object[] getDataForResult(IIpsElement object) {
        if (object instanceof IProductCmptGeneration) {
            return new Object[] { ((IProductCmptGeneration)object).getProductCmpt(), object };
        }
        return null;
    }
}
