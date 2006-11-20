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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IRuntimeIdStrategy;

/**
 * Calculates a new runtime id ensured to be unique for all product components contained
 * in the project the product component is contained in and all projects this one depends on.
 * If this project is used by others, it can <strong>not</strong> be ensured that the returned
 * id will be unique for these projects.
 * 
 * @author Thorsten Guenther
 */
public class DefaultRuntimeIdStrategy implements
		IRuntimeIdStrategy {

	/**
	 * {@inheritDoc}
	 */
	public String getRuntimeId(IProductCmpt productCmpt) throws CoreException {
		return getRuntimeId(productCmpt.getIpsProject(), productCmpt.getName());
	}

    /**
     * {@inheritDoc}
     */
    public String getRuntimeId(IIpsProject project, String productCmptName) throws CoreException {
        String id = project.getRuntimeIdPrefix() + productCmptName;
        String uniqueId = id;
        
        int i = 1;
        while (project.findProductCmptByRuntimeId(uniqueId) != null) {
            uniqueId = id + i;
            i++;
        }
        
        return uniqueId;
    }
    
	/**
	 * {@inheritDoc}
	 */
	public boolean sameRuntimeId(IProductCmpt cmpt1, IProductCmpt cmpt2) {
		return cmpt1.getRuntimeId().equals(cmpt2.getRuntimeId());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		return obj instanceof DefaultRuntimeIdStrategy;
	}	
}
