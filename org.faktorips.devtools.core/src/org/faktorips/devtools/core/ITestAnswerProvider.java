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

package org.faktorips.devtools.core;

/**
 * Interface for classes which provide values to be returned on simulated 
 * user interactions.
 * 
 * @author Thorsten Guenther
 */
public interface ITestAnswerProvider {
	
	/**
	 * Returns the answer as boolean.
	 */
	public boolean getBooleanAnswer();
	
	/**
	 * Returns the answer as String.
	 */
	public String getStringAnswer();
	
	/**
	 * Returns the answer as unspecified object.
	 */
	public Object getAnswer();

}
