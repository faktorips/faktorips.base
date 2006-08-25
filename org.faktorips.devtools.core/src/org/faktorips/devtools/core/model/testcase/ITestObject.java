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
     * Returns the root test policy component element.
     */    
    public ITestObject getRoot();
    
    /**
	 * Returns <code>true</code> if the test parameter is an input object
	 * otherwise <code>false</code>.
	 */
    public boolean isInput();
	
    /**
     * Returns <code>true</code> if the test parameter is a expected object
     * otherwise <code>false</code>.
     */
    public boolean isExpectedResult();
    
    /**
     * Returns <code>true</code> if the test parameter is a combined object
     * (containing input and expected result) otherwise <code>false</code>.
     */
    public boolean isCombined();

    /**
     * Returns <code>true</code> if the test object is a root object otherwise <code>false</code>.
     * @return
     */
    public boolean isRoot();    
}
