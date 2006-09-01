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

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.util.ArgumentCheck;

/**
 * Content provider for the test case domain.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseContentProvider implements ITreeContentProvider {
	private static Object[] EMPTY_ARRAY = new Object[0];
	
	/** Defines the type of the content which will be currently provided: 
	 *    the input objects, the expected result objects, or both could be provided */
	public static final int INPUT = 0;
	public static final int EXPECTED_RESULT = 1;
    public static final int COMBINED = 2;
	public static final int UNKNOWN = -1;
	private int contentType = UNKNOWN;
	
	/** Sort functions */
	public static TestPolicyCmptSorter TESTPOLICYCMPT_SORTER = new TestPolicyCmptSorter();
	public static TestValueSorter TESTVALUE_SORTER = new TestValueSorter();
	
	// Contains the test case for which the content will be provided
	private ITestCase testCase;
	
	// Indicates if the structure should be displayed without relation layer
	private boolean withoutRelations = false;
	
	public TestCaseContentProvider(int contentType, ITestCase testCase){
		ArgumentCheck.notNull(testCase);
		this.contentType = contentType;
		this.testCase = testCase;
	}
	
	/**
	 * Returns the test case.
	 */
	public ITestCase getTestCase(){
		return testCase;
	}
	
	/**
	 * Returns <code>true</code> if the content will be provided without the relation layer.
	 * If the complete structure will be displayed (with relations) then <code>false</code> will be returned.
	 */
	public boolean isWithoutRelations() {
		return withoutRelations;
	}

	/**
	 * Set if the relation layer will be shown <code>false</code> 
	 * or if the relation should be hidden <code>true</code>.
	 */
	public void setWithoutRelations(boolean withoutRelations) {
		this.withoutRelations = withoutRelations;
	}
	
	/**
	 * Returns the int value for the corresponding type, input or expected result.
	 */
	public int getContentType() {
		return contentType;
	}

	/**
	 * Returns the corresponding test policy component objects.<br>
	 * Input, expected result or both objects.<br>
	 * Rerurns <code>null</code> if this content provider has an unkown type.
	 */
	public ITestPolicyCmpt[] getPolicyCmpt(){
		if (isInput()){
			return testCase.getInputTestPolicyCmpts();
		}else if (isExpectedResult()){
			return testCase.getExpectedResultTestPolicyCmpts();
		}else if (isCombined()){
		    return testCase.getTestPolicyCmpts();
        }
		return null;
	}
	
	/**
	 * Returns the corresponding test value objects.<br>
	 * Input or expected result objects.<br>
	 * Rerurns <code>null</code> if this content provider has an unkown type.
	 */
	public ITestValue[] getValues(){
		if (isCombined()){
            return testCase.getTestValues();
		}else if (isExpectedResult()){
			return testCase.getExpectedResultTestValues();
		}else if (isInput()){
		    return testCase.getInputTestValues();
        }
		return null;
	}
	
	/**
	 * Returns <code>true</code> if this content provider provides the input objetcs of the test case.
	 */
	public boolean isInput(){
		return contentType == INPUT || contentType == COMBINED;
	}
	
	/**
	 * Returns <code>true</code> if this content provider provides the expected result objects of the test case.
	 */	
	public boolean isExpectedResult(){
		return contentType == EXPECTED_RESULT || contentType == COMBINED;
	}

    /**
	 * Returns <code>true</code> if this content provider provides the expected result and input objects of the test case.
	 */	
	public boolean isCombined(){
	    return contentType == COMBINED;
	}
	
    /**
     * Sets the content type of the content provider.
     */
    public void setContentType(int contentType){
        this.contentType = contentType;
    }
    
	/**
	 * {@inheritDoc}
	 */
	public Object[] getChildren(Object parentElement) {
	    if(parentElement instanceof ITestPolicyCmpt) {
	        return getChildsForTestPolicyCmpt((ITestPolicyCmpt)parentElement);
	    }else if(parentElement instanceof ITestPolicyCmptRelation){
	    	return getChildsForTestPolicyCmptRelation((ITestPolicyCmptRelation) parentElement);
	    }else if(parentElement instanceof TestCaseTypeRelation){
	    	return getChildsForTestCaseTypeRelation((TestCaseTypeRelation) parentElement);
	    }
	    return EMPTY_ARRAY;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getParent(Object element) {
	    if(element instanceof ITestPolicyCmpt) {
	        return ((ITestPolicyCmpt)element).getParent();
	    }else if(element instanceof ITestPolicyCmptRelation){
	    	return ((ITestPolicyCmptRelation) element).getParent();
	    }else if(element instanceof TestCaseTypeRelation){
	    	return ((TestCaseTypeRelation) element).getParentTestPolicyCmpt();
	    }
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
		if (inputElement instanceof ITestCase){
			ITestCase testCase = (ITestCase) inputElement;
			ArrayList elements = new ArrayList();
			if (isCombined()){
			    // return input and expected result objects
			    ITestValue[] testValues = testCase.getTestValues();
			    for (int i = 0; i < testValues.length; i++) {
			        elements.add(testValues[i]);
			    }
			    ITestPolicyCmpt[] testPolicyCmpts = testCase.getTestPolicyCmpts();
			    for (int i = 0; i < testPolicyCmpts.length; i++) {
			        elements.add(testPolicyCmpts[i]);
			    }
			}else if(isExpectedResult()){
				// return expected result objects
			    ITestValue[] expectedResulTestValues = testCase.getExpectedResultTestValues();
			    for (int i = 0; i < expectedResulTestValues.length; i++) {
			        ITestValue value = expectedResulTestValues[i];
			        elements.add(value);
			    }
				ITestPolicyCmpt[] expectedResultPolicyCmpts = testCase.getExpectedResultTestPolicyCmpts();
				for (int i = 0; i < expectedResultPolicyCmpts.length; i++) {
					elements.add(expectedResultPolicyCmpts[i]);
				}
			}else if(isInput()){
			    // return input objects
			    ITestValue[] inputTestValues = testCase.getInputTestValues();
			    for (int i = 0; i < inputTestValues.length; i++) {
			        elements.add(inputTestValues[i]);
			    }
			    ITestPolicyCmpt[] inputPolicyCmpts = testCase.getInputTestPolicyCmpts();
			    for (int i = 0; i < inputPolicyCmpts.length; i++) {
			        elements.add(inputPolicyCmpts[i]);
			    }
            }
			return elements.toArray();
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

	/**
	 * Returns all test policy component objects which are provided by this provider.
	 */
	public ITestPolicyCmpt[] getPolicyCmpts() {
	    if(isCombined()){
	        return testCase.getTestPolicyCmpts();
        }else if (isInput()){
			return testCase.getInputTestPolicyCmpts();
		}else if (isExpectedResult()){
            return testCase.getExpectedResultTestPolicyCmpts();
        }else{
			return new ITestPolicyCmpt[0];
		}
	}

	/**
	 * Finds the test policy component by the given path.
	 */
	public ITestPolicyCmpt findPolicyCmpt(String path) throws CoreException {
		return testCase.findTestPolicyCmpt(path);
	}
	
	/*
	 * Returns all child of the given test case type relation parameter 
	 * (dummy relation based on the test case type definition)
	 */
	private Object[] getChildsForTestCaseTypeRelation(TestCaseTypeRelation dummyRelation) {
		// show instances of this test policy component type parameter
		ArrayList childs = new ArrayList();
		
		ITestPolicyCmpt parent = dummyRelation.getParentTestPolicyCmpt();
		if (parent != null){
			ITestPolicyCmptRelation[] relations = parent.getTestPolicyCmptRelations(dummyRelation.getName());
			for (int i = 0; i < relations.length; i++) {
				ITestPolicyCmptRelation relation = relations[i];
				if (relation.isComposition()){            
					try {
                        if ((isExpectedResult() && relation.findTarget().isExpectedResult())
                         || (isInput() && relation.findTarget().isInput()))
                            childs.add(relation.findTarget());
                    } catch (CoreException e) {
                        // ignore exception, the failure will be displayed by the validation
                    }
				}else{
					childs.add(relation);
				}
			}
		}
		return childs.toArray(new IIpsElement[0]);
	}

	/*
	 * Returns all child of the given test case relation.
	 */
	private Object[] getChildsForTestPolicyCmptRelation(ITestPolicyCmptRelation testPcRelation) {
		if (testPcRelation.isAccoziation()){
			return EMPTY_ARRAY;
		}else{
			ITestPolicyCmpt[] childs = new ITestPolicyCmpt[1];
			try {
				childs[0] = testPcRelation.findTarget();
			} catch (CoreException e) {
				return EMPTY_ARRAY;
			}
			return childs;
		}
	}

	/*
	 * Returns childs of the test policy component.
	 */
	private Object[] getChildsForTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) {
		ITestPolicyCmptRelation[] relations = testPolicyCmpt.getTestPolicyCmptRelations();
		if (withoutRelations){
			// show childs without relation layer
			List childTestPolicyCmpt = new ArrayList(relations.length);
			for (int i = 0; i < relations.length; i++) {
				ITestPolicyCmptRelation relation = relations[i];
				if (relation.isComposition()){
					ITestPolicyCmpt target=null;
					try {
						target = relation.findTarget();
					} catch (CoreException e) {
						IpsPlugin.logAndShowErrorDialog(e);
					}
                    if ((isInput() && target.isInput()) || (isExpectedResult() && target.isExpectedResult()))
                        childTestPolicyCmpt.add(target);
				}else{
                    // assoziation will be added
                    childTestPolicyCmpt.add(relations[i]);
				}
			}
			return (IIpsElement[]) childTestPolicyCmpt.toArray(new IIpsElement[0]);
		}else{
			// group childs using the test policy component type
			ArrayList childs = new ArrayList();
			ArrayList childNames = new ArrayList();
			try {
				// get all childs from the test case type definition
				ITestPolicyCmptTypeParameter typeParam = testCase.findTestPolicyCmptTypeParameter(testPolicyCmpt);
				if (typeParam != null){
					ITestPolicyCmptTypeParameter[] children = typeParam.getTestPolicyCmptTypeParamChilds();
					for (int i = 0; i < children.length; i++) {
					    ITestPolicyCmptTypeParameter parameter = children[i];
						if (parameterMatchesRole(parameter)){
						    TestCaseTypeRelation dummyRelation = getDummyRelation(parameter, testPolicyCmpt);
						    childs.add(dummyRelation);
                        }
						childNames.add(parameter.getName());
					}
				}
				// add relations which are not added by the test case parameter
                //   relation with missing test case type parameter
				ITestPolicyCmptRelation[] relationsInTestCase = testPolicyCmpt.getTestPolicyCmptRelations();
				for (int i = 0; i < relationsInTestCase.length; i++) {
					ITestPolicyCmptRelation relation = relationsInTestCase[i];
					if (! childNames.contains(relation.getTestPolicyCmptTypeParameter())){
						childs.add(relation);
					}
				}
				return childs.toArray(new Object[0]);	
			} catch (CoreException e) {
				// ignore model error, the model consitence between the test case type and the test case
				// will be check when openening the editor, therefore it will be ignored  is here
				return EMPTY_ARRAY;
			}
		}
	}

    private HashMap dummyRelations = new HashMap();
    
    private TestCaseTypeRelation getDummyRelation(ITestPolicyCmptTypeParameter parameter, ITestPolicyCmpt testPolicyCmpt) {
        String id = parameter.getName() + "#" + testPolicyCmpt.getName();
        TestCaseTypeRelation dummyRelation = (TestCaseTypeRelation) dummyRelations.get(id);
        if (dummyRelation == null){
            dummyRelation = new TestCaseTypeRelation(parameter, testPolicyCmpt);
            dummyRelations.put(id, dummyRelation);
        }
        return dummyRelation;
    }

    /*
     * Returns <code>true</code> if the given paramter matches the current role which the content
     * provider provides.
     */
    private boolean parameterMatchesRole(ITestPolicyCmptTypeParameter parameter) {
        return (isExpectedResult() && parameter.isExpextedResultParameter())
                || (isInput() && parameter.isInputParameter());
    }

    /*
	 * Helper class to sort test policy component objecs.
	 */
	private static class TestPolicyCmptSorter implements Comparator{
		public int compare(Object o1, Object o2) {
			ITestPolicyCmpt testPolicyCmpt1 = (ITestPolicyCmpt) o1;
			ITestPolicyCmpt testPolicyCmpt2 = (ITestPolicyCmpt) o2;
			return testPolicyCmpt1.getProductCmpt().compareTo(testPolicyCmpt2.getProductCmpt());
		}
	}
	
	/*
	 * Helper class to sort test value objecs.
	 */
	private static class TestValueSorter implements Comparator{
		public int compare(Object o1, Object o2) {
			ITestValue testValue1 = (ITestValue) o1;
			ITestValue testValue2 = (ITestValue) o2;
			return testValue1.getTestValueParameter().compareTo(testValue2.getTestValueParameter());
		}
	}
}
