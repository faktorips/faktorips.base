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

package org.faktorips.devtools.core.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 * Specification of a test policy component relation.
 * 
 * @author Joerg Ortmann
 */
public interface ITestPolicyCmptRelation extends IIpsObjectPart {
	
	/** Property names */
	public final static String PROPERTY_POLICYCMPTTYPE = "testPolicyCmptType"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    
    /**
     * Returns the qualified name of policy component class.
     */
	public String getTestPolicyCmptType();
	
    /**
     * Sets the qualified name of the policy component class.
     */	
	public void setTestPolicyCmptType(String pcType);
	
    /**
     * Returns the test policy component type or <code>null</code> if the policy component type does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the policy component type.
     */	
	public ITestPolicyCmptTypeParameter findTestPolicyCmptType() throws CoreException;
	
	/**
	 * Returns the target.
	 */
	public String getTarget();
	
	/**
	 * Sets the target.
	 */
	public void setTarget(String target);

    /**
     * Returns the target or <code>null</code> if the targte does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */		
	public ITestPolicyCmpt findTarget() throws CoreException;
	
    /**
     * Creates a new test policy component as child and returns it.
     */
	public ITestPolicyCmpt newTargetTestPolicyCmptChild();	
	
	/**
	 * Returns <code>true</code> if the relation is an accociation.
	 */
	public boolean isAccociation();
	
	/**
	 * Returns <code>true</code> if the relation is a composition.
	 */
	public boolean isComposition();
	
	/**
	 * Returns the test case this relation belongs to.
	 */
	public ITestCase getTestCase();
	
	/**
	 * Set <code>true</code> if this object is transient, changed on this object will not update the source file.
	 */
	public void setTransient(boolean isTransient);
}
