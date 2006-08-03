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

package org.faktorips.devtools.core.model.testcase;

import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 *  Specification of a test object.
 *  
 * @author Joerg Ortmann
 */
public interface ITestObject extends IIpsObjectPart  {
	
	/**
	 * Returns <code>true</code> if the test parameter is an input parameter
	 * or <code>false</code> if this is an expected result test parameter.
	 */
	public boolean isInputObject();
	
	/**
	 * Set <code>true</code> if this parameter is an input or 
	 * <code>false</code> if this is an expected result test parameter.
	 */
	public void setInputParameter(boolean isInputType);
}
