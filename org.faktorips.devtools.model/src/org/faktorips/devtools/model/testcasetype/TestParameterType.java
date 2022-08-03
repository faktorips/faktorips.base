/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.testcasetype;

public enum TestParameterType {

    INPUT("input", Messages.TestParameterType_Input, 0), //$NON-NLS-1$

    EXPECTED_RESULT("expectedResult", Messages.TestParameterType_ExpectedResult, 1), //$NON-NLS-1$

    COMBINED("combined", Messages.TestParameterType_Combined, 2); //$NON-NLS-1$

    private final Integer index;
    private final String id;
    private final String name;

    TestParameterType(String id, String name, int index) {
        this.id = id;
        this.name = name;
        this.index = Integer.valueOf(index);
    }

    public Integer getIndex() {
        return index;
    }

    /**
     * Returns the test parameter type by the given id or <code>UNKNOWN</code> if the id not
     * represent a test parameter type.
     */
    public static final TestParameterType getTestParameterType(String id) {
        for (TestParameterType type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Returns the test parameter type by the given index or <code>null</code> if an invalid index
     * is given.
     */
    public static final TestParameterType getTestParameterType(Integer index) {
        for (TestParameterType type : values()) {
            if (type.index.equals(index)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Special compare method to check if the Type matches each other.<br>
     * Returns <code>true</code> if both Type are "equals". Returns <code>true</code> if one Type is
     * combined and the other Type is either input or expected result. Otherwise returns
     * <code>false</code>.
     * 
     * @throws NullPointerException if one of the given Types are null.
     */
    public static boolean isTypeMatching(TestParameterType type1, TestParameterType type2) {
        if (type1.equals(type2) || (type1.equals(COMBINED) && (type2.equals(INPUT) || type2.equals(EXPECTED_RESULT)))) {
            return true;
        }

        if (type2.equals(COMBINED) && (type1.equals(INPUT) || type1.equals(EXPECTED_RESULT))) {
            return true;
        }

        return false;
    }

    /**
     * Special compare method to check if the type of a child matches the type of the parent.<br>
     * Returns <code>true</code> if both types are "equals". Returns <code>true</code> if the parent
     * type is combined and the other type is either input or expected result. Otherwise returns
     * <code>false</code>.
     * 
     * @throws NullPointerException if one of the given types are null.
     */
    public static boolean isChildTypeMatching(TestParameterType typeChild, TestParameterType typeParent) {
        if ((typeChild == typeParent)
                || (typeParent == COMBINED && (typeChild == INPUT || typeChild == EXPECTED_RESULT))) {
            return true;
        }

        return false;
    }

    /**
     * Returns the index of the given type inside the default enumeration.
     */
    public static int getIndexOfType(TestParameterType type) {
        return type.index;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return getName();
    }

}
