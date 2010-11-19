/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPartContainerTest extends AbstractIpsPluginTest {

    private TestIpsObjectPartContainer container;
    private IpsModel model;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        container = new TestIpsObjectPartContainer();
        model = (IpsModel)container.getIpsModel();
    }

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

    public void testIsExtPropertyDefinitionAvailable() {
        ExtensionPropertyDefinition extProperty0 = new StringExtensionPropertyDefinition();
        extProperty0.setPropertyId("org.foo.prop0");
        extProperty0.setDefaultValue("default");
        extProperty0.setExtendedType(TestIpsObjectPartContainer.class);
        model.addIpsObjectExtensionProperty(extProperty0);
        assertTrue(container.isExtPropertyDefinitionAvailable("org.foo.prop0"));
        assertFalse(container.isExtPropertyDefinitionAvailable("org.foo.prop1"));
    }

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
        CDATASection valueSection = XmlUtil.getFirstCDataSection(extProp0El);
        assertNotNull(valueSection);
        assertEquals("default", valueSection.getData());

        // not null, property has been explicitly set
        container.setExtPropertyValue("org.foo.prop0", "value");
        el = container.toXml(newDocument());
        extPropertiesEl = XmlUtil.getFirstElement(el, IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        assertNotNull(extPropertiesEl);
        extProp0El = XmlUtil.getFirstElement(extPropertiesEl, IpsObjectPartContainer.XML_VALUE_ELEMENT);
        isNull = extProp0El.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        assertFalse(Boolean.valueOf(isNull).booleanValue());
        valueSection = XmlUtil.getFirstCDataSection(extProp0El);
        assertNotNull(valueSection);
        assertEquals("value", valueSection.getData());

        // null
        container.setExtPropertyValue("org.foo.prop0", null);
        el = container.toXml(newDocument());
        extPropertiesEl = XmlUtil.getFirstElement(el, IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        assertNotNull(extPropertiesEl);
        extProp0El = XmlUtil.getFirstElement(extPropertiesEl, IpsObjectPartContainer.XML_VALUE_ELEMENT);
        isNull = extProp0El.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        assertTrue(Boolean.valueOf(isNull).booleanValue());
        valueSection = XmlUtil.getFirstCDataSection(extProp0El);
        assertNull(valueSection);
    }

    public void testInitFromXml() {
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

    public void testInitFromXmlMissingExtPropDefinition() {
        ExtensionPropertyDefinition extProp0 = new StringExtensionPropertyDefinition();
        extProp0.setPropertyId("org.foo.prop0");
        extProp0.setExtendedType(TestIpsObjectPartContainer.class);
        extProp0.setDefaultValue("default0");

        model.addIpsObjectExtensionProperty(extProp0);

        final StringBuffer buf = new StringBuffer();
        ILogListener listener = new ILogListener() {

            @Override
            public void logging(IStatus status, String plugin) {
                buf.append(status.getMessage());
            }

        };
        IpsPlugin.getDefault().getLog().addLogListener(listener);
        Element docEl = getTestDocument().getDocumentElement();
        container.initFromXml(docEl);
        IpsPlugin.getDefault().getLog().removeLogListener(listener);

        assertTrue(buf.indexOf("Extension property") != -1);
        assertTrue(buf.indexOf("org.foo.prop1") != -1);
        assertTrue(buf.indexOf("is unknown") != -1);
        assertEquals("value0", container.getExtPropertyValue("org.foo.prop0"));
        try {
            container.getExtPropertyValue("org.foo.prop1");
            fail();
        } catch (IllegalArgumentException e) {
            // because property doesn't exist
        }
    }

    public void testNewMemento() {
        Memento memento = container.newMemento();
        assertEquals(container, memento.getOriginator());
    }

    public void testSetState() throws CoreException {
        container.setDescription("blabla");
        Memento memento = container.newMemento();
        container.setDescription("newDescription");
        container.setState(memento);
        assertEquals("blabla", container.getDescription());

        // test if new parts are removed when the state is restored from the memento
        IIpsProject ipsProject = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot rootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        IPolicyCmptType type = this.newPolicyCmptType(rootFolder, "folder.TestProduct");
        memento = type.newMemento();
        type.newPolicyCmptTypeAttribute();
        assertEquals(1, type.getChildren().length);
        type.setState(memento);
        assertEquals(0, type.getChildren().length);

        IpsSrcFile file2 = new IpsSrcFile(null, IpsObjectType.POLICY_CMPT_TYPE.getFileName("file"));
        IIpsObject pdObject2 = new PolicyCmptType(file2);
        try {
            pdObject2.setState(memento);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Test method for IpsObjectPartContainer#validate(). Tests wether the validation is performed
     * on <code>IpsObjectPartContainer</code>s contained in historic <code>IIpsSrcFile</code>s.
     */
    public void testValidate() throws CoreException {
        // create srcfile with contents
        IIpsProject proj = newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = proj.getIpsPackageFragmentRoots()[0];
        IProductCmpt product = newProductCmpt(root, "TestProductCmpt");
        IProductCmptGeneration generation = (IProductCmptGeneration)product.newGeneration();
        generation.newConfigElement();
        generation.newLink("");

        // validate
        MessageList messages = product.getIpsSrcFile().getIpsObject().validate(proj);
        assertNotNull(messages);
        assertFalse(messages.isEmpty());
        assertNotNull(messages.getMessageByCode(IProductCmptGeneration.MSGCODE_NO_TEMPLATE));

        // save contents
        product.getIpsSrcFile().save(true, null);

        // load data with immutable srcfile (historic srcfile) that should not be validated
        IFile file = product.getIpsSrcFile().getCorrespondingFile();
        IpsSrcFileImmutable srcFileImmutable = new IpsSrcFileImmutable("TestSrcFileImmutable.ipsproduct",
                file.getContents());
        MessageList messagesImmutable = srcFileImmutable.getIpsObject().validate(proj);
        assertNotNull(messagesImmutable);
        assertTrue(messagesImmutable.isEmpty());
    }

    class TestIpsObjectPartContainer extends AtomicIpsObjectPart {

        private String name;
        private int numOfUpdateSrcFileCalls = 0;

        @Override
        protected void objectHasChanged() {
            ++numOfUpdateSrcFileCalls;
        }

        protected int numOfUpdateSrcFileCalls() {
            return numOfUpdateSrcFileCalls;
        }

        @Override
        protected Element createElement(Document doc) {
            return doc.createElement("TestPart");
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

    }

    class TestExtProperty extends StringExtensionPropertyDefinition {

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
