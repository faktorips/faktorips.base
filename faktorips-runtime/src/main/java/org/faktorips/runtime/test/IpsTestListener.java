/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.test;

/**
 * 
 * @author Jan Ortmann
 */
public interface IpsTestListener {

    public void testStarted(IpsTest2 test);

    public void testFinished(IpsTest2 test);

    public void testFailureOccured(IpsTestFailure failure);

}
