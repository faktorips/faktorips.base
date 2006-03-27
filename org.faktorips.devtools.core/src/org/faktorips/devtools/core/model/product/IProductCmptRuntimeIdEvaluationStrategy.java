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

package org.faktorips.devtools.core.model.product;

import org.eclipse.core.runtime.CoreException;


/**
 * Interface for classes that provide a strategy for evaluating 
 * new runtime-ids for new product components.
 * 
 * @author Thorsten Guenther
 */
public interface IProductCmptRuntimeIdEvaluationStrategy {

	/**
	 * Finds the runtime id to use for the given product component. The result
	 * of this method is not ensured to be the same for different calls. This
	 * method should only be used to find the id for a new product component,
	 * but does not set the runtime id for the product component.
	 * 
	 * @param productCmpt
	 *            The completeley initialized product component to find the
	 *            runtime id for.
	 * @throws CoreException
	 *             if an error occurs during evaluation.
	 */
	public String evaluateRuntimeId(IProductCmpt productCmpt)
			throws CoreException;
	
}
