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

class MyListener implements IpsTestListener {

    int startedCount = 0;
    int finishCount = 0;
    int failureCount = 0;

    IpsTest2 lastStarted;
    IpsTest2 lastFinished;
    IpsTestFailure lastFailure;

    @Override
    public void testStarted(IpsTest2 test) {
        startedCount++;
        lastStarted = test;
    }

    @Override
    public void testFinished(IpsTest2 test) {
        finishCount++;
        lastFinished = test;
    }

    @Override
    public void testFailureOccured(IpsTestFailure failure) {
        failureCount++;
        lastFailure = failure;
    }

}
