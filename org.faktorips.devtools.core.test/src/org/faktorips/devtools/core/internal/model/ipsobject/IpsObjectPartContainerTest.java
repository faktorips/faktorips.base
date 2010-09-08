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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPartContainerTest extends AbstractIpsPluginTest {

    private TestIpsObjectPartContainer container;

    private IIpsProject ipsProject;

    private IpsModel model;

    private IDescription usDescription;

    private IDescription germanDescription;

    private ILabel usLabel;

    private ILabel germanLabel;

    private ContentChangeEvent lastEvent;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addSupportedLanguage(Locale.GERMAN);
        properties.addSupportedLanguage(Locale.US);
        properties.setDefaultLanguage(properties.getSupportedLanguage(Locale.GERMAN));
        ipsProject.setProperties(properties);

        container = new TestIpsObjectPartContainer();
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

    // OK to suppress deprecation warnings as this is a test for a deprecated method.
    @SuppressWarnings("deprecation")
    public void testSetDescription() throws CoreException {
        IPolicyCmptType policyContainer = newPolicyCmptType(ipsProject, "TestPolicy");
        container.getIpsModel().addChangeListener(new ContentsChangeListener() {
            @Override
            public void contentsChanged(ContentChangeEvent event) {
                lastEvent = event;
            }
        });

        policyContainer.setDescription("new description");
        assertEquals("new description", policyContainer.getDescription(Locale.GERMAN).getText());
        assertEquals("", policyContainer.getDescription(Locale.US).getText());
        assertTrue(policyContainer.getIpsSrcFile().isDirty());
        assertEquals(policyContainer.getIpsSrcFile(), lastEvent.getIpsSrcFile());

        try {
            container.setDescription(null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    // OK to suppress deprecation warnings as this is a test for a deprecated method.
    @SuppressWarnings("deprecation")
    public void testGetDescriptionDeprecatedVersion() throws CoreException {
        IPolicyCmptType policyContainer = newPolicyCmptType(ipsProject, "TestPolicy");
        assertEquals("", policyContainer.getDescription());

        Locale localizationLocale = IpsPlugin.getMultiLanguageSupport().getLocalizationLocale();
        IDescription localizedDescription = policyContainer.getDescription(localizationLocale);
        if (localizedDescription == null) {
            localizedDescription = policyContainer.newDescription();
            localizedDescription.setLocale(localizationLocale);
        }
        localizedDescription.setText("blub");
        assertEquals("blub", policyContainer.getDescription());
    }

    // OK to suppress deprecation warnings as this is a test for a deprecated method.
    @SuppressWarnings("deprecation")
    public void testIsDescriptionChangeable() {
        assertTrue(container.isDescriptionChangable());
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
        assertEquals(3, type.getChildren().length);
        type.setState(memento);
        assertEquals(2, type.getChildren().length);

        IpsSrcFile file2 = new IpsSrcFile(null, IpsObjectType.POLICY_CMPT_TYPE.getFileName("file"));
        IIpsObject pdObject2 = new PolicyCmptType(file2);
        try {
            pdObject2.setState(memento);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Test method for IpsObjectPartContainer#validate(). Tests whether the validation is performed
     * on <code>IpsObjectPartContainer</code>s contained in historic <code>IIpsSrcFile</code>s.
     */
    public void testValidate() throws CoreException {
        // create srcfile with contents
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        IProductCmpt product = newProductCmpt(root, "TestProductCmpt");
        IProductCmptGeneration generation = (IProductCmptGeneration)product.newGeneration();
        generation.newConfigElement();
        generation.newLink("");

        // validate
        MessageList messages = product.getIpsSrcFile().getIpsObject().validate(ipsProject);
        assertNotNull(messages);
        assertFalse(messages.isEmpty());
        assertNotNull(messages.getMessageByCode(IProductCmptGeneration.MSGCODE_NO_TEMPLATE));

        // save contents
        product.getIpsSrcFile().save(true, null);

        // load data with immutable srcfile (historic srcfile) that should not be validated
        IFile file = product.getIpsSrcFile().getCorrespondingFile();
        IpsSrcFileImmutable srcFileImmutable = new IpsSrcFileImmutable("TestSrcFileImmutable.ipsproduct", file
                .getContents());
        MessageList messagesImmutable = srcFileImmutable.getIpsObject().validate(ipsProject);
        assertNotNull(messagesImmutable);
        assertTrue(messagesImmutable.isEmpty());
    }

    public void testGetChildren() {
        IIpsElement[] children = container.getChildren();
        assertEquals(4, children.length);

        List<IIpsElement> childrenList = Arrays.asList(children);
        assertTrue(childrenList.contains(usDescription));
        assertTrue(childrenList.contains(germanDescription));
        assertTrue(childrenList.contains(usLabel));
        assertTrue(childrenList.contains(germanLabel));
    }

    public void testReinitPartCollections() {
        assertEquals(2, container.getLabels().size());
        assertEquals(2, container.getDescriptions().size());
        container.reinitPartCollectionsAccessor();
        assertEquals(0, container.getLabels().size());
        assertEquals(0, container.getDescriptions().size());
    }

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

    public void testNewPartReflection() {
        assertTrue(container.newPartAccessor(Label.class) instanceof ILabel);
        assertEquals(3, container.getLabels().size());

        assertTrue(container.newPartAccessor(Description.class) instanceof IDescription);
        assertEquals(3, container.getDescriptions().size());
    }

    public void testNewDescription() {
        assertEquals(2, container.getDescriptions().size());
        assertNotNull(container.newDescription());
        assertEquals(3, container.getDescriptions().size());
    }

    public void testNewLabel() {
        assertEquals(2, container.getLabels().size());
        assertNotNull(container.newLabel());
        assertEquals(3, container.getLabels().size());
    }

    public void testGetDescription() {
        assertEquals(usDescription, container.getDescription(Locale.US));
        assertEquals(germanDescription, container.getDescription(Locale.GERMAN));
        assertNull(container.getDescription(Locale.KOREAN));
    }

    public void testGetLabel() {
        assertEquals(usLabel, container.getLabel(Locale.US));
        assertEquals(germanLabel, container.getLabel(Locale.GERMAN));
        assertNull(container.getLabel(Locale.KOREAN));
    }

    public void testGetDescriptions() {
        List<IDescription> descriptionList = container.getDescriptions();
        assertEquals(2, descriptionList.size());
        try {
            descriptionList.remove(descriptionList.get(0));
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testGetLabels() {
        List<ILabel> labelList = container.getLabels();
        assertEquals(2, labelList.size());
        try {
            labelList.remove(labelList.get(0));
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testGetCaption() throws CoreException {
        assertEquals("", container.getCaption(Locale.US));
        try {
            container.getCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetPluralCaption() throws CoreException {
        assertEquals("", container.getPluralCaption(Locale.US));
        try {
            container.getPluralCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetLastResortCaption() {
        assertEquals("", container.getLastResortCaption());
    }

    public void testGetLastResortPluralCaption() {
        assertEquals("", container.getLastResortPluralCaption());
    }

    class TestIpsObjectPartContainer extends AtomicIpsObjectPart implements IDescribedElement, ILabeledElement {

        private String name;

        private int numOfUpdateSrcFileCalls;

        public TestIpsObjectPartContainer() throws CoreException {
            super(newPolicyCmptType(ipsProject, "Parent"), "someId");
        }

        @Override
        protected void objectHasChanged() {
            ++numOfUpdateSrcFileCalls;
        }

        protected int numOfUpdateSrcFileCalls() {
            return numOfUpdateSrcFileCalls;
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
            return doc.createElement("TestPart");
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
