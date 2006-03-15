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

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

/**
 * Thrown when a circle relation is detected. A circle relations means
 * that one product or policy component has a relation to itself at least
 * for one way for component follwing the ralation to its target, taking the
 * relations defined on the target and following them to the targets and so on.
 * 
 * @author Thorsten Guenther
 */
public class CircleRelationException extends Exception {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -3945323856832361062L;

	private IIpsElement[] circlePath;

	/**
	 * Creates a new exception with the given path. The first element 
	 * in the path has to be the <code>IProductCmpt</code> the
	 * circle was detected at. The second element contains the <code>
	 * IProductCmptTypeRelation</code> the relation to the next
	 * <code>IProductCmpt</code> on the circle path is the target from
	 * and so on. 
	 */
	public CircleRelationException(IIpsElement[] circlePath) {
		
		for (int i = 0; i < circlePath.length; i++) {
			if (i % 2 == 0 && !(circlePath[i] instanceof IProductCmpt)) {
				throw new RuntimeException("Circle path not in the required form"); //$NON-NLS-1$
			} else if (i % 2 == 1 && !(circlePath[i] instanceof IProductCmptTypeRelation)) {
				throw new RuntimeException("Circle path not in the required form"); //$NON-NLS-1$
			}
		}
		
		
		this.circlePath = circlePath;
	}
	
	/**
	 * Returns the path of <code>IProductCmpt</code>s and 
	 * <code>IProductCmptTypeRelation</code>s. The first element 
	 * in the returned array is the <code>IProductCmpt</code> the
	 * circle was detected at. The second element contains the <code>
	 * IProductCmptTypeRelation</code> the relation to the next
	 * <code>IProductCmpt</code> on the circle path is the target from
	 * and so on.
	 */
	public IIpsElement[] getCirclePath() {
		return circlePath;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		for (int i = circlePath.length-1; i >= 0; i--) {
			result.append(circlePath[i].getName());
			if (i%2 != 0) {
				result.append(" -> "); //$NON-NLS-1$
			}
			else if (i%2 == 0 && i > 0) {
				result.append(":"); //$NON-NLS-1$
			}
		}
		return result.toString();
	}
	
}
