/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.internal.model.type.Attribute;
import org.faktorips.devtools.core.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomValidationsMapTest {
    private ICustomValidation<Attribute> attributeValidation = new ValidationOnAttribute();
    private ICustomValidation<PolicyCmptTypeAttribute> pcTypeAttributeValidation = new ValidationOnPolicyCmptType();

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
        Collection<ICustomValidation<?>> validationsToPut = new ArrayList<ICustomValidation<?>>();
        validationsToPut.add(attributeValidation);
        validationsToPut.add(pcTypeAttributeValidation);

        validationsMap.putAll(Attribute.class, validationsToPut);
    }

    @Test
    public void testPutAll_storedUnderDifferentKey() {
        Collection<ICustomValidation<?>> validationsToPut = new ArrayList<ICustomValidation<?>>();
        validationsToPut.add(attributeValidation);
        validationsToPut.add(pcTypeAttributeValidation);

        validationsMap.putAll(PolicyCmptTypeAttribute.class, validationsToPut);

        assertTrue(validationsMap.containsValidationsFor(PolicyCmptTypeAttribute.class));
        assertFalse(validationsMap.containsValidationsFor(Attribute.class));
    }

    class ValidationOnAttribute implements ICustomValidation<Attribute> {

        @Override
        public Class<Attribute> getExtendedClass() {
            return Attribute.class;
        }

        @Override
        public MessageList validate(Attribute objectToValidate, IIpsProject ipsProject) throws CoreException {
            return null;
        }

    }

    class ValidationOnPolicyCmptType implements ICustomValidation<PolicyCmptTypeAttribute> {

        @Override
        public Class<PolicyCmptTypeAttribute> getExtendedClass() {
            return PolicyCmptTypeAttribute.class;
        }

        @Override
        public MessageList validate(PolicyCmptTypeAttribute objectToValidate, IIpsProject ipsProject)
                throws CoreException {
            return null;
        }

    }

}
