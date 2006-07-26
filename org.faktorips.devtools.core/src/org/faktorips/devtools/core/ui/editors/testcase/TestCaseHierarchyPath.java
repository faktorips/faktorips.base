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

import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;

/**
 * Class to evalulate and navigate a hierarchy path for test case or test case types.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseHierarchyPath{
	/** Seperator between each hierarchy element */
	private static final String separator = "|";
	
	/** Contains the complete hierarchy path */
	private String hierarchyPath = "";
	
	public TestCaseHierarchyPath(String hierarchyPath){
		this.hierarchyPath = hierarchyPath;
	}
	
	public TestCaseHierarchyPath(ITestPolicyCmpt currTestPolicyCmpt, boolean evalForTestCase){
		if (evalForTestCase){
			this.hierarchyPath = evalHierarchyPathForTestCase(currTestPolicyCmpt, "");
		}else{
			this.hierarchyPath = evalHierarchyPathForTestCaseType(currTestPolicyCmpt, "");
		}
	}
	
	public TestCaseHierarchyPath(ITestPolicyCmptRelation relation, boolean evalforTestCase){
		String relationPath = getHierarchyFromRelation(relation);
		if (evalforTestCase){	
			this.hierarchyPath = evalHierarchyPathForTestCase((ITestPolicyCmpt) relation.getParent(), relationPath);
		}else{
			this.hierarchyPath = evalHierarchyPathForTestCaseType((ITestPolicyCmpt) relation.getParent(), relationPath);
		}
	}
	
	/**
	 * Returns the hierarchy path.
	 */
	public String getHierarchyPath() {
		return hierarchyPath;
	}

	private String evalHierarchyPathForTestCaseType(ITestPolicyCmpt currTestPolicyCmpt, String hierarchyPath){
		while (!currTestPolicyCmpt.isRoot()){
			if (hierarchyPath.length()>0)
				hierarchyPath = separator + hierarchyPath ;
			ITestPolicyCmptRelation testPcTypeRelation = (ITestPolicyCmptRelation) currTestPolicyCmpt.getParent();
			hierarchyPath = getHierarchyFromRelation(testPcTypeRelation) + hierarchyPath;
			currTestPolicyCmpt = (ITestPolicyCmpt) testPcTypeRelation.getParent();
		}
		hierarchyPath = currTestPolicyCmpt.getTestPolicyCmptType() + (hierarchyPath.length() > 0 ? separator + hierarchyPath : "");
		return hierarchyPath;
	}
	
	private String evalHierarchyPathForTestCase(ITestPolicyCmpt currTestPolicyCmpt, String hierarchyPath){
		while (!currTestPolicyCmpt.isRoot()){
			if (hierarchyPath.length()>0)
				hierarchyPath = separator + hierarchyPath ;
			hierarchyPath = separator + currTestPolicyCmpt.getLabel() + hierarchyPath;
			ITestPolicyCmptRelation testPcTypeRelation = (ITestPolicyCmptRelation) currTestPolicyCmpt.getParent();
			hierarchyPath = getHierarchyFromRelation(testPcTypeRelation) + hierarchyPath;
			currTestPolicyCmpt = (ITestPolicyCmpt) testPcTypeRelation.getParent();
		}
		hierarchyPath = currTestPolicyCmpt.getLabel() + (hierarchyPath.length() > 0 ? separator + hierarchyPath : "");
		return hierarchyPath;
	}
	
	private String getHierarchyFromRelation(ITestPolicyCmptRelation relation){
		return relation.getTestPolicyCmptType();	
	}
	
	/**
	 * Returns <code>true</code> if there is a next path element.
	 */
	public boolean hasNext(){
		return hierarchyPath.length() > 0;
	}
			
	/**
	 * Returns the current path element and sets the navigation pointer one element forward.
	 */
	public String next(){
		String next = "";
		if (hierarchyPath.indexOf(separator)>=0){
			next = hierarchyPath.substring(0, hierarchyPath.indexOf(separator));
			hierarchyPath = hierarchyPath.substring(hierarchyPath.indexOf(separator) + 1);
			return next;
		}else{
			next = hierarchyPath;
			hierarchyPath = "";
		}
		return next;
	}
	
	public String toString(){
		return hierarchyPath;
	}
}

