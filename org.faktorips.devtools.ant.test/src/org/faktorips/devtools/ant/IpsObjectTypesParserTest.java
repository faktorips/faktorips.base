/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.ant;

import static org.faktorips.devtools.ant.IpsObjectTypesParser.getIpsObjectTypes;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

public class IpsObjectTypesParserTest {

    @SuppressWarnings("deprecation")
    private static final IpsObjectType[] ALL_STANDARD_TYPES = new IpsObjectType[] { IpsObjectType.ENUM_CONTENT,
            IpsObjectType.ENUM_TYPE, IpsObjectType.POLICY_CMPT_TYPE,
            IpsObjectType.PRODUCT_CMPT, IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.PRODUCT_TEMPLATE,
            IpsObjectType.TABLE_CONTENTS, IpsObjectType.TABLE_STRUCTURE, IpsObjectType.TEST_CASE,
            IpsObjectType.TEST_CASE_TYPE };

    @Test
    public void testGetIpsObjectTypes_Null() {
        assertThat(getIpsObjectTypes(null, ALL_STANDARD_TYPES), containsAll(ALL_STANDARD_TYPES));
    }

    @Test
    public void testGetIpsObjectTypes_Empty() {
        assertThat(getIpsObjectTypes(" ", ALL_STANDARD_TYPES), containsAll(ALL_STANDARD_TYPES));
    }

    @Test
    public void testGetIpsObjectTypes_All() {
        assertThat(getIpsObjectTypes("all", ALL_STANDARD_TYPES), containsAll(ALL_STANDARD_TYPES));
    }

    @Test
    public void testGetIpsObjectTypes_NotAll() {
        assertThat(getIpsObjectTypes("!all", ALL_STANDARD_TYPES), containsAll());
    }

    @Test
    public void testGetIpsObjectTypes_Model() {
        assertThat(getIpsObjectTypes("MODEL", ALL_STANDARD_TYPES), containsAll(modelTypes()));
    }

    @Test
    public void testGetIpsObjectTypes_AllNotProduct() {
        assertThat(getIpsObjectTypes("All,!Product", ALL_STANDARD_TYPES), containsAll(modelTypes()));
    }

    @Test
    public void testGetIpsObjectTypes_Includes() {
        assertThat(getIpsObjectTypes("EnumContent, EnumType", ALL_STANDARD_TYPES),
                containsAll(IpsObjectType.ENUM_CONTENT, IpsObjectType.ENUM_TYPE));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetIpsObjectTypes_InAndExcludes() {
        assertThat(getIpsObjectTypes("Model, !EnumType, EnumContent", ALL_STANDARD_TYPES),
                containsAll(IpsObjectType.ENUM_CONTENT, IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE,
                        IpsObjectType.TABLE_STRUCTURE, IpsObjectType.TEST_CASE_TYPE));
    }

    private IpsObjectType[] modelTypes() {
        Set<IpsObjectType> modelTypes = new HashSet<>();
        for (IpsObjectType ipsObjectType : ALL_STANDARD_TYPES) {
            if (!ipsObjectType.isProductDefinitionType()) {
                modelTypes.add(ipsObjectType);
            }
        }
        return modelTypes.toArray(new IpsObjectType[0]);
    }

    private TypeSafeMatcher<? super IpsObjectType[]> containsAll(final IpsObjectType... ipsObjectTypes) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(IpsObjectType[] ipsObjectTypes2) {
                if (ipsObjectTypes2.length != ipsObjectTypes.length) {
                    return false;
                }

                List<IpsObjectType> ipsObjectTypes2AsList = Arrays.asList(ipsObjectTypes2);

                for (IpsObjectType val : ipsObjectTypes) {
                    if (!ipsObjectTypes2AsList.contains(val)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(ipsObjectTypes);
            }
        };
    }
}
