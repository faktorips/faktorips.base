/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.testcasetype;

/**
 * Specification of a test rule parameter.
 * 
 * @author Joerg Ortmann
 */
public interface ITestRuleParameter extends ITestParameter {

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTRULEPARAMETER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the validation rule must have the expected result
     * type.
     */
    public final static String MSGCODE_NOT_EXPECTED_RESULT = MSGCODE_PREFIX + "NotExpectedResult"; //$NON-NLS-1$

}
