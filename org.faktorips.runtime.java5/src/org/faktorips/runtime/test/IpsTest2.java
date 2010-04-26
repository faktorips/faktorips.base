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

package org.faktorips.runtime.test;

import org.faktorips.runtime.IRuntimeRepository;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class IpsTest2 {

    private String qName;
    private String fullPath;

    /* Contains the component registry the component uses to resolve references to other components */
    private IRuntimeRepository runtimeRepository;

    public IpsTest2(String qName) {
        this.qName = qName;
    }

    /**
     * Returns the test's qualified name.
     */
    public String getQualifiedName() {
        return qName;
    }

    /**
     * Returns the test's file name including the path.
     */
    public String getFullPath() {
        return fullPath;
    }

    /**
     * Sets the test's file name including the path.
     */
    public void setFullPath(String fileName) {
        this.fullPath = fileName;
    }

    /**
     * Returns the test's unqualified name.
     */
    public String getName() {
        int index = qName.lastIndexOf('.');
        if (index == -1) {
            return qName;
        }
        return qName.substring(index + 1);
    }

    /**
     * Returns the runtime repository for searching and creating object during the test run.
     */
    public IRuntimeRepository getRepository() {
        return runtimeRepository;
    }

    /**
     * Sets the runtime repository.
     */
    public void setRepository(IRuntimeRepository runtimeRepository) {
        this.runtimeRepository = runtimeRepository;
    }

    /**
     * Returns the number of test cases in this test. If this is a test the method returns 1, if
     * this is a suite the number of test cases in the suite itself and all suites that are
     * contained in this one.
     */
    public abstract int countTestCases();

    protected abstract void run(IpsTestResult result);

}
