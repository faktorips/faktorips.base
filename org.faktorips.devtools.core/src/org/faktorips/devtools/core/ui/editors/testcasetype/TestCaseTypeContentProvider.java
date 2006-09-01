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

package org.faktorips.devtools.core.ui.editors.testcasetype;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;

/**
 * Content provider for the test case domain.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeContentProvider implements ITreeContentProvider {
	private static Object[] EMPTY_ARRAY = new Object[0];
	
	/**
	 * {@inheritDoc}
	 */
	public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ITestPolicyCmptTypeParameter){
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter) parentElement;
            return testPolicyCmptTypeParam.getTestPolicyCmptTypeParamChilds();
        }
	    return EMPTY_ARRAY;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getParent(Object element) {
	    // only the objects above have parents, in other case no parent necessary
	    return null;
	}

	/**
	 * {@inheritDoc}
	 */	
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		if (children==null) {
			return false;
		}
		return children.length > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ITestCaseType){
            ITestCaseType testCaseType = (ITestCaseType) inputElement;
            ArrayList elemens = new ArrayList();
            ITestValueParameter testValueParams[] = testCaseType.getTestValueParameters();
            for (int i = 0; i < testValueParams.length; i++) {
                elemens.add(testValueParams[i]);
            }
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParams[] = testCaseType.getTestPolicyCmptTypeParameters();
            for (int i = 0; i < testPolicyCmptTypeParams.length; i++) {
                elemens.add(testPolicyCmptTypeParams[i]);
            }
            return elemens.toArray();
        }
        return EMPTY_ARRAY;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to do
	}
}
