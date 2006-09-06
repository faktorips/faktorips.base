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

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;
import org.faktorips.values.EnumValue;

public class TestParameterType extends DefaultEnumValue {
    
    public final static TestParameterType INPUT;
    
    public final static TestParameterType EXPECTED_RESULT;

    public final static TestParameterType COMBINED;
    
    private final static DefaultEnumType enumType; 

    public final static TestParameterType UNKNOWN;
    
    private final static DefaultEnumType nonDefaultEnumType; 
    
    private Integer index;
    
    static {
        enumType = new DefaultEnumType("TestParameterTypeValue", TestParameterType.class); //$NON-NLS-1$
        INPUT = new TestParameterType(enumType, "input", Messages.TestParameterType_Input, 0); //$NON-NLS-1$
        EXPECTED_RESULT = new TestParameterType(enumType, "expectedResult", Messages.TestParameterType_ExpectedResult, 1); //$NON-NLS-1$
        COMBINED = new TestParameterType(enumType, "combined", Messages.TestParameterType_Combined, 2); //$NON-NLS-1$
        
        nonDefaultEnumType = new DefaultEnumType("TestParameterTypeValueUnknown", TestParameterType.class); //$NON-NLS-1$
        UNKNOWN = new TestParameterType(nonDefaultEnumType, "unknown", Messages.TestParameterType_Unknown, Integer.MAX_VALUE); //$NON-NLS-1$
    }

    public TestParameterType(DefaultEnumType type, String id, String name, int index){
        super(type, id, name);
        this.index = new Integer(index);
    }
    
    public Integer getIndex(){
        return index;
    }
    
    /**
     * Returns the enum type which contains all test parameter roles.
     */
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    /**
     * Returns the test parameter type by the given id or <code>null</code> if the id not represent a
     * test parameter type.
     */
    public final static TestParameterType getTestParameterType(String id) {
        return (TestParameterType) enumType.getEnumValue(id);
    }
    
    /**
     * Returns the test parameter type by the given index or <code>null</code> if an invalid index is given.
     */
    public final static TestParameterType getTestParameterType(Integer index) {
        EnumValue[] types = enumType.getValues();
        if (index.intValue()>=types.length)
            return null;
        return (TestParameterType) types[index.intValue()];
    }
    
    /**
     * Returns the unknown test parameter type.
     */
    public final static TestParameterType getUnknownTestParameterType() {
        return (TestParameterType) nonDefaultEnumType.getEnumValue(0);
    }    
    
    /**
     * Special compare method to check if the Type matches each other.<br>
     * Returns <code>true</code> if both Type are "equals".
     * Returns <code>true</code> if one Type is combined and the other Type is either input or expected result.
     * Otherwise returns <code>false</code>.
     * 
     * @throws NullPointerException if one of the given Type are null.
     */
    public static boolean isRoleMatching(TestParameterType type1, TestParameterType type2){
        if (type1.equals(type2))
            return true;
        
        if (type2.equals(COMBINED) && (type2.equals(INPUT) || type2.equals(EXPECTED_RESULT)))
            return true;

        if (type2.equals(COMBINED) && (type1.equals(INPUT) || type1.equals(EXPECTED_RESULT)))
            return true;
        
        return false;
    }

    /**
     * Special compare method to check if the type of a child matches the type of the parent.<br>
     * Returns <code>true</code> if both types are "equals".
     * Returns <code>true</code> if the parent type is combined and the other type is either input or expected result.
     * Otherwise returns <code>false</code>.
     * 
     * @throws NullPointerException if one of the given roles are null.
     */
    public static boolean isChildTypeMatching(TestParameterType typeChild, TestParameterType typeParent) {
        if (typeChild.equals(typeParent))
            return true;

        if (typeParent.equals(COMBINED) && (typeChild.equals(INPUT) || typeChild.equals(EXPECTED_RESULT)))
            return true;
        
        return false;
    }
    
    /**
     * Returns the image.
     */
    public Image getImage() {
        if (this == EXPECTED_RESULT) {
            return IpsPlugin.getDefault().getImage("TestCaseExpResult.gif"); //$NON-NLS-1$
        } else {
            return IpsPlugin.getDefault().getImage("TestCaseInput.gif"); //$NON-NLS-1$
        }
    }    
}
