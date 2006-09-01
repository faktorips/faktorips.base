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

package org.faktorips.devtools.core.model.testcasetype;

import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

public class TestParameterRole extends DefaultEnumValue {
    
    public final static TestParameterRole INPUT;
    
    public final static TestParameterRole EXPECTED_RESULT;

    public final static TestParameterRole COMBINED;
    
    private final static DefaultEnumType enumType; 

    public final static TestParameterRole UNKNOWN;
    
    private final static DefaultEnumType nonDefaultEnumType; 
    
    static {
        enumType = new DefaultEnumType("TestParameterRoleValue", TestParameterRole.class); //$NON-NLS-1$
        INPUT = new TestParameterRole(enumType, "input", Messages.TestParameterRole_Input); //$NON-NLS-1$
        EXPECTED_RESULT = new TestParameterRole(enumType, "expectedResult", Messages.TestParameterRole_ExpectedResult); //$NON-NLS-1$
        COMBINED = new TestParameterRole(enumType, "combined", Messages.TestParameterRole_Combined); //$NON-NLS-1$
        
        nonDefaultEnumType = new DefaultEnumType("TestParameterRoleValueUnknown", TestParameterRole.class); //$NON-NLS-1$
        UNKNOWN = new TestParameterRole(nonDefaultEnumType, "unknown", Messages.TestParameterRole_Unknown); //$NON-NLS-1$
    }

    /**
     * Returns the enum type which contains all test parameter roles.
     */
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    /**
     * Returns the test parameter role by the given id or <code>null</code> if the id not represent a
     * test parameter role.
     */
    public final static TestParameterRole getTestParameterRole(String id) {
        return (TestParameterRole) enumType.getEnumValue(id);
    }
    
    /**
     * Returns the unknown test parameter role.
     */
    public final static TestParameterRole getUnknownTestParameterRole() {
        return (TestParameterRole) nonDefaultEnumType.getEnumValue(0);
    }    
    
    private TestParameterRole(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }
    
    /**
     * Special compare method to check if the role matches each other.<br>
     * Returns <code>true</code> if both roles are "equals".
     * Returns <code>true</code> if one role is combined and the other role is either input or expected result.
     * Otherwise returns <code>false</code>.
     * 
     * @throws NullPointerException if one of the given roles are null.
     */
    public static boolean isRoleMatching(TestParameterRole role1, TestParameterRole role2){
        if (role1.equals(role2))
            return true;
        
        if (role1.equals(COMBINED) && (role2.equals(INPUT) || role2.equals(EXPECTED_RESULT)))
            return true;

        if (role2.equals(COMBINED) && (role1.equals(INPUT) || role1.equals(EXPECTED_RESULT)))
            return true;
        
        return false;
    }

    /**
     * Special compare method to check if the role of a child matches the role of the parent.<br>
     * Returns <code>true</code> if both roles are "equals".
     * Returns <code>true</code> if the parent role is combined and the other role is either input or expected result.
     * Otherwise returns <code>false</code>.
     * 
     * @throws NullPointerException if one of the given roles are null.
     */
    public static boolean isChildRoleMatching(TestParameterRole roleChild, TestParameterRole roleParent) {
        if (roleChild.equals(roleParent))
            return true;

        if (roleParent.equals(COMBINED) && (roleChild.equals(INPUT) || roleChild.equals(EXPECTED_RESULT)))
            return true;
        
        return false;
    }   
}
