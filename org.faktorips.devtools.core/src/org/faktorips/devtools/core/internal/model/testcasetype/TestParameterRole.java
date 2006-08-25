/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.testcasetype;

import java.util.HashMap;

/**
 * Enumeration class to represent the test parameter role.
 * 
 * @author Joerg Ortmann
 */
public class TestParameterRole {
    public static final TestParameterRole INPUT = new TestParameterRole("input");
    public static final TestParameterRole EXPECTED_RESULT = new TestParameterRole("expectedResult");
    public static final TestParameterRole COMBINED = new TestParameterRole("combined");
    public static final TestParameterRole UNKNOWN = new TestParameterRole("unknown");
    
    private static HashMap roles;
    
    private final String roleName;

    private TestParameterRole(String roleName) {
        this.roleName = roleName;
        if (roles == null)
        	roles = new HashMap();
        
        roles.put(roleName, this);
    }

    /**
     * Returns the string representation of the test parameter role.
     */
    public String toString() {
        return roleName;
    }
    
    /**
     * Returns the corresponding test parameter role object.
     * If the given role name doesn't matches an existing role return <code>null</code>.
     */
    public static TestParameterRole getTestParameterRole(String role){
    	TestParameterRole roleFound = (TestParameterRole) roles.get(role);
    	if (roleFound == null)
    		return TestParameterRole.UNKNOWN;
    	else
    		return roleFound;
    }
    
    /**
     * Special compare method to check the correct role interpretation.
     * Returns <code>true</code> if both roles are "equals".
     * Returns <code>true</code> if one role is combined and the other role is either input or expected result.
     * Otherwise returns <code>false</code>.
     * Throws a NullPointerException if one of the given roles are null.
     */
    public static boolean isRoleMatching(TestParameterRole role1, TestParameterRole role2){
        if (role1.equals(COMBINED) && (role2.equals(INPUT) || role2.equals(EXPECTED_RESULT)))
            return true;

        if (role2.equals(COMBINED) && (role1.equals(INPUT) || role1.equals(EXPECTED_RESULT)))
            return true;
        
        return role1.equals(role2);
    }
}
