/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TestEnumType {

    public static final TestEnumType FIRSTVALUE = new TestEnumType("1", "first");
    public static final TestEnumType SECONDVALUE = new TestEnumType("2", "second");
    public static final TestEnumType THIRDVALUE = new TestEnumType("3", "third");

    private static final List<TestEnumType> allValues;

    static {
        allValues = new ArrayList<>();
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

    public static final TestEnumType[] getAllValues() {
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

    public static final TestEnumType valueOf(String id) {

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

    public static String getIdByName(String name) {
        return Arrays.stream(getAllValues())
                .filter(v -> Objects.equals(v.getName(), name))
                .map(v -> v.getId())
                .findFirst()
                .orElse(null);
    }
}
