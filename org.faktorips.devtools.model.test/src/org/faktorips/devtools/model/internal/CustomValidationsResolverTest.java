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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.internal.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.model.internal.type.Attribute;
import org.faktorips.devtools.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class CustomValidationsResolverTest {

    private List<ICustomValidation<?>> allValidations = new ArrayList<>();
    private ValidationOnAttribute validationOnAttribute = new ValidationOnAttribute();
    private ValidationOnIAttribute validationOnIAttribute = new ValidationOnIAttribute();
    private ValidationOnPolicyCmptType validationOnPolicyCmptType = new ValidationOnPolicyCmptType();
    private ValidationOnIPolicyCmptType validationOnIPolicyCmptType = new ValidationOnIPolicyCmptType();

    @Before
    public void setUp() {
        allValidations = new ArrayList<>();
        allValidations.add(validationOnIAttribute);
        allValidations.add(validationOnPolicyCmptType);
        allValidations.add(validationOnAttribute);
        allValidations.add(validationOnIPolicyCmptType);
    }

    @Test
    public void testGetCustomValidations_should_ReturnValidationsRegisteredForTheClassOrInterfaceItself() {
        CustomValidationsResolver validationsPerType = CustomValidationsResolver.createFromList(allValidations);
        Set<ICustomValidation<?>> validations = validationsPerType.getCustomValidations(IAttribute.class);
        assertEquals(1, validations.size());
        assertTrue(validations.contains(validationOnIAttribute));
    }

    @Test
    public void testGetCustomValidations_should_ReturnValidationsRegisteredForAnImplementedInterface() {
        CustomValidationsResolver validationsPerType = CustomValidationsResolver.createFromList(allValidations);
        Set<ICustomValidation<?>> validations = validationsPerType.getCustomValidations(Attribute.class);
        assertTrue(validations.contains(validationOnIAttribute));
    }

    @Test
    public void testGetCustomValidations_should_ReturnValidationsRegisteredForSameClass() {
        CustomValidationsResolver validationsPerType = CustomValidationsResolver.createFromList(allValidations);
        Set<ICustomValidation<?>> validations = validationsPerType.getCustomValidations(PolicyCmptTypeAttribute.class);
        assertEquals(4, validations.size());
        assertTrue(validations.contains(validationOnAttribute));
        assertTrue(validations.contains(validationOnIAttribute));
        assertTrue(validations.contains(validationOnPolicyCmptType));
        assertTrue(validations.contains(validationOnIPolicyCmptType));
    }

    @Test
    public void testGetCustomValidations_should_ReturnValidationsRegisteredForInteface() {
        CustomValidationsResolver validationsPerType = CustomValidationsResolver.createFromList(allValidations);
        Set<ICustomValidation<?>> validations = validationsPerType.getCustomValidations(IPolicyCmptTypeAttribute.class);
        assertEquals(2, validations.size());
        assertTrue(validations.contains(validationOnIAttribute));
        assertTrue(validations.contains(validationOnIPolicyCmptType));
    }

    @Test
    public void testGetCustomValidations_should_ReturnValidationsRegisteredForSuperType() {
        CustomValidationsResolver validationsPerType = CustomValidationsResolver.createFromList(allValidations);
        Set<ICustomValidation<?>> validations = validationsPerType.getCustomValidations(Attribute.class);
        assertEquals(2, validations.size());
        assertTrue(validations.contains(validationOnAttribute));
        assertTrue(validations.contains(validationOnIAttribute));
    }

    @Test
    public void testGetCustomValidations_should_ReturnValidationsRegisteredForSuperInterface() {
        CustomValidationsResolver validationsPerType = CustomValidationsResolver.createFromList(allValidations);
        Set<ICustomValidation<?>> validations = validationsPerType.getCustomValidations(IAttribute.class);
        assertEquals(1, validations.size());
        assertTrue(validations.contains(validationOnIAttribute));
    }

    @Test
    public void testGetCustomValidations_InterfacesOnly() {
        allValidations = new ArrayList<>();
        allValidations.add(validationOnIAttribute);
        allValidations.add(validationOnIPolicyCmptType);

        CustomValidationsResolver validationsPerType = CustomValidationsResolver.createFromList(allValidations);
        Set<ICustomValidation<?>> validations = validationsPerType.getCustomValidations(Attribute.class);
        assertEquals(1, validations.size());
        assertTrue(validations.contains(validationOnIAttribute));

        validationsPerType = CustomValidationsResolver.createFromList(allValidations);
        Set<ICustomValidation<?>> validations2 = validationsPerType.getCustomValidations(PolicyCmptTypeAttribute.class);
        assertEquals(2, validations2.size());
        assertTrue(validations2.contains(validationOnIAttribute));
        assertTrue(validations2.contains(validationOnIPolicyCmptType));
    }

    @Test
    public void testGetCustomValidations_CacheResult() {
        CustomValidationsResolver validationsPerType = spy(CustomValidationsResolver.createFromList(allValidations));

        validationsPerType.getCustomValidations(Attribute.class);
        verify(validationsPerType).resolveCustomValidationsForClassHierarchy(Attribute.class);
        reset(validationsPerType);

        verifyCached(validationsPerType, Attribute.class);
    }

    @Test
    public void testGetCustomValidations_CacheClearAfterAdd() {
        CustomValidationsResolver validationsPerType = spy(CustomValidationsResolver.createFromList(allValidations));

        validationsPerType.getCustomValidations(Attribute.class);
        verify(validationsPerType).resolveCustomValidationsForClassHierarchy(Attribute.class);
        reset(validationsPerType);

        verifyCached(validationsPerType, Attribute.class);

        validationsPerType.addCustomValidation(validationOnAttribute);
        validationsPerType.getCustomValidations(Attribute.class);
        verify(validationsPerType).resolveCustomValidationsForClassHierarchy(Attribute.class);
    }

    @Test
    public void testGetCustomValidations_AlsoCacheResultForNonexistentType() {
        CustomValidationsResolver validationsPerType = spy(CustomValidationsResolver.createFromList(allValidations));

        validationsPerType.getCustomValidations(IConfigElement.class);
        verify(validationsPerType).resolveCustomValidationsForClassHierarchy(IConfigElement.class);
        reset(validationsPerType);

        verifyCached(validationsPerType, IConfigElement.class);
    }

    private void verifyCached(CustomValidationsResolver validationsPerType,
            Class<? extends IIpsObjectPartContainer> clazz) {
        validationsPerType.getCustomValidations(clazz);
        verify(validationsPerType, never()).resolveCustomValidationsForClassHierarchy(clazz);
    }

    class ValidationOnAttribute implements ICustomValidation<Attribute> {

        @Override
        public Class<Attribute> getExtendedClass() {
            return Attribute.class;
        }

        @Override
        public MessageList validate(Attribute objectToValidate, IIpsProject ipsProject) {
            return null;
        }

    }

    class ValidationOnIAttribute implements ICustomValidation<IAttribute> {

        @Override
        public Class<IAttribute> getExtendedClass() {
            return IAttribute.class;
        }

        @Override
        public MessageList validate(IAttribute objectToValidate, IIpsProject ipsProject) {
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
                {
            return null;
        }

    }

    class ValidationOnIPolicyCmptType implements ICustomValidation<IPolicyCmptTypeAttribute> {

        @Override
        public Class<IPolicyCmptTypeAttribute> getExtendedClass() {
            return IPolicyCmptTypeAttribute.class;
        }

        @Override
        public MessageList validate(IPolicyCmptTypeAttribute objectToValidate, IIpsProject ipsProject)
                {
            return null;
        }

    }

}
