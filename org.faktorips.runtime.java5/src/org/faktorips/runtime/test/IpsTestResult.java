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

package org.faktorips.runtime.test;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsTestResult {

    private List<IpsTestListener> listeners = new ArrayList<IpsTestListener>();
    private List<IpsTestFailure> failures = new ArrayList<IpsTestFailure>(100);
    
    /**
     * 
     */
    public IpsTestResult() {
        super();
    }

    public int countFailures(){
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
        } catch (Throwable t) {
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
