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

package org.faktorips.devtools.core.model;


/**
 * Thrown when a cycle relation is detected. A cycle relation means
 * that one product or policy component has a relation to itself at least
 * for one way for component follwing the ralation to its target, taking the
 * relations defined on the target and following them to the targets and so on.
 * 
 * @author Thorsten Guenther
 */
public class CycleException extends Exception {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -3945323856832361062L;

	private IIpsElement[] cyclePath;

	/**
	 * Creates a new exception with the given path. 
	 */
	public CycleException(IIpsElement[] cyclePath) {
		this.cyclePath = cyclePath;
	}
	
	/**
	 * Returns the path for this cycle. The content of the path 
	 * depends on the creator of this exception.
	 */
	public IIpsElement[] getCyclePath() {
		return cyclePath;
	}
	
}
