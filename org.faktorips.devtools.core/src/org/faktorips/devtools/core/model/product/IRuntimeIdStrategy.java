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
import org.faktorips.devtools.core.model.IIpsProject;


/**
 * Interface for classes that provide a strategy for initializing 
 * the runtime-ids for new product components.
 * 
 * @author Thorsten Guenther
 */
public interface IRuntimeIdStrategy {

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
	public String getRuntimeId(IProductCmpt productCmpt)
			throws CoreException;
	
    /**
     * Finds the runtime id to use for the given project and product component name.
     * The result of this method is not ensured to be the same for different calls.
     * This method should only be used to find the id for a new product component,
     * but does not set the runtime id for the product component.
     * 
     * @param project
     *            The project which will be used to evaluate the runtime id.
     * @param productCmptName 
     *            The name of the new product component for which the runtime id will be 
     *            returned.
     * @throws CoreException
     *             if an error occurs during evaluation.
     */
    public String getRuntimeId(IIpsProject project, String productCmptName)
            throws CoreException;
    
	/**
	 * Compares the runtime ids of the given product components. This method was introduced
	 * because a simple call to <code>equals()</code> comparing the two runtime ids returned by a
	 * call to <code>getRuntimeId()</code> can not be used to decide whether the runtime ids of
	 * two product compontes are the same or not. This is because the strategy might also use the templates
	 * the product component are based on to decide if the two have an identical id.
	 * E.g. a product 1 based on the template MotorProduct and a product 2 based on the template HomeProduct
	 * might both have the same runtime id 42, but because they are based on different templates they are
	 * considered as different.  
	 * 
	 * @param cmpt1 The first product component to check.
	 * @param cmpt2 The second product component to check.
	 * @return <code>true</code> if the runtime ids of both product components 
	 * are the same, <code>false</code> otherwise.
	 */
	public boolean sameRuntimeId(IProductCmpt cmpt1, IProductCmpt cmpt2);
	
}
