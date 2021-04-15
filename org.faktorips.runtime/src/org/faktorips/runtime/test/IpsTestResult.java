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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsTestResult {

    private List<IpsTestListener> listeners = new ArrayList<>();
    private List<IpsTestFailure> failures = new ArrayList<>(100);

    public IpsTestResult() {
        super();
    }

    public int countFailures() {
        return failures.size();
    }

    public void addListener(IpsTestListener listener) {
        listeners.add(listener);
    }

    public void remove(IpsTestListener listener) {
        listeners.remove(listener);
    }

    public void run(IpsTest2 test) {
        test.run(this);
    }

    void run(IpsTestCaseBase test) {
        try {
            notifyListenerAboutStart(test);
            test.executeBusinessLogic();
            test.executeAsserts(this);
            // CSOFF: IllegalCatch
        } catch (Throwable t) {
            // CSON: IllegalCatch
            addFailure(new IpsTestFailure(test, t));
        } finally {
            notifyListenerAboutFinished(test);
        }
    }

    private void notifyListenerAboutStart(IpsTest2 test) {
        for (IpsTestListener listener : listeners) {
            listener.testStarted(test);
        }
    }

    private void notifyListenerAboutFinished(IpsTest2 test) {
        for (IpsTestListener listener : listeners) {
            listener.testFinished(test);
        }
    }

    public void addFailure(IpsTestFailure failure) {
        failures.add(failure);
        for (IpsTestListener listener : listeners) {
            listener.testFailureOccured(failure);
        }
    }

}
