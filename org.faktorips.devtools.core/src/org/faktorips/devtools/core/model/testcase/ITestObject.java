/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;

/**
 * Specification of a test object.
 * 
 * @author Joerg Ortmann
 */
public interface ITestObject extends IIpsObjectPart {

    /**
     * Returns the name of the corresponding test parameter.
     */
    public String getTestParameterName();

    /**
     * Returns the root test policy component element.
     */
    public ITestObject getRoot();

    /**
     * Returns <code>true</code> if the test parameter is an input object otherwise
     * <code>false</code>.
     */
    public boolean isInput();

    /**
     * Returns <code>true</code> if the test parameter is a expected object otherwise
     * <code>false</code>.
     */
    public boolean isExpectedResult();

    /**
     * Returns <code>true</code> if the test parameter is a combined object (containing input and
     * expected result) otherwise <code>false</code>.
     */
    public boolean isCombined();

    /**
     * Returns <code>true</code> if the test object is a root object otherwise <code>false</code>.
     */
    public boolean isRoot();

    /**
     * Finds the corresponding test parameter. Returns <code>null</code> if no test parameter found.
     */
    public ITestParameter findTestParameter(IIpsProject ipsProject) throws CoreException;

}
