/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.faktorips.devtools.model.internal.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.model.internal.type.Attribute;
import org.faktorips.devtools.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomValidationsMapTest {
    private ICustomValidation<Attribute> attributeValidation = new ValidationOnAttribute("AttributeValidation");
    private ICustomValidation<PolicyCmptTypeAttribute> pcTypeAttributeValidation = new ValidationOnPolicyCmptType(
            "PolicyCmptTypeAttributeValidation");

    private CustomValidationsMap validationsMap = new CustomValidationsMap();

    @Test
    public void testPut_withExtendingClass() {
        validationsMap.put(attributeValidation);

        assertFalse(validationsMap.get(Attribute.class).isEmpty());
    }

    @Test
    public void testPut_noDuplicateElements() {
        validationsMap.put(attributeValidation);
        validationsMap.put(attributeValidation);
        validationsMap.put(attributeValidation);

        assertEquals(1, validationsMap.get(Attribute.class).size());
    }

    @Test(expected = RuntimeException.class)
    public void testPut_wrongType() {
        validationsMap.put(Attribute.class, pcTypeAttributeValidation);
    }

    @Test
    public void testGetKeys() {
        validationsMap.put(attributeValidation);
        validationsMap.put(pcTypeAttributeValidation);

        Set<Class<? extends IIpsObjectPartContainer>> keys = validationsMap.getKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains(Attribute.class));
        assertTrue(keys.contains(PolicyCmptTypeAttribute.class));
    }

    @Test
    public void testClear() {
        validationsMap.put(attributeValidation);
        validationsMap.put(attributeValidation);
        validationsMap.put(attributeValidation);
        assertFalse(validationsMap.get(Attribute.class).isEmpty());

        validationsMap.clear();

        assertFalse(validationsMap.containsValidationsFor(Attribute.class));
    }

    @Test
    public void testContainsValidationsFor() {
        validationsMap.put(attributeValidation);
        validationsMap.put(pcTypeAttributeValidation);

        assertTrue(validationsMap.containsValidationsFor(Attribute.class));
        assertTrue(validationsMap.containsValidationsFor(PolicyCmptTypeAttribute.class));
    }

    @Test(expected = RuntimeException.class)
    public void testPutAll_wrongType() {
        Collection<ICustomValidation<?>> validationsToPut = new ArrayList<>();
        validationsToPut.add(attributeValidation);
        validationsToPut.add(pcTypeAttributeValidation);

        validationsMap.putAll(Attribute.class, validationsToPut);
    }

    @Test
    public void testPutAll_storedUnderDifferentKey() {
        Collection<ICustomValidation<?>> validationsToPut = new ArrayList<>();
        validationsToPut.add(attributeValidation);
        validationsToPut.add(pcTypeAttributeValidation);

        validationsMap.putAll(PolicyCmptTypeAttribute.class, validationsToPut);

        assertTrue(validationsMap.containsValidationsFor(PolicyCmptTypeAttribute.class));
        assertFalse(validationsMap.containsValidationsFor(Attribute.class));
    }

    @Test
    public void testOrder() {
        validationsMap = new CustomValidationsMap();
        ICustomValidation<Attribute> cv1 = new ValidationOnAttribute("cv1");
        ICustomValidation<PolicyCmptTypeAttribute> cv2 = new ValidationOnPolicyCmptType("cv2");
        ICustomValidation<Attribute> cv3 = new ValidationOnAttribute("cv3");
        ICustomValidation<PolicyCmptTypeAttribute> cv4 = new ValidationOnPolicyCmptType("cv4");
        ICustomValidation<Attribute> cv5 = new ValidationOnAttribute("cv5");
        ICustomValidation<PolicyCmptTypeAttribute> cv6 = new ValidationOnPolicyCmptType("cv6");
        ICustomValidation<Attribute> cv7 = new ValidationOnAttribute("cv7");
        ICustomValidation<PolicyCmptTypeAttribute> cv8 = new ValidationOnPolicyCmptType("cv8");

        validationsMap.put(cv1);
        validationsMap.put(cv2);
        validationsMap.put(cv3);
        validationsMap.put(cv4);
        validationsMap.put(cv5);
        validationsMap.put(cv6);
        validationsMap.put(cv7);
        validationsMap.put(cv8);

        assertThat(validationsMap.get(Attribute.class), Is.is(sameOrder(cv1, cv3, cv5, cv7)));
        assertThat(validationsMap.get(PolicyCmptTypeAttribute.class), Is.is(sameOrder(cv2, cv4, cv6, cv8)));
    }

    @SafeVarargs
    static Matcher<Set<ICustomValidation<?>>> sameOrder(
            ICustomValidation<?>... values) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("matching this order: ["
                        + Arrays.stream(values).map(ICustomValidation::toString).collect(Collectors.joining(", "))
                        + "]");
            }

            @Override
            protected boolean matchesSafely(Set<ICustomValidation<?>> customValidation) {
                if (customValidation.size() != values.length) {
                    return false;
                }
                int i = 0;
                for (ICustomValidation<?> validation : customValidation) {
                    if (!values[i].equals(validation)) {
                        return false;
                    }
                    i++;
                }
                return true;
            }
        };
    }

    class ValidationOnAttribute implements ICustomValidation<Attribute> {

        private String name;

        public ValidationOnAttribute(String name) {
            this.name = name;
        }

        @Override
        public Class<Attribute> getExtendedClass() {
            return Attribute.class;
        }

        @Override
        public MessageList validate(Attribute objectToValidate, IIpsProject ipsProject) {
            return null;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    class ValidationOnPolicyCmptType implements ICustomValidation<PolicyCmptTypeAttribute> {

        private String name;

        public ValidationOnPolicyCmptType(String name) {
            this.name = name;
        }

        @Override
        public Class<PolicyCmptTypeAttribute> getExtendedClass() {
            return PolicyCmptTypeAttribute.class;
        }

        @Override
        public MessageList validate(PolicyCmptTypeAttribute objectToValidate, IIpsProject ipsProject) {
            return null;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
