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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.JavaClass2DatatypeAdaptor;
import org.faktorips.devtools.core.internal.model.DynamicEnumDatatype;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IpsProjectPropertiesTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
    }

    public void testValidate_DefinedDatatypes() throws CoreException {
        IpsProjectProperties props = new IpsProjectProperties();
        MessageList list = props.validate(ipsProject);
        int numOfMessages = list.getNoOfMessages();
        DynamicValueDatatype dynDatatype = new DynamicValueDatatype(ipsProject);
        props.setDefinedDatatypes(new DynamicValueDatatype[] { dynDatatype });
        // must supress as the dynamic datatype's class can't be loaded (and this is written to the
        // error log.)
        suppressLoggingDuringExecutionOfThisTestCase();
        list = props.validate(ipsProject);
        // now there should be at least one more message
        assertTrue(list.getNoOfMessages() > numOfMessages);
    }

    public void testValidate_PredefinedDatatypes() throws CoreException {
        IpsProjectProperties props = new IpsProjectProperties();
        MessageList list = props.validate(ipsProject);
        int numOfMessages = list.getNoOfMessages();
        props.setPredefinedDatatypesUsed(ipsProject.getIpsModel().getPredefinedValueDatatypes());
        list = props.validate(ipsProject);
        assertEquals(numOfMessages, list.getNoOfMessages()); // there should be at least one or
        // message

        props.setPredefinedDatatypesUsed(new String[] { "unknownDatatype" });
        list = props.validate(ipsProject);
        assertTrue(list.getNoOfMessages() > numOfMessages); // there should be at least one or
        // message
        assertTrue(list.getMessageByCode(IIpsProjectProperties.MSGCODE_UNKNOWN_PREDEFINED_DATATYPE) != null);
    }

    public void testOptionalConstraints() {
        IpsProjectProperties props = new IpsProjectProperties();

        // tests non-defaults, too.
        for (int i = 0; i < 4; ++i) {
            props.setDerivedUnionIsImplementedRuleEnabled((i & 1) == 1);
            props.setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled((i & 2) == 2);
            props.setRulesWithoutReferencesAllowedEnabled((i & 1) == 1);
            props.setPersistenceSupport((i & 1) == 1);

            Element projectEl = props.toXml(newDocument());
            props = new IpsProjectProperties();
            props.initFromXml(ipsProject, projectEl);

            assertEquals(props.isDerivedUnionIsImplementedRuleEnabled(), (i & 1) == 1);
            assertEquals(props.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(),
                    (i & 2) == 2);
            assertEquals(props.isRulesWithoutReferencesAllowedEnabled(), (i & 1) == 1);
            assertEquals(props.isPersistenceSupportEnabled(), (i & 1) == 1);
        }
    }

    public void testToXml() {
        IpsProjectProperties props = new IpsProjectProperties();
        props.setModelProject(true);
        props.setProductDefinitionProject(true);
        props.setJavaProjectContainsClassesForDynamicDatatypes(true);
        props.setChangesOverTimeNamingConventionIdForGeneratedCode("myConvention");
        props.setBuilderSetId("myBuilder");
        props.setRuntimeIdPrefix("newRuntimeIdPrefix");
        props.setDerivedUnionIsImplementedRuleEnabled(true);
        props.setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(true);
        props.setProductCmptNamingStrategy(new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", true));
        IIpsObjectPath path = new IpsObjectPath(ipsProject);
        path.newSourceFolderEntry(ipsProject.getProject().getFolder("model"));
        props.setIpsObjectPath(path);
        props.setPredefinedDatatypesUsed(new String[] { "Integer", "Boolean" });
        props.setMinRequiredVersionNumber("featureId", "versionNumber");
        props.setPersistenceSupport(true);

        Document doc = newDocument();
        Element configEl = doc.createElement("IpsArtefactBuilderSetConfig");
        Element propEl = doc.createElement("Property");
        propEl.setAttribute("name", "xxx");
        propEl.setAttribute("value", "yyy");
        configEl.appendChild(propEl);

        IpsArtefactBuilderSetConfigModel config = new IpsArtefactBuilderSetConfigModel();
        config.initFromXml(configEl);
        props.setBuilderSetConfig(config);
        DynamicEnumDatatype datatype = new DynamicEnumDatatype(ipsProject);
        datatype.setIsSupportingNames(true);
        datatype.setGetNameMethodName("getMe");
        datatype.setQualifiedName("org.foo.SomeType");
        datatype.setAdaptedClassName("org.foo.SomeClass");
        props.addDefinedDatatype(datatype);
        JavaClass2DatatypeAdaptor messageListDatatype = new JavaClass2DatatypeAdaptor("MessageList",
                "org.faktorips.MessageList");
        props.addDefinedDatatype(messageListDatatype);

        props.addResourcesPathExcludedFromTheProductDefiniton("a.xml");
        props.addResourcesPathExcludedFromTheProductDefiniton("src/a");

        Element projectEl = props.toXml(newDocument());
        props = new IpsProjectProperties();
        props.initFromXml(ipsProject, projectEl);
        assertTrue(props.isModelProject());
        assertTrue(props.isProductDefinitionProject());
        assertTrue(props.isDerivedUnionIsImplementedRuleEnabled());
        assertTrue(props.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled());
        assertEquals("newRuntimeIdPrefix", props.getRuntimeIdPrefix());
        assertTrue(props.isJavaProjectContainsClassesForDynamicDatatypes());
        assertEquals("myConvention", props.getChangesOverTimeNamingConventionIdForGeneratedCode());
        assertEquals("myBuilder", props.getBuilderSetId());
        assertTrue(props.getProductCmptNamingStrategy() instanceof DateBasedProductCmptNamingStrategy);
        DateBasedProductCmptNamingStrategy strategy = (DateBasedProductCmptNamingStrategy)props
                .getProductCmptNamingStrategy();
        assertEquals("yyyy-MM", strategy.getDateFormatPattern());
        assertEquals(" ", strategy.getVersionIdSeparator());
        assertTrue(strategy.isPostfixAllowed());
        path = props.getIpsObjectPath();
        assertNotNull(path);
        assertEquals(1, path.getEntries().length);
        String[] datatypes = props.getPredefinedDatatypesUsed();
        assertNotNull(datatypes);
        assertEquals(2, datatypes.length);
        assertEquals("Integer", datatypes[0]);
        assertEquals("Boolean", datatypes[1]);
        assertEquals("versionNumber", props.getMinRequiredVersionNumber("featureId"));
        assertEquals("yyy", props.getBuilderSetConfig().getPropertyValue("xxx"));

        assertEquals(2, props.getDefinedDatatypes().size());
        DynamicEnumDatatype newDatatype = (DynamicEnumDatatype)props.getDefinedDatatypes().get(0);
        assertEquals("getMe", newDatatype.getGetNameMethodName());
        assertTrue(newDatatype.isSupportingNames());
        JavaClass2DatatypeAdaptor newDatatype2 = (JavaClass2DatatypeAdaptor)props.getDefinedDatatypes().get(1);
        assertFalse(newDatatype2.isValueDatatype());
        assertEquals("MessageList", newDatatype2.getQualifiedName());
        assertEquals("org.faktorips.MessageList", newDatatype2.getJavaClassName());

        assertTrue(props.isResourceExcludedFromProductDefinition("a.xml"));
        assertTrue(props.isResourceExcludedFromProductDefinition("src/a"));
        assertTrue(props.isPersistenceSupportEnabled());
    }

    public void testAddDefinedDatatype() {
        IpsProjectProperties props = new IpsProjectProperties();

        DynamicValueDatatype type1 = new DynamicValueDatatype(ipsProject);
        type1.setQualifiedName("type1");
        props.addDefinedDatatype(type1);
        assertEquals(type1, props.getDefinedValueDatatypes()[0]);

        DynamicValueDatatype type2 = new DynamicValueDatatype(ipsProject);
        type2.setQualifiedName("type2");
        props.addDefinedDatatype(type2);
        assertEquals(type1, props.getDefinedValueDatatypes()[0]);
        assertEquals(type2, props.getDefinedValueDatatypes()[1]);

        DynamicValueDatatype type1b = new DynamicValueDatatype(ipsProject);
        type1b.setQualifiedName("type1");
        props.addDefinedDatatype(type1b);
        assertEquals(type1b, props.getDefinedValueDatatypes()[0]);
        assertEquals(type2, props.getDefinedValueDatatypes()[1]);

        // type with qName=null
        DynamicValueDatatype type3 = new DynamicValueDatatype(ipsProject);
        props.addDefinedDatatype(type3);
        assertEquals(type3, props.getDefinedValueDatatypes()[2]);

        JavaClass2DatatypeAdaptor type4 = new JavaClass2DatatypeAdaptor("org.Message");
        props.addDefinedDatatype(type4);
        assertEquals(type4, props.getDefinedDatatypes().get(3));
    }

    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        IpsProjectProperties props = new IpsProjectProperties();
        props.initFromXml(ipsProject, docEl);
        assertTrue(props.isModelProject());
        assertTrue(props.isProductDefinitionProject());
        assertTrue(props.isJavaProjectContainsClassesForDynamicDatatypes());
        assertTrue(props.isDerivedUnionIsImplementedRuleEnabled());
        assertTrue(props.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled());
        assertEquals("myConvention", props.getChangesOverTimeNamingConventionIdForGeneratedCode());
        assertEquals("testPrefix", props.getRuntimeIdPrefix());

        DateBasedProductCmptNamingStrategy namingStrategy = (DateBasedProductCmptNamingStrategy)props
                .getProductCmptNamingStrategy();
        assertEquals(" ", namingStrategy.getVersionIdSeparator());
        assertEquals("yyyy-MM", namingStrategy.getDateFormatPattern());
        assertTrue(namingStrategy.isPostfixAllowed());
        assertEquals(ipsProject, namingStrategy.getIpsProject());

        assertEquals("org.faktorips.devtools.stdbuilder.ipsstdbuilderset", props.getBuilderSetId());
        IIpsObjectPath path = props.getIpsObjectPath();
        assertNotNull(path);
        assertEquals(1, path.getEntries().length);

        assertEquals("propValue", props.getBuilderSetConfig().getPropertyValue("prop"));

        // used predefined datatype
        String[] datatypes = props.getPredefinedDatatypesUsed();
        assertNotNull(datatypes);
        assertEquals(2, datatypes.length);
        assertEquals("Boolean", datatypes[0]);
        assertEquals("Integer", datatypes[1]);

        // datatype definitions
        List<Datatype> definedTypes = props.getDefinedDatatypes();
        assertEquals(3, definedTypes.size());
        assertEquals("PaymentMode", definedTypes.get(0).getQualifiedName());
        assertTrue(definedTypes.get(0) instanceof DynamicEnumDatatype);
        assertEquals("PaymentMode", definedTypes.get(1).getQualifiedName());
        assertTrue(!(definedTypes.get(1) instanceof DynamicEnumDatatype));
        assertEquals("MessageList", definedTypes.get(2).getQualifiedName());
        assertTrue(definedTypes.get(2) instanceof JavaClass2DatatypeAdaptor);

        assertEquals("min.Version", props.getMinRequiredVersionNumber("required.id"));
        assertEquals(2, props.getRequiredIpsFeatureIds().length);

        // test resource filter
        assertTrue(props.isResourceExcludedFromProductDefinition("src"));
        assertTrue(props.isResourceExcludedFromProductDefinition("build/build.xml"));
    }
}
