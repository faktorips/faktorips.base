/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.ILogListener;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.abstraction.AFile;
import org.faktorips.devtools.model.abstraction.AResource;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.extproperties.BooleanExtensionPropertyDefinition;
import org.faktorips.devtools.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.DefaultVersion;
import org.faktorips.devtools.model.internal.DefaultVersionProvider;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.util.memento.Memento;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class IpsObjectPartContainerTest extends AbstractIpsPluginTest {

    private static final String ANY_ID = "anyId";

    private static final String ANY_NAME = "anyName";

    private TestIpsObjectPartContainer container;

    private TestIpsObjectPartContainerWithVersion versionedContainer;

    private IIpsProject ipsProject;

    private IpsModel model;

    private IDescription usDescription;

    private IDescription germanDescription;

    private ILabel usLabel;

    private ILabel germanLabel;

    private PolicyCmptType containerParent;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();

        containerParent = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Parent");
        container = new TestIpsObjectPartContainer(containerParent);
        versionedContainer = new TestIpsObjectPartContainerWithVersion(containerParent);
        model = (IpsModel)container.getIpsModel();

        usDescription = container.getDescription(Locale.US);
        usDescription.setText("US Description");
        germanDescription = container.getDescription(Locale.GERMAN);
        germanDescription.setText("German Description");

        usLabel = container.getLabel(Locale.US);
        usLabel.setValue("foo");
        usLabel.setPluralValue("foos");
        germanLabel = container.getLabel(Locale.GERMAN);
        germanLabel.setValue("bar");
        germanLabel.setPluralValue("bars");
    }

    @Test
    public void testinitDefaultVersion_emptyVersion() {
        setProjectVersion("0");

        versionedContainer.initDefaultVersion();

        assertTrue(StringUtils.isEmpty(versionedContainer.getSinceVersionString()));
    }

    @Test
    public void testinitDefaultVersion_nonEmptyVersion() {
        setProjectVersion("501");

        versionedContainer.initDefaultVersion();

        assertEquals("501", versionedContainer.getSinceVersionString());
    }

    @Test
    public void testinitDefaultVersion_qualifiedVersion() {
        setProjectVersion("0.0.501.qualifier");

        versionedContainer.initDefaultVersion();

        assertEquals("0.0.501", versionedContainer.getSinceVersionString());
    }

    private void setProjectVersion(String version) {
        DefaultVersionProvider versionProvider = (DefaultVersionProvider)versionedContainer.getIpsProject()
                .getVersionProvider();
        versionProvider.setProjectVersion(new DefaultVersion(version));
    }

    @Test
    public void testGetExtProperty() {
        ExtensionPropertyDefinition extProperty0 = new StringExtensionPropertyDefinition();
        extProperty0.setPropertyId("org.foo.prop0");
        extProperty0.setDefaultValue("default");
        extProperty0.setExtendedType(TestIpsObjectPartContainer.class);
        model.addIpsObjectExtensionProperty(extProperty0);

        assertEquals("default", container.getExtPropertyValue("org.foo.prop0"));
        container.setExtPropertyValue("org.foo.prop0", "blabla");
        assertEquals("blabla", container.getExtPropertyValue("org.foo.prop0"));
        container.setExtPropertyValue("org.foo.prop0", null);
        assertNull(container.getExtPropertyValue("org.foo.prop0"));

        try {
            container.getExtPropertyValue("undefinedProperty");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSetExtProperty() {
        // define ext property
        TestExtProperty extProperty0 = new TestExtProperty();
        extProperty0.setPropertyId("org.foo.prop0");
        extProperty0.setExtendedType(TestIpsObjectPartContainer.class);
        model.addIpsObjectExtensionProperty(extProperty0);

        assertEquals(0, extProperty0.afterSetCounter);
        assertEquals(0, extProperty0.beforeSetCounter);
        assertEquals(0, container.numOfUpdateSrcFileCalls);
        container.setExtPropertyValue("org.foo.prop0", "blabla");
        assertEquals(1, container.numOfUpdateSrcFileCalls);
        assertEquals(1, extProperty0.afterSetCounter);
        assertEquals(1, extProperty0.beforeSetCounter);

        // setting the same value shouldn't update the src file.
        container.setExtPropertyValue("org.foo.prop0", "blabla");
        assertEquals(1, container.numOfUpdateSrcFileCalls);
        // but beforeSetValue and afterSetValue should have been called
        assertEquals(2, extProperty0.afterSetCounter);
        assertEquals(2, extProperty0.beforeSetCounter);

        assertEquals("blabla", container.getExtPropertyValue("org.foo.prop0"));

        try {
            container.setExtPropertyValue("undefinedProperty", null);
        } catch (IllegalArgumentException e) {
        }

        // test veto set
        extProperty0.allowValueToBeSet = false;
        container.setExtPropertyValue("org.foo.prop0", "newValue");
        // as beforeSetValue returns now true, the property should still contain the old value
        assertEquals("blabla", container.getExtPropertyValue("org.foo.prop0"));
    }

    @Test
    public void testIsExtPropertyDefinitionAvailable() {
        ExtensionPropertyDefinition extProperty0 = new StringExtensionPropertyDefinition();
        extProperty0.setPropertyId("org.foo.prop0");
        extProperty0.setDefaultValue("default");
        extProperty0.setExtendedType(TestIpsObjectPartContainer.class);
        model.addIpsObjectExtensionProperty(extProperty0);
        assertTrue(container.isExtPropertyDefinitionAvailable("org.foo.prop0"));
        assertFalse(container.isExtPropertyDefinitionAvailable("org.foo.prop1"));
    }

    @Test
    public void testToXml_ExtensionProperties() {
        // no extension properties
        Element el = container.toXml(newDocument());
        Element extPropertiesEl = XmlUtil.getFirstElement(el, IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        assertNull(extPropertiesEl);

        ExtensionPropertyDefinition extProperty0 = new StringExtensionPropertyDefinition();
        extProperty0.setPropertyId("org.foo.prop0");
        extProperty0.setDefaultValue("default");
        extProperty0.setExtendedType(TestIpsObjectPartContainer.class);
        model.addIpsObjectExtensionProperty(extProperty0);
        model.addIpsObjectExtensionProperty(extProperty0);

        // not null, property has never been accesses => default value must be used
        el = container.toXml(newDocument());
        extPropertiesEl = XmlUtil.getFirstElement(el, IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        assertNotNull(extPropertiesEl);
        Element extProp0El = XmlUtil.getFirstElement(extPropertiesEl, IpsObjectPartContainer.XML_VALUE_ELEMENT);
        String isNull = extProp0El.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        assertFalse(Boolean.valueOf(isNull).booleanValue());
        String valueSection = XmlUtil.getCDATAorTextContent(extProp0El);
        assertNotNull(valueSection);
        assertEquals("default", valueSection);

        // not null, property has been explicitly set
        container.setExtPropertyValue("org.foo.prop0", "value");
        el = container.toXml(newDocument());
        extPropertiesEl = XmlUtil.getFirstElement(el, IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        assertNotNull(extPropertiesEl);
        extProp0El = XmlUtil.getFirstElement(extPropertiesEl, IpsObjectPartContainer.XML_VALUE_ELEMENT);
        isNull = extProp0El.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        assertFalse(Boolean.valueOf(isNull).booleanValue());
        valueSection = XmlUtil.getCDATAorTextContent(extProp0El);
        assertNotNull(valueSection);
        assertEquals("value", valueSection);

        // null
        container.setExtPropertyValue("org.foo.prop0", null);
        el = container.toXml(newDocument());
        extPropertiesEl = XmlUtil.getFirstElement(el, IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        assertNotNull(extPropertiesEl);
        extProp0El = XmlUtil.getFirstElement(extPropertiesEl, IpsObjectPartContainer.XML_VALUE_ELEMENT);
        isNull = extProp0El.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        assertTrue(Boolean.valueOf(isNull).booleanValue());
        valueSection = XmlUtil.getCDATAorTextContent(extProp0El);
        assertNull(valueSection);
    }

    @Test
    public void testToXml_ExtensionProperties_MissingDefinition() {
        // init with several ext prop values
        container.initFromXml(getTestDocument().getDocumentElement());

        ExtensionPropertyDefinition extProperty0 = new StringExtensionPropertyDefinition();
        extProperty0.setPropertyId("org.foo.prop0");
        extProperty0.setDefaultValue("default");
        extProperty0.setExtendedType(TestIpsObjectPartContainer.class);
        model.addIpsObjectExtensionProperty(extProperty0);

        Element containerElement = container.toXml(newDocument());
        containsExtPropValueForId(containerElement, "org.foo.prop0");
        containsExtPropValueForId(containerElement, "org.foo.prop1");
        containsExtPropValueForId(containerElement, "org.foo.prop2");
        containsExtPropValueForId(containerElement, "org.foo.prop3");
    }

    private void containsExtPropValueForId(Element containerElement, String extPropId) {
        Element extPropertiesEl = XmlUtil.getFirstElement(containerElement,
                IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        NodeList extPropValues = extPropertiesEl.getElementsByTagName(IpsObjectPartContainer.XML_VALUE_ELEMENT);
        for (int i = 0; i < extPropValues.getLength(); i++) {
            Element value = (Element)extPropValues.item(i);
            assertTrue(value.hasAttribute("id"));
            if (value.getAttribute("id").equals(extPropId)) {
                return;
            }
        }
        fail();
    }

    @Test
    public void testInitFromXml_ExtensionProperties() {
        ExtensionPropertyDefinition extProp0 = new StringExtensionPropertyDefinition();
        extProp0.setPropertyId("org.foo.prop0");
        extProp0.setExtendedType(TestIpsObjectPartContainer.class);
        extProp0.setDefaultValue("default0");

        ExtensionPropertyDefinition extProp1 = new StringExtensionPropertyDefinition();
        extProp1.setPropertyId("org.foo.prop1");
        extProp1.setExtendedType(TestIpsObjectPartContainer.class);
        extProp1.setDefaultValue("default1");

        ExtensionPropertyDefinition extProp2 = new StringExtensionPropertyDefinition();
        extProp2.setPropertyId("org.foo.prop2");
        extProp2.setExtendedType(TestIpsObjectPartContainer.class);
        extProp2.setDefaultValue("default2");

        ExtensionPropertyDefinition extProp3 = new StringExtensionPropertyDefinition();
        extProp3.setPropertyId("org.foo.prop3");
        extProp3.setExtendedType(TestIpsObjectPartContainer.class);
        extProp3.setDefaultValue("default3");

        model.addIpsObjectExtensionProperty(extProp0);
        model.addIpsObjectExtensionProperty(extProp1);
        model.addIpsObjectExtensionProperty(extProp2);
        model.addIpsObjectExtensionProperty(extProp3);

        Element docEl = getTestDocument().getDocumentElement();
        container.initFromXml(docEl);
        assertEquals("value0", container.getExtPropertyValue("org.foo.prop0"));
        assertEquals("value1", container.getExtPropertyValue("org.foo.prop1"));
        assertEquals("<>&", container.getExtPropertyValue("org.foo.prop2"));
        assertNull(container.getExtPropertyValue("org.foo.prop3"));

        // test if a new extension property that is not stored in the xml is initialized with it's
        // default value
        ExtensionPropertyDefinition newProp = new StringExtensionPropertyDefinition();
        newProp.setPropertyId("org.foo.newProp");
        newProp.setExtendedType(TestIpsObjectPartContainer.class);
        newProp.setDefaultValue("defaultValue");
        model.addIpsObjectExtensionProperty(newProp);
        container.initFromXml(docEl);
        assertEquals("defaultValue", container.getExtPropertyValue("org.foo.newProp"));
    }

    @Test
    public void testInitFromXml_ExtensionPropertyWithoutExtPropDefinition() {
        ExtensionPropertyDefinition extProp0 = new StringExtensionPropertyDefinition();
        extProp0.setPropertyId("org.foo.prop0");
        extProp0.setExtendedType(TestIpsObjectPartContainer.class);
        extProp0.setDefaultValue("default0");

        model.addIpsObjectExtensionProperty(extProp0);

        // test whether property values were loaded even without their ExtensionPropertyDefinitions
        Element docEl = getTestDocument().getDocumentElement();
        container.initFromXml(docEl);
        assertEquals("value0", container.getExtPropertyValue("org.foo.prop0"));

        // getExtPropertyValue() will throw exception, so test whether values are written to xml
        Element containerElement = container.toXml(newDocument());
        containsExtPropValueForId(containerElement, "org.foo.prop0");
        containsExtPropValueForId(containerElement, "org.foo.prop1");
        containsExtPropValueForId(containerElement, "org.foo.prop2");
        containsExtPropValueForId(containerElement, "org.foo.prop3");
    }

    @Test
    public void testInitFromXmlMissingExtPropDefinition() {
        ExtensionPropertyDefinition extProp0 = new StringExtensionPropertyDefinition();
        extProp0.setPropertyId("org.foo.prop0");
        extProp0.setExtendedType(TestIpsObjectPartContainer.class);
        extProp0.setDefaultValue("default0");

        model.addIpsObjectExtensionProperty(extProp0);

        final StringBuilder sb = new StringBuilder();
        ILogListener listener = (status, $) -> sb.append(status.getMessage());
        IpsLog.get().addLogListener(listener);
        try {
            Element docEl = getTestDocument().getDocumentElement();
            container.initFromXml(docEl);
        } finally {
            IpsLog.get().removeLogListener(listener);
        }

        assertTrue(sb.indexOf("Extension property") != -1);
        assertTrue(sb.indexOf("org.foo.prop1") != -1);
        assertTrue(sb.indexOf("is unknown") != -1);
        assertEquals("value0", container.getExtPropertyValue("org.foo.prop0"));

        // exception if no value (not even null) is available
        try {
            container.getExtPropertyValue("org.foo.propInexistent");
            fail();
        } catch (IllegalArgumentException e) {
            // because property doesn't exist
        }
    }

    @Test
    public void testInitFromXmlVersion() throws CoreRuntimeException {
        Element docEl = getTestDocument().getDocumentElement();
        IIpsProjectProperties properties = ipsProject.getProperties();
        ipsProject.setProperties(properties);
        String expectedVersion = new String("1.2.3");

        versionedContainer.initFromXml(docEl);

        assertEquals(expectedVersion, versionedContainer.getSinceVersionString());
    }

    @Test
    public void testNewMemento() {
        Memento memento = container.newMemento();
        assertEquals(container, memento.getOriginator());
    }

    @Test
    public void testSetState() throws CoreRuntimeException {
        germanDescription.setText("blabla");
        Memento memento = container.newMemento();
        germanDescription.setText("newDescription");
        container.setState(memento);
        assertEquals("blabla", germanDescription.getText());

        // test if new parts are removed when the state is restored from the memento
        IIpsPackageFragmentRoot rootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        IPolicyCmptType type = this.newPolicyCmptType(rootFolder, "folder.TestProduct");
        memento = type.newMemento();
        type.newPolicyCmptTypeAttribute();
        assertEquals(5, type.getChildren().length);
        type.setState(memento);
        assertEquals(4, type.getChildren().length);

        IpsSrcFile file2 = new IpsSrcFile(null, IpsObjectType.POLICY_CMPT_TYPE.getFileName("file"));
        IIpsObject pdObject2 = new PolicyCmptType(file2);
        try {
            pdObject2.setState(memento);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testValidate() throws CoreRuntimeException {
        // create srcfile with contents
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        IProductCmpt product = newProductCmpt(root, "TestProductCmpt");

        IProductCmptGeneration generation = (IProductCmptGeneration)product.newGeneration();
        generation.newLink("");

        // validate
        MessageList messages = product.validate(ipsProject);

        assertNotNull(messages);
        assertFalse(messages.isEmpty());
        assertNotNull(messages.getMessageByCode(IProductCmptGeneration.MSGCODE_NO_TEMPLATE));
    }

    /**
     * Test method for {@link IpsObjectPartContainer#validate(IIpsProject)}. Tests whether the
     * validation is performed on {@link IpsObjectPartContainer} contained in a immutable
     * {@link IIpsSrcFile}.
     */
    @Test
    public void testValidate_NotValidateIpsSrcFileImmutable() throws CoreRuntimeException {
        // create srcfile with contents
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        IProductCmpt product = newProductCmpt(root, "TestProductCmpt");
        IProductCmptGeneration generation = (IProductCmptGeneration)product.newGeneration();
        generation.newLink("");
        // save contents
        product.getIpsSrcFile().save(true, null);

        // load data with immutable srcfile that should not be validated
        AFile file = product.getIpsSrcFile().getCorrespondingFile();
        IpsSrcFileImmutable srcFileImmutable = new IpsSrcFileImmutable("TestSrcFileImmutable.ipsproduct",
                file.getContents());

        MessageList messagesImmutable = srcFileImmutable.getIpsObject().validate(ipsProject);

        assertNotNull(messagesImmutable);
        assertTrue(messagesImmutable.isEmpty());
    }

    /**
     * Test method for {@link IpsObjectPartContainer#validate(IIpsProject)}. Tests whether the
     * validation is performed on {@link IpsObjectPartContainer} contained in an off-root
     * {@link IIpsSrcFile}.
     */
    @Test
    public void testValidate_NotValidateIpsSrcFileOffRoot() throws CoreRuntimeException {
        // create srcfile with contents
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        IProductCmpt product = newProductCmpt(root, "TestProductCmpt");
        IProductCmptGeneration generation = (IProductCmptGeneration)product.newGeneration();
        generation.newLink("");
        // save contents
        product.getIpsSrcFile().save(true, null);

        // load data with off-root srcfile that should not be validated
        AFile file = product.getIpsSrcFile().getCorrespondingFile();
        IpsSrcFileOffRoot srcFile = new IpsSrcFileOffRoot(file);

        MessageList messagesImmutable = srcFile.getIpsObject().validate(ipsProject);
        assertNotNull(messagesImmutable);
        assertTrue(messagesImmutable.isEmpty());
    }

    @Test
    public void testValidateDescriptionCountOk() throws CoreRuntimeException {
        MessageList validationMessageList = container.validate(ipsProject);
        assertNull(validationMessageList.getMessageByCode(IIpsObjectPartContainer.MSGCODE_INVALID_DESCRIPTION_COUNT));
    }

    @Test
    public void testValidateDescriptionCountTooFewDescriptions() throws CoreRuntimeException {
        usDescription.delete();
        MessageList validationMessageList = container.validate(ipsProject);

        Message expectedMessage = validationMessageList
                .getMessageByCode(IIpsObjectPartContainer.MSGCODE_INVALID_DESCRIPTION_COUNT);

        assertNotNull(expectedMessage);
        assertEquals(Message.WARNING, expectedMessage.getSeverity());
    }

    @Test
    public void testValidateDescriptionCountTooManyDescriptions() throws CoreRuntimeException {
        container.newDescription();
        MessageList validationMessageList = container.validate(ipsProject);

        Message expectedMessage = validationMessageList
                .getMessageByCode(IIpsObjectPartContainer.MSGCODE_INVALID_DESCRIPTION_COUNT);

        assertNotNull(expectedMessage);
        assertEquals(Message.WARNING, expectedMessage.getSeverity());
    }

    @Test
    public void testValidateDescriptionCount_UsingOwnProject() throws CoreRuntimeException {
        IpsProject superProject = mock(IpsProject.class);

        container.validate(superProject);

        verify(superProject, never()).getReadOnlyProperties();
    }

    @Test
    public void testValidateLabelCountOk() throws CoreRuntimeException {
        MessageList validationMessageList = container.validate(ipsProject);
        assertNull(validationMessageList.getMessageByCode(IIpsObjectPartContainer.MSGCODE_INVALID_LABEL_COUNT));
    }

    @Test
    public void testValidateLabelCountTooFewLabels() throws CoreRuntimeException {
        usLabel.delete();
        MessageList validationMessageList = container.validate(ipsProject);

        Message expectedMessage = validationMessageList
                .getMessageByCode(IIpsObjectPartContainer.MSGCODE_INVALID_LABEL_COUNT);

        assertNotNull(expectedMessage);
        assertEquals(Message.WARNING, expectedMessage.getSeverity());
    }

    @Test
    public void testValidateLabelCountTooManyLabels() throws CoreRuntimeException {
        container.newLabel();
        MessageList validationMessageList = container.validate(ipsProject);

        Message expectedMessage = validationMessageList
                .getMessageByCode(IIpsObjectPartContainer.MSGCODE_INVALID_LABEL_COUNT);

        assertNotNull(expectedMessage);
        assertEquals(Message.WARNING, expectedMessage.getSeverity());
    }

    @Test
    public void testValidateLabelCount_UsingOwnProject() throws CoreRuntimeException {
        IpsProject superProject = mock(IpsProject.class);

        container.validate(superProject);

        verify(superProject, never()).getReadOnlyProperties();
    }

    @Test
    public void testGetChildren() {
        IIpsElement[] children = container.getChildren();
        assertEquals(4, children.length);

        List<IIpsElement> childrenList = Arrays.asList(children);
        assertTrue(childrenList.contains(usDescription));
        assertTrue(childrenList.contains(germanDescription));
        assertTrue(childrenList.contains(usLabel));
        assertTrue(childrenList.contains(germanLabel));
    }

    @Test
    public void testReinitPartCollections() {
        assertEquals(2, container.getLabels().size());
        assertEquals(2, container.getDescriptions().size());
        container.reinitPartCollectionsAccessor();
        assertEquals(0, container.getLabels().size());
        assertEquals(0, container.getDescriptions().size());
    }

    @Test
    public void testAddRemovePart() {
        ILabel labelToAdd = container.newLabel();
        assertEquals(3, container.getLabels().size());
        container.removePartAccessor(labelToAdd);
        assertEquals(2, container.getLabels().size());
        container.addPartAccessor(labelToAdd);
        assertEquals(3, container.getLabels().size());

        IDescription descriptionToAdd = container.newDescription();
        assertEquals(3, container.getDescriptions().size());
        container.removePartAccessor(descriptionToAdd);
        assertEquals(2, container.getDescriptions().size());
        container.addPartAccessor(descriptionToAdd);
        assertEquals(3, container.getDescriptions().size());
    }

    @Test
    public void testNewPartXml() throws DOMException, ParserConfigurationException {
        Document xmlDoc = createXmlDocument("Blub");
        Element element = xmlDoc.createElement(ILabel.XML_TAG_NAME);
        assertTrue(container.newPartAccessor(element, "blub") instanceof ILabel);
        assertEquals(3, container.getLabels().size());

        element = xmlDoc.createElement(IDescription.XML_TAG_NAME);
        assertTrue(container.newPartAccessor(element, "blub") instanceof IDescription);
        assertEquals(3, container.getDescriptions().size());

        element = xmlDoc.createElement("foobar");
        assertNull(container.newPartAccessor(element, "xyz"));
    }

    @Test
    public void testNewPartReflection() {
        assertTrue(container.newPartAccessor(Label.class) instanceof ILabel);
        assertEquals(3, container.getLabels().size());

        assertTrue(container.newPartAccessor(Description.class) instanceof IDescription);
        assertEquals(3, container.getDescriptions().size());
    }

    @Test
    public void testNewDescription() {
        assertNotNull(container.newDescription());
        assertEquals(3, container.getDescriptions().size());
    }

    @Test
    public void testSetDescriptionText() {
        container.setDescriptionText(Locale.US, "foo");
        container.setDescriptionText(Locale.GERMAN, "bar");
        assertEquals("foo", container.getDescriptionText(Locale.US));
        assertEquals("bar", container.getDescriptionText(Locale.GERMAN));
    }

    @Test
    public void testSetDescriptionTextNullPointerLocale() {
        try {
            container.setDescriptionText(null, "foo");
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSetDescriptionTextNullPointerText() {
        container.setDescriptionText(Locale.US, null);
        assertEquals("", container.getDescriptionText(Locale.US));
    }

    @Test
    public void testSetDescriptionTextNotExistent() {
        try {
            container.setDescriptionText(Locale.TAIWAN, "foo");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testNewLabel() {
        assertNotNull(container.newLabel());
        assertEquals(3, container.getLabels().size());
    }

    @Test
    public void testGetLabelValue() {
        assertEquals(germanLabel.getValue(), container.getLabelValue(Locale.GERMAN));
        assertEquals(usLabel.getValue(), container.getLabelValue(Locale.US));
    }

    @Test
    public void testGetLabelValueNotExistent() {
        assertNull(container.getLabelValue(Locale.TAIWAN));
    }

    @Test
    public void testGetLabelValueNullPointer() {
        try {
            container.getLabelValue(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetPluralLabelValue() {
        assertEquals(germanLabel.getPluralValue(), container.getPluralLabelValue(Locale.GERMAN));
        assertEquals(usLabel.getPluralValue(), container.getPluralLabelValue(Locale.US));
    }

    @Test
    public void testGetPluralLabelValueNotExistent() {
        assertNull(container.getPluralLabelValue(Locale.TAIWAN));
    }

    @Test
    public void testGetPluralLabelValueNullPointer() {
        try {
            container.getPluralLabelValue(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSetLabelValue() {
        container.setLabelValue(Locale.US, "foo");
        assertEquals("foo", container.getLabelValue(Locale.US));
    }

    @Test
    public void testSetLabelValueNullPointerLocale() {
        try {
            container.setLabelValue(null, "foo");
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSetLabelValueNullPointerValue() {
        container.setLabelValue(Locale.US, null);
        assertEquals("", container.getLabelValue(Locale.US));
    }

    @Test
    public void testSetLabelValueNotExistent() {
        try {
            container.setLabelValue(Locale.TAIWAN, "foo");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSetPluralLabelValue() {
        container.setPluralLabelValue(Locale.US, "foos");
        assertEquals("foos", container.getPluralLabelValue(Locale.US));
    }

    @Test
    public void testSetPluralLabelValueNullPointerLocale() {
        try {
            container.setPluralLabelValue(null, "foos");
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSetPluralLabelValueNullPointerPluralValue() {
        container.setPluralLabelValue(Locale.US, null);
        assertEquals("", container.getPluralLabelValue(Locale.US));
    }

    @Test
    public void testSetPluralLabelValueNotExistent() {
        try {
            container.setPluralLabelValue(Locale.TAIWAN, "foos");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGetDescription() {
        assertEquals(usDescription, container.getDescription(Locale.US));
        assertEquals(germanDescription, container.getDescription(Locale.GERMAN));
    }

    @Test
    public void testGetDescriptionNotExistent() {
        assertNull(container.getDescription(Locale.KOREAN));
    }

    @Test
    public void testGetDescriptionNullPointer() {
        try {
            container.getDescription(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetDescriptionText() {
        assertEquals(usDescription.getText(), container.getDescriptionText(Locale.US));
        assertEquals(germanDescription.getText(), container.getDescriptionText(Locale.GERMAN));
    }

    @Test
    public void testGetDescriptionTextNotExistent() {
        assertEquals("", container.getDescriptionText(Locale.KOREAN));
    }

    @Test
    public void testGetDescriptionTextNullPointer() {
        try {
            container.getDescriptionText(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testNewPart() throws CoreRuntimeException {
        TestUnlabeledIpsObjectPartContainer unlabeledContainer = new TestUnlabeledIpsObjectPartContainer(
                newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Parent2"));

        Element xmlTagLabel = newDocument().createElement(ILabel.XML_TAG_NAME);
        Element xmlTagDescription = newDocument().createElement(IDescription.XML_TAG_NAME);

        String idLabel = "ABCDE";
        String idDescription = "ABCDEF";

        assertNull(unlabeledContainer.newPart(xmlTagLabel, idLabel));
        assertEquals(idLabel, container.newPart(xmlTagLabel, idLabel).getId());

        assertNull(unlabeledContainer.newPart(xmlTagDescription, idDescription));
        assertEquals(idDescription, container.newPart(xmlTagDescription, idDescription).getId());
        assertEquals(0, unlabeledContainer.numOfUpdateSrcFileCalls);
        assertEquals(container.xml, unlabeledContainer.copyXml);
    }

    @Test
    public void testGetLabel() {
        assertEquals(usLabel, container.getLabel(Locale.US));
        assertEquals(germanLabel, container.getLabel(Locale.GERMAN));
        assertNull(container.getLabel(Locale.KOREAN));
    }

    @Test
    public void testGetDescriptions() throws CoreRuntimeException {
        changeSupportedLanguagesOrder();
        List<IDescription> descriptionList = container.getDescriptions();
        assertEquals(descriptionList.get(0), usDescription);
        assertEquals(descriptionList.get(1), germanDescription);
    }

    @Test
    public void testGetDescriptionsDefensiveCopy() {
        List<IDescription> descriptions = container.getDescriptions();
        int descriptionCount = descriptions.size();
        descriptions.remove(0);
        assertEquals(descriptionCount, container.getDescriptions().size());
    }

    @Test
    public void testGetLabels() throws CoreRuntimeException {
        changeSupportedLanguagesOrder();
        List<ILabel> labelList = container.getLabels();
        assertEquals(labelList.get(0), usLabel);
        assertEquals(labelList.get(1), germanLabel);
    }

    private void changeSupportedLanguagesOrder() throws CoreRuntimeException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        Set<ISupportedLanguage> supportedLanguages = properties.getSupportedLanguages();
        ISupportedLanguage[] languageArray = supportedLanguages
                .toArray(new ISupportedLanguage[supportedLanguages.size()]);
        properties.removeSupportedLanguage(languageArray[0]);
        properties.removeSupportedLanguage(languageArray[1]);
        properties.addSupportedLanguage(languageArray[1].getLocale());
        properties.addSupportedLanguage(languageArray[0].getLocale());
        ipsProject.setProperties(properties);
    }

    @Test
    public void testGetLabelsDefensiveCopy() {
        List<ILabel> labels = container.getLabels();
        int labelCount = labels.size();
        labels.remove(0);
        assertEquals(labelCount, container.getLabels().size());
    }

    @Test
    public void testGetCaption() throws CoreRuntimeException {
        assertEquals("", container.getCaption(Locale.US));
        try {
            container.getCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetPluralCaption() throws CoreRuntimeException {
        assertEquals("", container.getPluralCaption(Locale.US));
        try {
            container.getPluralCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetLastResortCaption() {
        assertEquals("", container.getLastResortCaption());
    }

    @Test
    public void testGetLastResortPluralCaption() {
        assertEquals("", container.getLastResortPluralCaption());
    }

    @Test
    public void testCopyFrom() throws CoreRuntimeException {
        TestIpsObjectPartContainer source = new TestIpsObjectPartContainer(
                newPolicyCmptTypeWithoutProductCmptType(ipsProject, "SourceParent"));

        // Can't use Mockito as the mocked class will be recognized as a different class
        container.copyFrom(source);

        assertEquals(container.xml, source.copyXml);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCopyFromIllegalSourceClass() {
        container.copyFrom(mock(IIpsObjectPartContainer.class));
    }

    @Test
    public void testGetExtensionPropertyDefinitions() throws Exception {
        BooleanExtensionPropertyDefinition property = new BooleanExtensionPropertyDefinition();
        property.setPropertyId(ANY_ID);
        property.setName(ANY_NAME);
        property.setExtendedType(IIpsObjectPartContainer.class);
        ((IpsModel)getIpsModel()).addIpsObjectExtensionProperty(property);
        IpsObjectPartContainer ipsObjectPartContainer = mock(IpsObjectPartContainer.class, CALLS_REAL_METHODS);

        Collection<IExtensionPropertyDefinition> extensionPropertyDefinitions = ipsObjectPartContainer
                .getExtensionPropertyDefinitions();

        assertThat(extensionPropertyDefinitions, hasItem((IExtensionPropertyDefinition)property));
    }

    @Test
    public void testGetExtensionPropertyDefinition() throws Exception {
        BooleanExtensionPropertyDefinition property = new BooleanExtensionPropertyDefinition();
        property.setPropertyId(ANY_ID);
        property.setName(ANY_NAME);
        property.setExtendedType(IIpsObjectPartContainer.class);
        ((IpsModel)getIpsModel()).addIpsObjectExtensionProperty(property);
        IpsObjectPartContainer ipsObjectPartContainer = mock(IpsObjectPartContainer.class, CALLS_REAL_METHODS);

        IExtensionPropertyDefinition extensionPropertyDefinitions = ipsObjectPartContainer
                .getExtensionPropertyDefinition(ANY_ID);

        assertNotNull(extensionPropertyDefinitions);
    }

    @Test
    public void testToXML_VersionToXml_ignoreVersionIfNotVersionControlled() {
        IVersion<?> version = mock(IVersion.class);
        when(version.asString()).thenReturn(ANY_ID);
        ((IpsObjectPartContainer)container).setSinceVersionString(version.asString());
        Element el = container.toXml(newDocument());

        String attribute = el.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_VERSION);
        assertTrue(attribute.isEmpty());
    }

    @Test
    public void testToXML_VersionToXml() {
        IVersion<?> version = mock(IVersion.class);
        when(version.asString()).thenReturn(ANY_ID);
        versionedContainer.setSinceVersionString(version.asString());
        Element el = versionedContainer.toXml(newDocument());

        String attribute = el.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_VERSION);
        assertEquals(ANY_ID, attribute);
    }

    @Test
    public void testRemoveObsoleteExtensionProperties() {
        ExtensionPropertyDefinition extProp0 = new StringExtensionPropertyDefinition();
        extProp0.setPropertyId("org.foo.prop0");
        extProp0.setExtendedType(TestIpsObjectPartContainer.class);
        extProp0.setDefaultValue("default0");

        model.addIpsObjectExtensionProperty(extProp0);

        // test whether property values were loaded even without their ExtensionPropertyDefinitions
        Element docEl = getTestDocument().getDocumentElement();
        container.initFromXml(docEl);
        assertEquals("value0", container.getExtPropertyValue("org.foo.prop0"));

        Element containerElement = container.toXml(newDocument());
        containsExtPropValueForId(containerElement, "org.foo.prop0");
        containsExtPropValueForId(containerElement, "org.foo.prop1");
        containsExtPropValueForId(containerElement, "org.foo.prop2");
        containsExtPropValueForId(containerElement, "org.foo.prop3");

        // now remove obsolete extension properties
        container.removeObsoleteExtensionProperties();

        containerElement = container.toXml(newDocument());
        containsExtPropValueForId(containerElement, "org.foo.prop0");
        assertFalse(container.isExtPropertyDefinitionAvailable("org.foo.prop1"));
        assertFalse(container.isExtPropertyDefinitionAvailable("org.foo.prop2"));
        assertFalse(container.isExtPropertyDefinitionAvailable("org.foo.prop3"));
    }

    private static class TestUnlabeledIpsObjectPartContainer extends IpsObjectPartContainer {

        private int numOfUpdateSrcFileCalls;

        private Element xml;

        private Element copyXml;

        public TestUnlabeledIpsObjectPartContainer(IIpsElement parent) {
            super(parent, "someId");
        }

        @Override
        protected void objectHasChanged() {
            ++numOfUpdateSrcFileCalls;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        protected Element createElement(Document doc) {
            xml = doc.createElement("TestPart");
            return xml;
        }

        @Override
        public boolean isPluralLabelSupported() {
            return true;
        }

        @Override
        public AResource getCorrespondingResource() {
            return null;
        }

        @Override
        public void delete() throws CoreRuntimeException {

        }

        @Override
        public IIpsObject getIpsObject() {
            IIpsObject ipsObject = mock(IIpsObject.class);
            IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
            when(ipsObject.getIpsSrcFile()).thenReturn(ipsSrcFile);
            return ipsObject;
        }

        @Override
        public boolean isValid(IIpsProject ipsProject) throws CoreRuntimeException {
            return false;
        }

        @Override
        public Severity getValidationResultSeverity(IIpsProject ipsProject) throws CoreRuntimeException {
            return Severity.NONE;
        }

        @Override
        protected IIpsElement[] getChildrenThis() {
            return new IIpsElement[0];
        }

        @Override
        protected void propertiesToXml(Element element) {

        }

        @Override
        protected void initPropertiesFromXml(Element element, String id) {
            copyXml = element;
        }

        @Override
        protected void reinitPartCollectionsThis() {

        }

        @Override
        protected boolean addPartThis(IIpsObjectPart part) {
            return false;
        }

        @Override
        protected boolean removePartThis(IIpsObjectPart part) {
            return false;
        }

        @Override
        protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
            return null;
        }

        @Override
        protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
            return null;
        }

        @Override
        protected void objectHasChanged(PropertyChangeEvent propertyChangeEvent) {
            objectHasChanged();
        }

    }

    private static class TestIpsObjectPartContainerWithVersion extends TestIpsObjectPartContainer
            implements IVersionControlledElement {

        public TestIpsObjectPartContainerWithVersion(IIpsElement parent) {
            super(parent);
        }
    }

    private static class TestIpsObjectPartContainer extends IpsObjectPartContainer
            implements IDescribedElement, ILabeledElement {

        private int numOfUpdateSrcFileCalls;

        private Element xml;

        private Element copyXml;

        public TestIpsObjectPartContainer(IIpsElement parent) {
            super(parent, "someId");
        }

        @Override
        protected void objectHasChanged() {
            ++numOfUpdateSrcFileCalls;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        protected Element createElement(Document doc) {
            xml = doc.createElement("TestPart");
            return xml;
        }

        /*
         * Calls the protected method reinitPartCollections() so that it can be accessed by this
         * test case.
         */
        private void reinitPartCollectionsAccessor() {
            super.reinitPartCollections();
        }

        /*
         * Calls the protected method addPart(IIpsObjectPart) so that it can be accessed by this
         * test case.
         */
        private boolean addPartAccessor(IIpsObjectPart part) {
            return super.addPart(part);
        }

        /*
         * Calls the protected method removePart(IIpsObjectPart) so that it can be accessed by this
         * test case.
         */
        private boolean removePartAccessor(IIpsObjectPart part) {
            return super.removePart(part);
        }

        /*
         * Calls the protected method newPart(Element, String) so that it can be accessed by this
         * test case.
         */
        private IIpsObjectPart newPartAccessor(Element xmlTag, String id) {
            return super.newPart(xmlTag, id);
        }

        /*
         * Calls the protected method newPart(Class) so that it can be accessed by this test case.
         */
        private IIpsObjectPart newPartAccessor(Class<? extends IIpsObjectPart> partType) {
            return super.newPart(partType);
        }

        @Override
        public boolean isPluralLabelSupported() {
            return true;
        }

        @Override
        public AResource getCorrespondingResource() {
            return null;
        }

        @Override
        public void delete() throws CoreRuntimeException {

        }

        @Override
        public IIpsObject getIpsObject() {
            IIpsObject ipsObject = mock(IIpsObject.class);
            IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
            when(ipsObject.getIpsSrcFile()).thenReturn(ipsSrcFile);
            return ipsObject;
        }

        @Override
        public boolean isValid(IIpsProject ipsProject) throws CoreRuntimeException {
            return false;
        }

        @Override
        public Severity getValidationResultSeverity(IIpsProject ipsProject) throws CoreRuntimeException {
            return Severity.NONE;
        }

        @Override
        protected IIpsElement[] getChildrenThis() {
            return new IIpsElement[0];
        }

        @Override
        protected void propertiesToXml(Element element) {

        }

        @Override
        protected void initPropertiesFromXml(Element element, String id) {
            copyXml = element;
        }

        @Override
        protected void reinitPartCollectionsThis() {

        }

        @Override
        protected boolean addPartThis(IIpsObjectPart part) {
            return false;
        }

        @Override
        protected boolean removePartThis(IIpsObjectPart part) {
            return false;
        }

        @Override
        protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
            return null;
        }

        @Override
        protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
            return null;
        }

        @Override
        protected void objectHasChanged(PropertyChangeEvent propertyChangeEvent) {
            objectHasChanged();
        }

    }

    private static class TestExtProperty extends StringExtensionPropertyDefinition {

        int beforeSetCounter = 0;

        int afterSetCounter = 0;

        boolean allowValueToBeSet = true;

        @Override
        public void afterSetValue(IIpsObjectPartContainer ipsObjectPart, Object value) {
            afterSetCounter++;
        }

        @Override
        public boolean beforeSetValue(IIpsObjectPartContainer ipsObjectPart, Object value) {
            beforeSetCounter++;
            return allowValueToBeSet;
        }

    }

}
