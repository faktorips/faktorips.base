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

package org.faktorips.devtools.core.model.testcasetype;

import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 *  Specification of a test parameter.
 *  
 * @author Joerg Ortmann
 */
public interface ITestParameter extends IIpsObjectPart {

	/**
	 * Returns the parameters's name, e.g. 'effectiveDate'.
	 * {@inheritDoc}
	 */
	public String getName();

	/**
	 * Sets the parameters's name, e.g. 'effectiveDate'.
	 */
	public void setName(String newName);
	
	/**
	 * Returns <code>true</code> if the test parameter is an input parameter, 
	 * otherwise <code>false</code>.
	 */
	public boolean isInputParameter();
	
	/**
	 * Returns <code>true</code> if the parameter is an expected result parameter, 
	 * otherwise <code>false</code>.
	 */
	public boolean isExpextedResultParameter();

	/**
	 * Set <code>true</code> if this parameter is an input or <code>false</code> 
	 * if this is an expected result test parameter.
	 */
	public void setInputParameter(boolean isInputType);
}
