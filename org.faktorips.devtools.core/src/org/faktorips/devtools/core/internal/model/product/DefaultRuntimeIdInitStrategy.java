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
import org.faktorips.devtools.core.model.product.IProductCmptRuntimeIdInitStrategy;

/**
 * Calculates a new runtime id ensured to be unique for all product components contained
 * in the project the product component is contained in and all projects this one depends on.
 * If this project is used by others, it can <strong>not</strong> be ensured that the returned
 * id will be unique for these projects.
 * 
 * @author Thorsten Guenther
 */
public class DefaultRuntimeIdInitStrategy implements
		IProductCmptRuntimeIdInitStrategy {

	/**
	 * {@inheritDoc}
	 */
	public String getRuntimeId(IProductCmpt productCmpt) throws CoreException {
		IIpsProject project = productCmpt.getIpsProject();
		String id = productCmpt.getIpsProject().getRuntimeIdPrefix() + productCmpt.getName();
		String uniqueId = id;
		
		int i = 1;
		while (project.findProductCmpt(uniqueId) != null) {
			uniqueId = id + i;
			i++;
		}
		
		return uniqueId;
	}

}
