/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import java.io.Serializable;
import java.util.List;

import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsExtensibleEnum;
import org.faktorips.values.ListUtil;

@IpsEnumType(name = "TestExtensibleEnum", attributeNames = { "id", "name" })
@IpsExtensibleEnum(enumContentName = "TestExtensibleEnum")
public final class TestExtensibleEnum implements Serializable, Comparable<TestExtensibleEnum> {

    public static final TestExtensibleEnum ENUM1 = new TestExtensibleEnum(0, "a1ID", "a1Name");
    public static final TestExtensibleEnum ENUM2 = new TestExtensibleEnum(1, "a2ID", "a2Name");
    public static final List<TestExtensibleEnum> VALUES = ListUtil.unmodifiableList(ENUM1, ENUM2);

    private static final long serialVersionUID = 1L;

    private final int index;
    private final String id;
    private final String name;

    public TestExtensibleEnum(int index, String id, String name) {
        this.index = index;
        this.id = id;
        this.name = name;
    }

    @IpsEnumAttribute(name = "id", identifier = true, unique = true)
    public String getId() {
        return id;
    }

    @IpsEnumAttribute(name = "name", unique = true, displayName = true)
    public String getName() {
        return name;
    }

    @Override
    public int compareTo(TestExtensibleEnum o) {
        return index - o.index;
    }
}