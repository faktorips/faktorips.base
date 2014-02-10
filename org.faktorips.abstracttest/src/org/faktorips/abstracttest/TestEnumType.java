/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import java.util.ArrayList;
import java.util.List;

public class TestEnumType {

    public final static TestEnumType FIRSTVALUE = new TestEnumType("1", "first");
    public final static TestEnumType SECONDVALUE = new TestEnumType("2", "second");
    public final static TestEnumType THIRDVALUE = new TestEnumType("3", "third");

    private final static List<TestEnumType> allValues;

    static {
        allValues = new ArrayList<TestEnumType>();
        allValues.add(FIRSTVALUE);
        allValues.add(SECONDVALUE);
        allValues.add(THIRDVALUE);
    }

    private String id;
    private String name;

    public TestEnumType(String id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public final static TestEnumType[] getAllValues() {
        return allValues.toArray(new TestEnumType[allValues.size()]);
    }

    public boolean isValueOf(String id) {
        try {
            valueOf(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public final static TestEnumType valueOf(String id) {

        TestEnumType[] allValues = getAllValues();
        for (TestEnumType allValue : allValues) {
            if (allValue.id.equals(id)) {
                return allValue;
            }
        }
        throw new IllegalArgumentException("Not a valid id for this enum type " + id);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getId();
    }
}
