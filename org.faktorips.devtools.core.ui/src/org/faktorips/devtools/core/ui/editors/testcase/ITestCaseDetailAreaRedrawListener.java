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

import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.testcase.ITestObject;

/**
 * Listener no notify about redrawing the test case detail area.
 * 
 * @author Joerg Ortmann
 */
public interface ITestCaseDetailAreaRedrawListener {

    public void visibleTestObjectsChanges(List<ITestObject> visibleTestObjects) throws IpsException;

}
