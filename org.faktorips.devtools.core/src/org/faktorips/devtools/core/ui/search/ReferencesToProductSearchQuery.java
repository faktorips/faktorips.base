/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;

/**
 * 
 * 
 * @author Stefan Widmaier
 */
public class ReferencesToProductSearchQuery extends ReferenceSearchQuery {
    
    public ReferencesToProductSearchQuery(IProductCmpt referenced) {
        super(referenced);
    }
    
    /**
     * @inheritDoc
     */
	protected IIpsElement[] findReferences() throws CoreException{
	    return referenced.getIpsProject().findReferencingProductCmptGenerations(referenced.getQualifiedNameType());
	}
    
    /**
     * @inheritDoc
     */
    protected Object[] getDataForResult(IIpsElement object) {
        return new Object[]{((IProductCmptGeneration)object).getProductCmpt(), object};
	}
}
