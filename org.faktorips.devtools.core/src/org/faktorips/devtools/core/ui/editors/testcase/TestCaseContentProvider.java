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
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
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
public class TestCaseContentProvider  implements ITreeContentProvider {
	private static Object[] EMPTY_ARRAY = new Object[0];
	
	/** Defines the type of the content - from the test case - which will be provided: 
	 *    the input objects or the expected result objects could be provided */
	public static final int TYPE_INPUT = 0;
	public static final int TYPE_EXPECTED_RESULT = 1;
	private int isTypeFor = -1;
	
	/** Contains the test case for which the content will be provided */
	private ITestCase testCase;
	
	/** Indicates if the structure should be displayed without relation layer */
	private boolean withoutRelations = false;
	
	public TestCaseContentProvider(int type, ITestCase testCase){
		ArgumentCheck.notNull(testCase);
		this.isTypeFor = type;
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
	 * Returns the corresponding test policy componet objects.<br>
	 * Input or expected result objects.<br>
	 * Rerurns <code>null</code> if this content provider has an unkown type.
	 */
	public ITestPolicyCmpt[] getPolicyCmpt(){
		if (isInputType()){
			return testCase.getInputPolicyCmpt();
		}else if (isExpectedResultTypes()){
			return testCase.getExpectedResultPolicyCmpt();
		}
		return null;
	}
	
	/**
	 * Returns the corresponding test value objects.<br>
	 * Input or expected result objects.<br>
	 * Rerurns <code>null</code> if this content provider has an unkown type.
	 */
	public ITestValue[] getValues(){
		if (isInputType()){
			return testCase.getInputValue();
		}else if (isExpectedResultTypes()){
			return testCase.getExpectedResultValue();
		}
		return null;
	}
	
	/**
	 * Returns <code>true</code> if this content provider provides the input objetcs of the test case.
	 */
	public boolean isInputType(){
		return isTypeFor == TYPE_INPUT;
	}
	
	/**
	 * Returns <code>true</code> if this content provider provides the expected result objects of the test case.
	 */	
	public boolean isExpectedResultTypes(){
		return isTypeFor == TYPE_EXPECTED_RESULT;
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
			ArrayList params = new ArrayList();
			if (isInputType()){
				// return input objects
				//   return all policy component objetcs
				ITestPolicyCmpt[] inputPolicyCmpts = testCase.getInputPolicyCmpt();
				for (int i = 0; i < inputPolicyCmpts.length; i++) {
					ITestPolicyCmpt cmpt = inputPolicyCmpts[i];
					params.add(cmpt);
				}
				//	return all test value objetcs
				ITestValue[] inputTestValues = testCase.getInputValue();
				for (int i = 0; i < inputTestValues.length; i++) {
					ITestValue value = inputTestValues[i];
					params.add(value);
				}
				return params.toArray();
			}else if(isExpectedResultTypes()){
				// return expected result objects
				//   return all policy component objetcs
				ITestPolicyCmpt[] expectedResultPolicyCmpts = testCase.getExpectedResultPolicyCmpt();
				for (int i = 0; i < expectedResultPolicyCmpts.length; i++) {
					ITestPolicyCmpt cmpt = expectedResultPolicyCmpts[i];
					params.add(cmpt);
				}
				//   return all test value objetcs
				ITestValue[] expectedResulTestValues = testCase.getExpectedResultValue();
				for (int i = 0; i < expectedResulTestValues.length; i++) {
					ITestValue value = expectedResulTestValues[i];
					params.add(value);
				}
				return params.toArray();
			}
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
	}

	/**
	 * Returns all child of the given test case type relation parameter 
	 * (dummy relation based on the test case type definition)
	 */
	private Object[] getChildsForTestCaseTypeRelation(TestCaseTypeRelation dummyRelation) {
		// show instances of this test policy component type parameter
		ArrayList childs = new ArrayList();
		
		ITestPolicyCmpt parent = dummyRelation.getParentTestPolicyCmpt();
		if (parent != null){
			ITestPolicyCmptRelation[] relations = parent.getTestPcTypeRelations(dummyRelation.getName());
			for (int i = 0; i < relations.length; i++) {
				ITestPolicyCmptRelation relation = relations[i];
				if (relation.isComposition()){
					try {
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

	/**
	 * Returns all child of the given test case relation.
	 */
	private Object[] getChildsForTestPolicyCmptRelation(ITestPolicyCmptRelation testPcRelation) {
		if (testPcRelation.isAccociation()){
			return EMPTY_ARRAY;
		}else{
			// TODO Joerg: need grouping functionality if the test case type param not exists?
			ITestPolicyCmpt[] childs = new ITestPolicyCmpt[1];
			try {
				childs[0] = testPcRelation.findTarget();
			} catch (CoreException e) {
				return EMPTY_ARRAY;
			}
			return childs;
		}
	}

	/**
	 * Returns childs of the test policy component.
	 */
	private Object[] getChildsForTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) {
		ITestPolicyCmptRelation[] relations = testPolicyCmpt.getTestPcTypeRelations();
		if (withoutRelations){
			IIpsElement[] childs = new ITestPolicyCmpt[relations.length];
			for (int i = 0; i < relations.length; i++) {
				ITestPolicyCmptRelation relation = relations[i];
				if (relation.isComposition()){
					ITestPolicyCmpt target=null;
					try {
						target = relation.findTarget();
					} catch (CoreException e) {
						IpsPlugin.logAndShowErrorDialog(e);
					}
					childs[i] = target;
				}else{
					childs[i] = relations[i];
				}
			}
			Arrays.sort(childs, TESTPOLICYCMPT_SORTER);
			return childs;
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
						TestCaseTypeRelation dummyRelation = new TestCaseTypeRelation(parameter);
						dummyRelation.setTestPolicyCmpt(testPolicyCmpt);
						childs.add(dummyRelation);
						childNames.add(dummyRelation.getName());
					}
				}
				// add relations which are not added by the test case parameter
				ITestPolicyCmptRelation[] relationsInTestCase = testPolicyCmpt.getTestPcTypeRelations();
				for (int i = 0; i < relationsInTestCase.length; i++) {
					ITestPolicyCmptRelation relation = relationsInTestCase[i];
					if (! childNames.contains(relation.getTestPolicyCmptType())){
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
	
	//
	// Helper classes
	//
	
	private static TestPolicyCmptSorter TESTPOLICYCMPT_SORTER = new TestPolicyCmptSorter();
	private static TestValueSorter TESTVALUE_SORTER = new TestValueSorter();

	/**
	 * Helper class to sort test policy component objecs.
	 */
	private static class TestPolicyCmptSorter implements Comparator{
		public int compare(Object o1, Object o2) {
			ITestPolicyCmpt testPolicyCmpt1 = (ITestPolicyCmpt) o1;
			ITestPolicyCmpt testPolicyCmpt2 = (ITestPolicyCmpt) o2;
			return testPolicyCmpt1.getProductCmpt().compareTo(testPolicyCmpt2.getProductCmpt());
		}
	}
	
	/**
	 * Helper class to sort test value objecs.
	 */
	private static class TestValueSorter implements Comparator{
		public int compare(Object o1, Object o2) {
			ITestValue testValue1 = (ITestValue) o1;
			ITestValue testValue2 = (ITestValue) o2;
			return testValue1.getTestValueParameter().compareTo(testValue2.getTestValueParameter());
		}
	}
	
	/**
	 * Returns all value objects which are provided by this provider in sorted order.
	 */
	public ITestValue[] getSortedValues() {
		ITestValue[] testValues = null;
		if (isInputType()){
			testValues = testCase.getInputValue();
		}else if (isExpectedResultTypes()){
			testValues = testCase.getExpectedResultValue();
		}else{
			return new ITestValue[0];
		}
		Arrays.sort(testValues, TESTVALUE_SORTER);
		return testValues;
	}

	/**
	 * Returns all test policy component objects which are provided by this provider in sorted order.
	 */
	public ITestPolicyCmpt[] getSortedPolicyCmpts() {
		ITestPolicyCmpt[] testPolicyCmpt = null;
		if (isInputType()){
			testPolicyCmpt = testCase.getInputPolicyCmpt();
		}else if (isExpectedResultTypes()){
			testPolicyCmpt = testCase.getExpectedResultPolicyCmpt();
		}else{
			return new ITestPolicyCmpt[0];
		}
		Arrays.sort(testPolicyCmpt, TESTPOLICYCMPT_SORTER);
		return testPolicyCmpt;
	}

	/**
	 * Generate and set a unique label for the given test policy component.
	 */
	public void generateUniqueLabelOfTestPolicyCmpt(ITestPolicyCmpt newTestPolicyCmpt) {
		String uniqueLabel = "";
		if (StringUtils.isEmpty(newTestPolicyCmpt.getProductCmpt())){
			uniqueLabel = newTestPolicyCmpt.getTestPolicyCmptType();
		}else{
			uniqueLabel = newTestPolicyCmpt.getProductCmpt();
		}
		// eval the unique idx of new component
		int idx = 1;
		String newUniqueLabel = uniqueLabel;
		if (newTestPolicyCmpt.isRoot()){
			ITestPolicyCmpt[] testPolicyCmpts = testCase.getInputPolicyCmpt();
			for (int i = 0; i < testPolicyCmpts.length; i++) {
				ITestPolicyCmpt cmpt = testPolicyCmpts[i];
				if (newUniqueLabel.equals(cmpt.getLabel())){
					idx ++;
					newUniqueLabel = uniqueLabel + " (" + idx + ")";
				}
			}
		}else{
			ITestPolicyCmpt parent = newTestPolicyCmpt.getParentPolicyCmpt();
			ITestPolicyCmptRelation[] relations = parent.getTestPcTypeRelations();
			ArrayList names = new ArrayList();
			for (int i = 0; i < relations.length; i++) {
				ITestPolicyCmptRelation relation = relations[i];
				if (relation.isComposition()){
					try {
						ITestPolicyCmpt child = relation.findTarget();
						names.add(child.getLabel());
					} catch (CoreException e) {
						IpsPlugin.logAndShowErrorDialog(e);
					}
				}
			}
			while (names.contains(newUniqueLabel)){
				idx ++;
				newUniqueLabel = uniqueLabel + " (" + idx + ")";
			}
		}
		newTestPolicyCmpt.setLabel(newUniqueLabel);
	}
}
