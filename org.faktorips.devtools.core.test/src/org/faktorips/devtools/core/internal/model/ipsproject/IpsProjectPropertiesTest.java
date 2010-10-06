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
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.JavaClass2DatatypeAdaptor;
import org.faktorips.devtools.core.internal.model.DynamicEnumDatatype;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.internal.model.productcmpt.NoVersionIdProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IpsProjectPropertiesTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IIpsProjectProperties properties;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        properties = new IpsProjectProperties();
        properties.addSupportedLanguage(Locale.ENGLISH);
        properties.addSupportedLanguage(Locale.GERMAN);
    }

    public void testValidate_ProductCmptNamingStrategy() throws CoreException {
        ((IpsProjectProperties)properties).setProductCmptNamingStrategyInternal(null, "UnknownStrategy-ID");
        MessageList result = properties.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IIpsProjectProperties.MSGCODE_INVALID_PRODUCT_CMPT_NAMING_STRATEGY));

        properties.setProductCmptNamingStrategy(new NoVersionIdProductCmptNamingStrategy());
        result = properties.validate(ipsProject);
        assertNull(result.getMessageByCode(IIpsProjectProperties.MSGCODE_INVALID_PRODUCT_CMPT_NAMING_STRATEGY));
    }

    public void testValidate_DefinedDatatypes() throws CoreException {
        IpsProjectProperties props = new IpsProjectProperties();
        MessageList list = props.validate(ipsProject);
        int numOfMessages = list.getNoOfMessages();
        DynamicValueDatatype dynDatatype = new DynamicValueDatatype(ipsProject);
        props.setDefinedDatatypes(new DynamicValueDatatype[] { dynDatatype });
        // must suppress as the dynamic datatype's class can't be loaded (and this is written to the
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

    public void testValidate_SupportedLanguagesIsoConformity() throws CoreException {
        IpsProjectProperties props = new IpsProjectProperties();
        MessageList list = props.validate(ipsProject);
        int numOfMessages = list.getNoOfMessages();

        // Test that there is no additional error message if the locale is OK.
        props.addSupportedLanguage(Locale.ENGLISH);
        list = props.validate(ipsProject);
        assertEquals(numOfMessages, list.getNoOfMessages());

        // Test for one more additional error message if the locale is not OK.
        props.addSupportedLanguage(new Locale("fooIsNotBarAndBarIsNotFoo"));
        list = props.validate(ipsProject);
        assertEquals(numOfMessages + 1, list.getNoOfMessages());
        assertNotNull(list.getMessageByCode(IIpsProjectProperties.MSGCODE_SUPPORTED_LANGUAGE_UNKNOWN_LOCALE));
    }

    public void testValidate_SupportedLanguagesDefaultLanguage() throws CoreException {
        IpsProjectProperties props = new IpsProjectProperties();
        MessageList list = props.validate(ipsProject);
        int numOfMessages = list.getNoOfMessages();

        // Test that there is no additional error message if everything is OK.
        props.addSupportedLanguage(Locale.ENGLISH);
        props.addSupportedLanguage(Locale.GERMAN);
        props.setDefaultLanguage(props.getSupportedLanguage(Locale.ENGLISH));
        list = props.validate(ipsProject);
        assertEquals(numOfMessages, list.getNoOfMessages());

        // Test for one more additional error message if there is a problem.
        ISupportedLanguage germanLanguage = props.getSupportedLanguage(Locale.GERMAN);
        ((SupportedLanguage)germanLanguage).setDefaultLanguage(true);
        list = props.validate(ipsProject);
        assertEquals(numOfMessages + 1, list.getNoOfMessages());
        assertNotNull(list.getMessageByCode(IIpsProjectProperties.MSGCODE_MORE_THAN_ONE_DEFAULT_LANGUAGE));
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
        // 1) Create a properties object ...
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

        props.addSupportedLanguage(Locale.ENGLISH);
        props.addSupportedLanguage(Locale.KOREAN);

        // 2) ... and retrieve the corresponding XML element ...
        Element projectEl = props.toXml(newDocument());

        // 3) ... then compare the XML element to the configuration.
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

        assertTrue(props.isSupportedLanguage(Locale.ENGLISH));
        assertTrue(props.isSupportedLanguage(Locale.KOREAN));
        assertFalse(props.isSupportedLanguage(Locale.GERMAN));
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

        // supported languages
        Set<ISupportedLanguage> supportedLanguages = props.getSupportedLanguages();
        assertEquals(2, supportedLanguages.size());
        assertTrue(supportedLanguages.contains(new SupportedLanguage(Locale.ENGLISH)));
        assertTrue(supportedLanguages.contains(new SupportedLanguage(Locale.GERMAN)));
    }

    public void testAddSupportedLanguage() {
        properties.addSupportedLanguage(Locale.TAIWAN);
        assertEquals(3, properties.getSupportedLanguages().size());
        assertTrue(properties.getSupportedLanguages().contains(new SupportedLanguage(Locale.TAIWAN)));
    }

    public void testRemoveSupportedLanguage() {
        properties.removeSupportedLanguage(properties.getSupportedLanguage(Locale.ENGLISH));
        assertEquals(1, properties.getSupportedLanguages().size());
        assertFalse(properties.getSupportedLanguages().contains(new SupportedLanguage(Locale.ENGLISH)));
    }

    public void testRemoveSupportedLanguageNullPointer() {
        try {
            properties.removeSupportedLanguage((ISupportedLanguage)null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testRemoveSupportedLanguageUsingLocale() {
        properties.removeSupportedLanguage(Locale.ENGLISH);
        assertEquals(1, properties.getSupportedLanguages().size());
        assertFalse(properties.getSupportedLanguages().contains(new SupportedLanguage(Locale.ENGLISH)));
    }

    public void testRemoveSupportedLanguageUsingLocaleNullPointer() {
        try {
            properties.removeSupportedLanguage((Locale)null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testSetDefaultLanguage() {
        ISupportedLanguage englishLanguage = properties.getSupportedLanguage(Locale.ENGLISH);
        properties.setDefaultLanguage(englishLanguage);
        assertTrue(englishLanguage.isDefaultLanguage());
        assertEquals(englishLanguage, properties.getDefaultLanguage());
    }

    public void testSetDefaultLanguageNullPointer() {
        try {
            properties.setDefaultLanguage((ISupportedLanguage)null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testSetDefaultLanguageUsingLocale() {
        properties.setDefaultLanguage(Locale.ENGLISH);
        assertEquals(properties.getSupportedLanguage(Locale.ENGLISH), properties.getDefaultLanguage());
    }

    public void testSetDefaultLanguageUsingLocaleNullPointer() {
        try {
            properties.setDefaultLanguage((Locale)null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testSetDefaultLanguageUsingLocaleIllegalLocale() {
        try {
            properties.setDefaultLanguage(Locale.TAIWAN);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsSupportedLanguage() {
        assertTrue(properties.isSupportedLanguage(Locale.ENGLISH));
        assertTrue(properties.isSupportedLanguage(Locale.US));
        assertFalse(properties.isSupportedLanguage(Locale.JAPANESE));
    }

    public void testIsSupportedLanguageNullPointer() {
        try {
            properties.isSupportedLanguage(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

}
