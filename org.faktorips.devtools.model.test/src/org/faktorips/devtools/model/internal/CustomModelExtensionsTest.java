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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.AbstractCustomValidation;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.extproperties.BooleanExtensionPropertyDefinition;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.model.internal.productcmpt.NoVersionIdProductCmptNamingStrategy;
import org.faktorips.devtools.model.internal.productcmpt.NoVersionIdProductCmptNamingStrategyFactory;
import org.faktorips.devtools.model.internal.type.Attribute;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategyFactory;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategyFactory;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class CustomModelExtensionsTest extends AbstractIpsPluginTest {

    private static final String ANY_ID = "anyId";
    private static final String ANY_NAME = "anyName";

    private IIpsProject ipsProject;
    private CustomModelExtensions modelExtensions;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        modelExtensions = (CustomModelExtensions)IIpsModel.get().getCustomModelExtensions();
    }

    @Test
    public void testGetExtensionPropertyDefinitions_empty() throws Exception {
        IIpsObjectPartContainer object = mock(IIpsObjectPartContainer.class);

        Map<String, IExtensionPropertyDefinition> propertyDefinitions = modelExtensions
                .getExtensionPropertyDefinitions(object);

        assertTrue(propertyDefinitions.isEmpty());
    }

    @Test
    public void testGetExtensionPropertyDefinitions_notEmpty() throws Exception {
        BooleanExtensionPropertyDefinition property = createExtensionProperty((IIpsObjectPartContainer)null);
        modelExtensions.addIpsObjectExtensionProperty(property);
        IIpsObjectPartContainer object = mock(IAttributeValue.class);

        Map<String, IExtensionPropertyDefinition> propertyDefinitions = modelExtensions
                .getExtensionPropertyDefinitions(object);

        assertThat(propertyDefinitions.values(), hasItem((IExtensionPropertyDefinition)property));
    }

    @Test
    public void testGetExtensionPropertyDefinitions_filtered() throws Exception {
        final IIpsObjectPartContainer object = mock(IAttributeValue.class);
        BooleanExtensionPropertyDefinition property = createExtensionProperty(object);
        modelExtensions.addIpsObjectExtensionProperty(property);

        Map<String, IExtensionPropertyDefinition> propertyDefinitions = modelExtensions
                .getExtensionPropertyDefinitions(object);

        assertTrue(propertyDefinitions.isEmpty());
    }

    @Test
    public void testGetExtensionPropertyDefinitions() {
        Class<?> class1 = IAttributeValue.class;
        Class<?> class2 = IConfigElement.class;
        BooleanExtensionPropertyDefinition extProperty1 = createExtensionProperty(class1);
        BooleanExtensionPropertyDefinition extProperty2 = createExtensionProperty(class1);
        BooleanExtensionPropertyDefinition extProperty3 = createExtensionProperty(class2);
        modelExtensions.addIpsObjectExtensionProperty(extProperty1);
        modelExtensions.addIpsObjectExtensionProperty(extProperty2);
        modelExtensions.addIpsObjectExtensionProperty(extProperty3);

        Set<IExtensionPropertyDefinition> definitions1 = modelExtensions.getExtensionPropertyDefinitions(class1, false);
        assertTrue(definitions1.contains(extProperty1));
        assertTrue(definitions1.contains(extProperty2));
        assertEquals(2, definitions1.size());

        Set<IExtensionPropertyDefinition> definitions2 = modelExtensions.getExtensionPropertyDefinitions(class2, false);
        assertTrue(definitions2.contains(extProperty3));
        assertEquals(1, definitions2.size());
    }

    @Test
    public void testGetExtensionPropertyDefinitions_NoDefinitionsRegistered() {
        Class<?> class_ = IAttributeValue.class;
        assertTrue(modelExtensions.getExtensionPropertyDefinitions(class_, false).isEmpty());
    }

    @Test
    public void testGetExtensionPropertyDefinitions_IncludeSupertypes() {
        Class<?> baseClass = IIpsObjectPartContainer.class;
        BooleanExtensionPropertyDefinition extProperty = createExtensionProperty(baseClass);
        modelExtensions.addIpsObjectExtensionProperty(extProperty);

        Class<?> subClass = IAttributeValue.class;
        assertTrue(modelExtensions.getExtensionPropertyDefinitions(subClass, true).contains(extProperty));
    }

    @Test
    public void testGetExtensionPropertyDefinitions_ExcludeSupertypes() {
        Class<?> baseClass = IIpsObjectPartContainer.class;
        BooleanExtensionPropertyDefinition extProperty = createExtensionProperty(baseClass);
        modelExtensions.addIpsObjectExtensionProperty(extProperty);

        Class<?> subClass = IAttributeValue.class;
        assertFalse(modelExtensions.getExtensionPropertyDefinitions(subClass, false).contains(extProperty));
    }

    private BooleanExtensionPropertyDefinition createExtensionProperty(Class<?> extendedType) {
        BooleanExtensionPropertyDefinition property = new BooleanExtensionPropertyDefinition();
        property.setPropertyId(ANY_ID);
        property.setName(ANY_NAME);
        property.setExtendedType(extendedType);
        return property;
    }

    private BooleanExtensionPropertyDefinition createExtensionProperty(IIpsObjectPartContainer object) {
        BooleanExtensionPropertyDefinition property = new BooleanExtensionPropertyDefinitionExtension(object);
        property.setPropertyId(ANY_ID);
        property.setName(ANY_NAME);
        property.setExtendedType(IAttributeValue.class);
        return property;
    }

    @Test
    public void testGetProductCmptNamingStrategyFactory() {
        IProductCmptNamingStrategyFactory factory = modelExtensions
                .getProductCmptNamingStrategyFactory(DateBasedProductCmptNamingStrategy.EXTENSION_ID);
        DateBasedProductCmptNamingStrategyFactory dateBasedFactory = (DateBasedProductCmptNamingStrategyFactory)factory;
        assertNotNull(dateBasedFactory);

        factory = modelExtensions
                .getProductCmptNamingStrategyFactory(NoVersionIdProductCmptNamingStrategy.EXTENSION_ID);
        NoVersionIdProductCmptNamingStrategyFactory noVersionIdFactory = (NoVersionIdProductCmptNamingStrategyFactory)factory;
        assertNotNull(noVersionIdFactory);

        assertNull(modelExtensions.getProductCmptNamingStrategyFactory("UnknownExtensionId"));
    }

    @Test
    public void testCustomValidation() throws CoreRuntimeException {
        PolicyCmptType type = newPolicyCmptType(ipsProject, "Policy");
        IPolicyCmptTypeAttribute attribute = type.newPolicyCmptTypeAttribute();
        attribute.setName("attr1");
        attribute.setDatatype("String");

        // make sure no NPE etc is thrown, when no custom validation is defined for attributes
        MessageList result = attribute.validate(ipsProject);

        // 1.) validation for the implementation class
        MyValidation1 validation1 = new MyValidation1();
        validation1.msgToReturn = Message.newError("M1", "blabal");
        modelExtensions.addCustomValidation(validation1);
        Set<?> validations = modelExtensions.getCustomValidations(PolicyCmptTypeAttribute.class);
        assertEquals(1, validations.size());
        assertTrue(validations.contains(validation1));

        int expectedNoOfMessages = result.size() + 1; // we don't assume 1 message, to be
        // independant of other tests.
        result = attribute.validate(ipsProject);
        assertEquals(expectedNoOfMessages, result.size());
        assertEquals(validation1.msgToReturn, result.getMessageByCode("M1"));

        // 2.) validation for the published interface class
        MyValidation2 validation2 = new MyValidation2();
        modelExtensions.addCustomValidation(validation2);
        validations = modelExtensions.getCustomValidations(PolicyCmptTypeAttribute.class);
        assertEquals(2, validations.size());
        assertTrue(validations.contains(validation1));
        assertTrue(validations.contains(validation2));

        expectedNoOfMessages++;
        result = attribute.validate(ipsProject);
        assertEquals(expectedNoOfMessages, result.size());
        assertEquals(validation1.msgToReturn, result.getMessageByCode("M1"));
        assertEquals(validation2.msgToReturn, result.getMessageByCode("M2"));

        // 3.) validation for the super type
        MyValidation3 validation3 = new MyValidation3();
        modelExtensions.addCustomValidation(validation3);
        validations = modelExtensions.getCustomValidations(PolicyCmptTypeAttribute.class);
        assertEquals(3, validations.size());
        assertTrue(validations.contains(validation1));
        assertTrue(validations.contains(validation2));
        assertTrue(validations.contains(validation3));

        expectedNoOfMessages++;
        result = attribute.validate(ipsProject);
        assertEquals(expectedNoOfMessages, result.size());
        assertEquals(validation1.msgToReturn, result.getMessageByCode("M1"));
        assertEquals(validation2.msgToReturn, result.getMessageByCode("M2"));
        assertEquals(validation3.msgToReturn, result.getMessageByCode("M3"));

        // 4.) validation for a super published interface
        MyValidation4 validation4 = new MyValidation4();
        modelExtensions.addCustomValidation(validation4);
        validations = modelExtensions.getCustomValidations(PolicyCmptTypeAttribute.class);
        assertEquals(4, validations.size());
        assertTrue(validations.contains(validation1));
        assertTrue(validations.contains(validation2));
        assertTrue(validations.contains(validation3));
        assertTrue(validations.contains(validation4));

        expectedNoOfMessages++;
        result = attribute.validate(ipsProject);
        assertEquals(expectedNoOfMessages, result.size());
        assertEquals(validation1.msgToReturn, result.getMessageByCode("M1"));
        assertEquals(validation2.msgToReturn, result.getMessageByCode("M2"));
        assertEquals(validation3.msgToReturn, result.getMessageByCode("M3"));
        assertEquals(validation4.msgToReturn, result.getMessageByCode("M4"));
    }

    private class MyValidation1 extends AbstractCustomValidation<PolicyCmptTypeAttribute> {

        private Message msgToReturn = Message.newError("M1", "blabal");

        public MyValidation1() {
            super(PolicyCmptTypeAttribute.class);
        }

        @Override
        public MessageList validate(PolicyCmptTypeAttribute attribute, IIpsProject ipsProject) throws CoreRuntimeException {
            MessageList result = new MessageList();
            result.add(msgToReturn);
            return result;
        }

    }

    private class MyValidation2 extends AbstractCustomValidation<IPolicyCmptTypeAttribute> {

        private Message msgToReturn = Message.newError("M2", "blabal");

        public MyValidation2() {
            super(IPolicyCmptTypeAttribute.class);
        }

        @Override
        public MessageList validate(IPolicyCmptTypeAttribute attribute, IIpsProject ipsProject) throws CoreRuntimeException {
            MessageList result = new MessageList();
            result.add(msgToReturn);
            return result;
        }

    }

    private class MyValidation3 extends AbstractCustomValidation<Attribute> {

        private Message msgToReturn = Message.newError("M3", "blabal");

        public MyValidation3() {
            super(Attribute.class);
        }

        @Override
        public MessageList validate(Attribute attribute, IIpsProject ipsProject) throws CoreRuntimeException {
            MessageList result = new MessageList();
            result.add(msgToReturn);
            return result;
        }

    }

    private class MyValidation4 extends AbstractCustomValidation<IAttribute> {

        private Message msgToReturn = Message.newError("M4", "blabal");

        public MyValidation4() {
            super(IAttribute.class);
        }

        @Override
        public MessageList validate(IAttribute attribute, IIpsProject ipsProject) throws CoreRuntimeException {
            MessageList result = new MessageList();
            result.add(msgToReturn);
            return result;
        }

    }

    public static class BooleanExtensionPropertyDefinitionExtension extends BooleanExtensionPropertyDefinition {
        private final IIpsObjectPartContainer object;

        public BooleanExtensionPropertyDefinitionExtension(IIpsObjectPartContainer object) {
            this.object = object;
        }

        @Override
        public boolean isApplicableFor(IIpsObjectPartContainer ipsObjectPartContainer) {
            return ipsObjectPartContainer != object;
        }
    }

}
