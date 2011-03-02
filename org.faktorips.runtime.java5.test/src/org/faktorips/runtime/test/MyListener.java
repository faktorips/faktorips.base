/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.test;

class MyListener implements IpsTestListener {

    int startedCount = 0;
    int finishCount = 0;
    int failureCount = 0;

    IpsTest2 lastStarted;
    IpsTest2 lastFinished;
    IpsTestFailure lastFailure;

    public void testStarted(IpsTest2 test) {
        startedCount++;
        lastStarted = test;
    }

    public void testFinished(IpsTest2 test) {
        finishCount++;
        lastFinished = test;
    }

    public void testFailureOccured(IpsTestFailure failure) {
        failureCount++;
        lastFailure = failure;
    }

}
