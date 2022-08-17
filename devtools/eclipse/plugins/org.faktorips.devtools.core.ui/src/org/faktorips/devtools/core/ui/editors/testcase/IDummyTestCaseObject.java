/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import org.faktorips.devtools.model.Validatable;
import org.faktorips.devtools.model.testcasetype.ITestParameter;

/**
 * Helper interface for all dummy test objects, a dummy test object is virtual test object displayed
 * in the user interface. E.g. TestCaseTypeRule, TestCaseTypeAssociation.
 */
public interface IDummyTestCaseObject extends Validatable {

    /**
     * Returns the test association type parameter.
     */
    ITestParameter getTestParameter();
}
