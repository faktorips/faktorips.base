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


/**
 *  Specification of a test rule parameter.
 *  
 * @author Joerg Ortmann
 */
public interface ITestRuleParameter extends ITestParameter {

    /** Property names */
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTRULEPARAMETER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the validation rule must have the expected result type.
     */
    public final static String MSGCODE_NOT_EXPECTED_RESULT = MSGCODE_PREFIX + "NotExpectedResult"; //$NON-NLS-1$
}
