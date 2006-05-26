/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.memento.Memento;
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
    
    /*
     * @see TestCase#setUp()
     */
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
        
        // test if a new extension property that is not stored in the xml is initialized with it's default value
        ExtensionPropertyDefinition newProp = new StringExtensionPropertyDefinition();
        newProp.setPropertyId("org.foo.newProp");
        newProp.setExtendedType(TestIpsObjectPartContainer.class);
        newProp.setDefaultValue("defaultValue");
        model.addIpsObjectExtensionProperty(newProp);
        container.initFromXml(docEl);
        assertEquals("defaultValue", container.getExtPropertyValue("org.foo.newProp"));
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
        type.newAttribute();
        assertEquals(1, type.getChildren().length);
        type.setState(memento);
        assertEquals(0, type.getChildren().length);
        
        IpsSrcFile file2 = new IpsSrcFile(null, IpsObjectType.POLICY_CMPT_TYPE.getFileName("file"));
        IIpsObject pdObject2 = new PolicyCmptType(file2);
        try {
            pdObject2.setState(memento);
            fail();
        } catch(IllegalArgumentException e) {
        }
    }

    class TestIpsObjectPartContainer extends IpsObjectPart {

        private String name;
        private int numOfUpdateSrcFileCalls = 0;
        
        protected void updateSrcFile() {
			++numOfUpdateSrcFileCalls;
		}
        
        protected int numOfUpdateSrcFileCalls() {
        	return numOfUpdateSrcFileCalls;
        }

		/**
         * Overridden IMethod.
         *
         * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#createElement(org.w3c.dom.Document)
         */
        protected Element createElement(Document doc) {
            return doc.createElement("TestPart");
        }

        
        /**
         * @return Returns the name.
         */
        public String getName() {
            return name;
        }
        
        /**
         * @param name The name to set.
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Overridden IMethod.
         *
         * @see org.faktorips.devtools.core.model.IIpsObjectPart#delete()
         */
        public void delete() {
            deleted = true;
        }

        private boolean deleted = false;

        /**
         * {@inheritDoc}
         */
        public boolean isDeleted() {
        	return deleted;
        }


        /**
         * Overridden IMethod.
         *
         * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
         */
        public Image getImage() {
            return null;
        }

    	/**
    	 * {@inheritDoc}
    	 */
    	public IIpsObjectPart newPart(Class partType) {
    		throw new IllegalArgumentException("Unknown part type" + partType);
    	}
        
    }
    
    class TestExtProperty extends StringExtensionPropertyDefinition {
        
        int beforeSetCounter = 0;
        int afterSetCounter = 0;
        boolean allowValueToBeSet = true;
        
        public void afterSetValue(IpsObjectPartContainer ipsObjectPart, Object value) {
            afterSetCounter++;
        }

        public boolean beforeSetValue(IpsObjectPartContainer ipsObjectPart, Object value) {
            beforeSetCounter++;
            return allowValueToBeSet;
        }
        
        
        
        
    }
}