/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;

/**
 * Helper interface for all dummy test objects, a dummy test object is virtual test object displayed
 * in the user interface. E.g. TestCaseTypeRule, TestCaseTypeAssociation.
 */
public interface IDummyTestCaseObject extends Validatable {

    /**
     * Returns the test association type parameter.
     */
    public ITestParameter getTestParameter();
}
