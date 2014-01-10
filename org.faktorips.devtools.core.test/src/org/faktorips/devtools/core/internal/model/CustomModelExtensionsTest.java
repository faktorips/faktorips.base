/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.AbstractCustomValidation;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.internal.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.internal.model.productcmpt.DateBasedProductCmptNamingStrategyFactory;
import org.faktorips.devtools.core.internal.model.productcmpt.NoVersionIdProductCmptNamingStrategy;
import org.faktorips.devtools.core.internal.model.productcmpt.NoVersionIdProductCmptNamingStrategyFactory;
import org.faktorips.devtools.core.internal.model.type.Attribute;
import org.faktorips.devtools.core.model.extproperties.BooleanExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategyFactory;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
        modelExtensions = (CustomModelExtensions)IpsPlugin.getDefault().getIpsModel().getCustomModelExtensions();
    }

    @Test
    public void testGetExtensionPropertyDefinitionss_empty() throws Exception {
        IIpsObjectPartContainer object = mock(IIpsObjectPartContainer.class);

        Map<String, IExtensionPropertyDefinition> propertyDefinitions = modelExtensions
                .getExtensionPropertyDefinitions(object);

        assertTrue(propertyDefinitions.isEmpty());
    }

    @Test
    public void testGetExtensionPropertyDefinitionss_notEmpty() throws Exception {
        BooleanExtensionPropertyDefinition property = createExtensionProperty(null);
        modelExtensions.addIpsObjectExtensionProperty(property);
        IIpsObjectPartContainer object = mock(IAttributeValue.class);

        Map<String, IExtensionPropertyDefinition> propertyDefinitions = modelExtensions
                .getExtensionPropertyDefinitions(object);

        assertThat(propertyDefinitions.values(), hasItem((IExtensionPropertyDefinition)property));
    }

    @Test
    public void testGetExtensionPropertyDefinitionss_filtered() throws Exception {
        final IIpsObjectPartContainer object = mock(IAttributeValue.class);
        BooleanExtensionPropertyDefinition property = createExtensionProperty(object);
        modelExtensions.addIpsObjectExtensionProperty(property);

        Map<String, IExtensionPropertyDefinition> propertyDefinitions = modelExtensions
                .getExtensionPropertyDefinitions(object);

        assertTrue(propertyDefinitions.isEmpty());
    }

    private BooleanExtensionPropertyDefinition createExtensionProperty(final IIpsObjectPartContainer diabledObject) {
        BooleanExtensionPropertyDefinition property = new BooleanExtensionPropertyDefinitionExtension(diabledObject);
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
    public void testCustomValidation() throws CoreException {
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
        public MessageList validate(PolicyCmptTypeAttribute attribute, IIpsProject ipsProject) throws CoreException {
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
        public MessageList validate(IPolicyCmptTypeAttribute attribute, IIpsProject ipsProject) throws CoreException {
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
        public MessageList validate(Attribute attribute, IIpsProject ipsProject) throws CoreException {
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
        public MessageList validate(IAttribute attribute, IIpsProject ipsProject) throws CoreException {
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

    @Test
    public void testGetExtensionPropertyDefinitions() throws Exception {

    }

}
