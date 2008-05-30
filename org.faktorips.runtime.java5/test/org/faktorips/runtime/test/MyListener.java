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

class MyListener implements IpsTestListener {

    int startedCount = 0;
    int finishCount = 0;
    int failureCount = 0;
    
    IpsTest2 lastStarted;
    IpsTest2 lastFinished;
    IpsTestFailure lastFailure;
    

    /**
     * {@inheritDoc}
     */
    public void testStarted(IpsTest2 test) {
        startedCount++;
        lastStarted = test;
    }

    /**
     * {@inheritDoc}
     */
    public void testFinished(IpsTest2 test) {
        finishCount++;
        lastFinished = test;
    }

    /**
     * {@inheritDoc}
     */
    public void testFailureOccured(IpsTestFailure failure) {
        failureCount++;
        lastFailure = failure;
    }
    
}