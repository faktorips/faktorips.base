/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.JavaClass2DatatypeAdaptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.DynamicEnumDatatype;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.internal.model.pctype.CamelCaseToUpperUnderscoreColumnNamingStrategy;
import org.faktorips.devtools.core.internal.model.pctype.CamelCaseToUpperUnderscoreTableNamingStrategy;
import org.faktorips.devtools.core.internal.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.internal.model.productcmpt.NoVersionIdProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.IIpsFeatureConfiguration;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.util.DesignTimeSeverity;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class IpsProjectPropertiesTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IIpsProjectProperties properties;

    private static final String MARKER = "MY_MARKER";
    private static final String ANOTHER_MARKER = "ANOTHER_MARKER";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();
        properties = new IpsProjectProperties(ipsProject);
        properties.addSupportedLanguage(Locale.ENGLISH);
        properties.addSupportedLanguage(Locale.GERMAN);
    }

    @Test
    public void testValidate_ProductCmptNamingStrategy() throws CoreException {
        ((IpsProjectProperties)properties).setProductCmptNamingStrategyInternal(null, "UnknownStrategy-ID");
        MessageList result = properties.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IIpsProjectProperties.MSGCODE_INVALID_PRODUCT_CMPT_NAMING_STRATEGY));

        properties.setProductCmptNamingStrategy(new NoVersionIdProductCmptNamingStrategy());
        result = properties.validate(ipsProject);
        assertNull(result.getMessageByCode(IIpsProjectProperties.MSGCODE_INVALID_PRODUCT_CMPT_NAMING_STRATEGY));
    }

    @Test
    public void testNoNamingStrategy() throws Exception {
        assertNull(properties.getProductCmptNamingStrategy());
        Element element = mock(Element.class);
        NodeList nodeList = mock(NodeList.class);
        when(element.getChildNodes()).thenReturn(nodeList);
        ((IpsProjectProperties)properties).initFromXml(ipsProject, element);
        assertNotNull(properties.getProductCmptNamingStrategy());
        assertTrue(properties.getProductCmptNamingStrategy() instanceof NoVersionIdProductCmptNamingStrategy);
        assertNotNull(properties.getProductCmptNamingStrategy().getIpsProject());
    }

    @Test
    public void testValidate_RequiredFeatures() throws CoreException {
        IIpsProjectProperties props = new IpsProjectProperties(ipsProject);
        MessageList ml = props.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IIpsProjectProperties.MSGCODE_MISSING_MIN_FEATURE_ID));

        props.setMinRequiredVersionNumber("org.faktorips.feature", "1.0.0");
        ml = props.validate(ipsProject);
        assertNull(ml.getMessageByCode(IIpsProjectProperties.MSGCODE_MISSING_MIN_FEATURE_ID));

        IIpsFeatureVersionManager versionManager = mock(IIpsFeatureVersionManager.class);
        when(versionManager.getFeatureId()).thenReturn("my.feature");
        when(versionManager.isRequiredForAllProjects()).thenReturn(false);
        IpsPlugin.getDefault().setFeatureVersionManagers(new IIpsFeatureVersionManager[] { versionManager });
        ml = props.validate(ipsProject);
        assertNull(ml.getMessageByCode(IIpsProjectProperties.MSGCODE_MISSING_MIN_FEATURE_ID));

        when(versionManager.isRequiredForAllProjects()).thenReturn(true);
        ml = props.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IIpsProjectProperties.MSGCODE_MISSING_MIN_FEATURE_ID));

        props.setMinRequiredVersionNumber("my.feature", "1.0.0");
        ml = props.validate(ipsProject);
        assertNull(ml.getMessageByCode(IIpsProjectProperties.MSGCODE_MISSING_MIN_FEATURE_ID));
    }

    @Test
    public void testValidate_DefinedDatatypes() throws CoreException {
        IIpsProjectProperties props = new IpsProjectProperties(ipsProject);
        MessageList list = props.validate(ipsProject);
        int numOfMessages = list.size();
        DynamicValueDatatype dynDatatype = new DynamicValueDatatype(ipsProject);
        props.setDefinedDatatypes(new DynamicValueDatatype[] { dynDatatype });
        // must suppress as the dynamic datatype's class can't be loaded (and this is written to the
        // error log.)
        suppressLoggingDuringExecutionOfThisTestCase();
        list = props.validate(ipsProject);
        // now there should be at least one more message
        assertTrue(list.size() > numOfMessages);
    }

    @Test
    public void testValidate_PredefinedDatatypes() throws CoreException {
        IIpsProjectProperties props = new IpsProjectProperties(ipsProject);
        MessageList list = props.validate(ipsProject);
        int numOfMessages = list.size();
        props.setPredefinedDatatypesUsed(ipsProject.getIpsModel().getPredefinedValueDatatypes());
        list = props.validate(ipsProject);
        assertEquals(numOfMessages, list.size()); // there should be at least one or
        // message

        props.setPredefinedDatatypesUsed(new String[] { "unknownDatatype" });
        list = props.validate(ipsProject);
        assertTrue(list.size() > numOfMessages); // there should be at least one or
        // message
        assertTrue(list.getMessageByCode(IIpsProjectProperties.MSGCODE_UNKNOWN_PREDEFINED_DATATYPE) != null);
    }

    @Test
    public void testValidate_SupportedLanguagesIsoConformity() throws CoreException {
        IIpsProjectProperties props = new IpsProjectProperties(ipsProject);
        MessageList list = props.validate(ipsProject);
        int numOfMessages = list.size();

        // Test that there is no additional error message if the locale is OK.
        props.addSupportedLanguage(Locale.ENGLISH);
        list = props.validate(ipsProject);
        assertEquals(numOfMessages, list.size());

        // Test for one more additional error message if the locale is not OK.
        props.addSupportedLanguage(new Locale("fooIsNotBarAndBarIsNotFoo"));
        list = props.validate(ipsProject);
        assertEquals(numOfMessages + 1, list.size());
        assertNotNull(list.getMessageByCode(IIpsProjectProperties.MSGCODE_SUPPORTED_LANGUAGE_UNKNOWN_LOCALE));
    }

    @Test
    public void testValidate_SupportedLanguagesDefaultLanguage() throws CoreException {
        IIpsProjectProperties props = new IpsProjectProperties(ipsProject);
        MessageList list = props.validate(ipsProject);
        int numOfMessages = list.size();

        // Test that there is no additional error message if everything is OK.
        props.addSupportedLanguage(Locale.ENGLISH);
        props.addSupportedLanguage(Locale.GERMAN);
        props.setDefaultLanguage(props.getSupportedLanguage(Locale.ENGLISH));
        list = props.validate(ipsProject);
        assertEquals(numOfMessages, list.size());

        // Test for one more additional error message if there is a problem.
        ISupportedLanguage germanLanguage = props.getSupportedLanguage(Locale.GERMAN);
        ((SupportedLanguage)germanLanguage).setDefaultLanguage(true);
        list = props.validate(ipsProject);
        assertEquals(numOfMessages + 1, list.size());
        assertNotNull(list.getMessageByCode(IIpsProjectProperties.MSGCODE_MORE_THAN_ONE_DEFAULT_LANGUAGE));
    }

    @Test
    public void testValidate_FeatureConfiguration_UnknownFeature() throws CoreException {
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);
        MessageList list = props.validate(ipsProject);
        int numOfMessages = list.size();

        // Test that there is no additional error message if everything is OK.
        props.setMinRequiredVersionNumber("a.feature.id", "0.0.1");
        props.setFeatureConfiguration("a.feature.id", new IpsFeatureConfiguration());
        list = props.validate(ipsProject);
        assertThat(list.size(), is(numOfMessages));

        // Test for one more additional error message if there is a problem.
        props.setFeatureConfiguration("another.feature.id", new IpsFeatureConfiguration());
        list = props.validate(ipsProject);
        assertThat(list.size(), is(numOfMessages + 1));
        assertThat(list.getMessageByCode(IIpsProjectProperties.MSGCODE_FEATURE_CONFIGURATION_UNKNOWN_FEATURE),
                is(notNullValue()));
        assertThat(list.getMessageByCode(IIpsProjectProperties.MSGCODE_FEATURE_CONFIGURATION_UNKNOWN_FEATURE).getText(),
                containsString("another.feature.id"));
    }

    @Test
    public void testOptionalConstraints() {
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);

        // tests non-defaults, too.
        for (int i = 0; i < 4; ++i) {
            props.setDerivedUnionIsImplementedRuleEnabled((i & 1) == 1);
            props.setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled((i & 2) == 2);
            props.setMarkerEnumsEnabled((i & 1) == 1);
            props.setRulesWithoutReferencesAllowedEnabled((i & 1) == 1);
            props.setPersistenceSupport((i & 1) == 1);
            props.setBusinessFunctionsForValidationRules((i & 1) == 1);
            props.setChangingOverTimeDefault((i & 1) == 1);

            Element projectEl = props.toXml(newDocument());
            props = new IpsProjectProperties(ipsProject);
            props.initFromXml(ipsProject, projectEl);

            assertEquals(props.isDerivedUnionIsImplementedRuleEnabled(), (i & 1) == 1);
            assertEquals(props.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(),
                    (i & 2) == 2);
            assertEquals(props.isRulesWithoutReferencesAllowedEnabled(), (i & 1) == 1);
            assertEquals(props.isPersistenceSupportEnabled(), (i & 1) == 1);
            assertEquals(props.isMarkerEnumsEnabled(), (i & 1) == 1);
            assertEquals(props.isBusinessFunctionsForValidationRulesEnabled(), (i & 1) == 1);
            assertEquals(props.isChangingOverTimeDefaultEnabled(), (i & 1) == 1);
        }
    }

    @Test
    public void testToXml() {
        // 1) Create a properties object ...
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);
        props.setModelProject(true);
        props.setProductDefinitionProject(true);
        props.setChangesOverTimeNamingConventionIdForGeneratedCode("myConvention");
        props.setBuilderSetId("myBuilder");
        props.setRuntimeIdPrefix("newRuntimeIdPrefix");
        props.setDerivedUnionIsImplementedRuleEnabled(true);
        props.setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(true);
        props.setBusinessFunctionsForValidationRules(true);
        props.setChangingOverTimeDefault(false);
        props.setGenerateValidatorClassDefault(true);
        props.setProductCmptNamingStrategy(new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", true));
        IIpsObjectPath path = new IpsObjectPath(ipsProject);
        path.newSourceFolderEntry(ipsProject.getProject().getFolder("model"));
        props.setIpsObjectPath(path);
        props.setPredefinedDatatypesUsed(new String[] { "Integer", "Boolean" });
        props.setMinRequiredVersionNumber("featureId", "versionNumber");
        props.setPersistenceSupport(true);
        props.getPersistenceOptions().setMaxTableColumnSize(100);
        props.getPersistenceOptions().setMaxTableColumnScale(101);
        props.getPersistenceOptions().setMaxTableColumnPrecision(102);
        props.getPersistenceOptions().setMaxTableColumnSize(100);
        props.getPersistenceOptions().setMaxTableColumnScale(101);
        props.getPersistenceOptions().setMaxTableColumnPrecision(102);

        props.setReleaseExtensionId("myReleaseExtension");
        props.setVersion("1.2.3");
        props.setVersionProviderId("myVersionProvider");

        createDocumentSetUp(props);
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

        IpsFeatureConfiguration featureConfiguration = new IpsFeatureConfiguration();
        featureConfiguration.set("foo", "bar");
        featureConfiguration.set("x", "y");
        props.setFeatureConfiguration("a.feature.id", featureConfiguration);

        // 2) ... and retrieve the corresponding XML element ...
        Element projectEl = props.toXml(newDocument());

        // 3) ... then compare the XML element to the configuration.
        props = new IpsProjectProperties(ipsProject);
        props.initFromXml(ipsProject, projectEl);
        assertTrue(props.isModelProject());
        assertTrue(props.isProductDefinitionProject());
        assertTrue(props.isDerivedUnionIsImplementedRuleEnabled());
        assertTrue(props.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled());
        assertTrue(props.isBusinessFunctionsForValidationRulesEnabled());
        assertFalse(props.isChangingOverTimeDefaultEnabled());
        assertTrue(props.isGenerateValidatorClassDefaultEnabled());
        assertEquals("newRuntimeIdPrefix", props.getRuntimeIdPrefix());
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

        // product release
        assertEquals("myReleaseExtension", props.getReleaseExtensionId());
        // version
        assertEquals("1.2.3", props.getVersion());
        assertEquals("myVersionProvider", props.getVersionProviderId());

        assertTrue(props.isSupportedLanguage(Locale.ENGLISH));
        assertTrue(props.isSupportedLanguage(Locale.KOREAN));
        assertFalse(props.isSupportedLanguage(Locale.GERMAN));

        assertEquals(100, props.getPersistenceOptions().getMaxTableColumnSize());
        assertEquals(101, props.getPersistenceOptions().getMaxTableColumnScale());
        assertEquals(102, props.getPersistenceOptions().getMaxTableColumnPrecision());

        IIpsFeatureConfiguration newFeatureConfiguration = props.getFeatureConfiguration("a.feature.id");
        assertThat(newFeatureConfiguration.get("foo"), is("bar"));
        assertThat(newFeatureConfiguration.get("x"), is("y"));
    }

    @Test
    public void testToXMLForMarkerEnums() {
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);
        props.addMarkerEnum(MARKER);

        assertEquals(1, props.getMarkerEnums().size());
        assertTrue(props.getMarkerEnums().contains(MARKER));

        props.addMarkerEnum(ANOTHER_MARKER);
        props.removeMarkerEnum(MARKER);

        createDocumentSetUp(props);
        Element projectEl = props.toXml(newDocument());
        props = new IpsProjectProperties(ipsProject);
        props.initFromXml(ipsProject, projectEl);

        assertEquals(1, props.getMarkerEnums().size());
        assertTrue(props.getMarkerEnums().contains(ANOTHER_MARKER));

    }

    private void createDocumentSetUp(IpsProjectProperties props) {
        Document doc = newDocument();
        Element configEl = doc.createElement("IpsArtefactBuilderSetConfig");
        Element propEl = doc.createElement("Property");
        propEl.setAttribute("name", "xxx");
        propEl.setAttribute("value", "yyy");
        configEl.appendChild(propEl);

        IpsArtefactBuilderSetConfigModel config = new IpsArtefactBuilderSetConfigModel();
        config.initFromXml(configEl);
        props.setBuilderSetConfig(config);
    }

    @Test
    public void testAddDefinedDatatype() {
        IIpsProjectProperties props = new IpsProjectProperties(ipsProject);

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

    @Test
    public void testInitFromXml() {
        IpsProjectProperties props = initPropertiesWithDocumentElement();
        assertTrue(props.isModelProject());
        assertTrue(props.isProductDefinitionProject());
        assertFalse(props.isDerivedUnionIsImplementedRuleEnabled());
        assertTrue(props.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled());
        assertTrue(props.isBusinessFunctionsForValidationRulesEnabled());
        assertFalse(props.isChangingOverTimeDefaultEnabled());
        assertFalse(props.isGenerateValidatorClassDefaultEnabled());
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

        // persistence options
        assertEquals(4092, props.getPersistenceOptions().getMaxTableColumnSize());
        assertEquals(10, props.getPersistenceOptions().getMaxTableColumnScale());
        assertEquals(11, props.getPersistenceOptions().getMaxTableColumnPrecision());
        assertEquals(30, props.getPersistenceOptions().getMaxTableNameLength());
        assertEquals(60, props.getPersistenceOptions().getMaxColumnNameLenght());
        assertTrue(props.getPersistenceOptions().isAllowLazyFetchForSingleValuedAssociations());
        assertEquals(CamelCaseToUpperUnderscoreColumnNamingStrategy.class.getName(),
                props.getPersistenceOptions().getTableColumnNamingStrategy().getClass().getName());
        assertEquals(CamelCaseToUpperUnderscoreTableNamingStrategy.class.getName(),
                props.getPersistenceOptions().getTableNamingStrategy().getClass().getName());

        // product release
        assertEquals("myReleaseExtension", props.getReleaseExtensionId());

        // version
        assertEquals("1.2.3", props.getVersion());
        assertEquals("myVersionProvider", props.getVersionProviderId());

        // supported languages
        Set<ISupportedLanguage> supportedLanguages = props.getSupportedLanguages();
        assertEquals(2, supportedLanguages.size());
        assertTrue(supportedLanguages.contains(new SupportedLanguage(Locale.ENGLISH)));
        assertTrue(supportedLanguages.contains(new SupportedLanguage(Locale.GERMAN)));
    }

    @Test
    public void testInitFromXmlForMarkerEnums() {
        IpsProjectProperties props = initPropertiesWithDocumentElement();

        assertEquals(2, props.getMarkerEnums().size());
        assertTrue(props.getMarkerEnums().contains("marker"));
        assertTrue(props.getMarkerEnums().contains("marker2"));
    }

    @Test
    public void testInitFromXmlForDisableEnumMarkers() {
        IpsProjectProperties props = initPropertiesWithDocumentElement();

        assertFalse(props.isMarkerEnumsEnabled());
    }

    @Test
    public void testIsBusinessFunctionsForValidationRulesEnabled_default() {
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);

        assertFalse(props.isBusinessFunctionsForValidationRulesEnabled());
    }

    @Test
    public void testIsBusinessFunctionsForValidationRulesEnabled() {
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);
        props.setBusinessFunctionsForValidationRules(true);

        assertTrue(props.isBusinessFunctionsForValidationRulesEnabled());
    }

    @Test
    public void testIsChangingOverTimeDefaultEnabled_default() {
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);

        assertFalse(props.isChangingOverTimeDefaultEnabled());
    }

    @Test
    public void testIsChangingOverTimeDefaultEnabled() {
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);
        props.setChangingOverTimeDefault(true);

        assertTrue(props.isChangingOverTimeDefaultEnabled());
    }

    @Test
    public void testIsGenerateValidationClassDefaultEnabled_default() {
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);

        assertFalse(props.isGenerateValidatorClassDefaultEnabled());
    }

    @Test
    public void testIsGenerateValidationClassDefaultEnabled() {
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);
        props.setGenerateValidatorClassDefault(true);

        assertTrue(props.isGenerateValidatorClassDefaultEnabled());
    }

    @Test
    public void testAddSupportedLanguage() {
        properties.addSupportedLanguage(Locale.TAIWAN);
        assertEquals(3, properties.getSupportedLanguages().size());
        assertTrue(properties.getSupportedLanguages().contains(new SupportedLanguage(Locale.TAIWAN)));
    }

    @Test
    public void testRemoveSupportedLanguage() {
        properties.removeSupportedLanguage(properties.getSupportedLanguage(Locale.ENGLISH));
        assertEquals(1, properties.getSupportedLanguages().size());
        assertFalse(properties.getSupportedLanguages().contains(new SupportedLanguage(Locale.ENGLISH)));
    }

    @Test
    public void testRemoveSupportedLanguageNullPointer() {
        try {
            properties.removeSupportedLanguage((ISupportedLanguage)null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testRemoveSupportedLanguageUsingLocale() {
        properties.removeSupportedLanguage(Locale.ENGLISH);
        assertEquals(1, properties.getSupportedLanguages().size());
        assertFalse(properties.getSupportedLanguages().contains(new SupportedLanguage(Locale.ENGLISH)));
    }

    @Test
    public void testRemoveSupportedLanguageUsingLocaleNullPointer() {
        try {
            properties.removeSupportedLanguage((Locale)null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSetDefaultLanguage() {
        ISupportedLanguage englishLanguage = properties.getSupportedLanguage(Locale.ENGLISH);
        properties.setDefaultLanguage(englishLanguage);
        assertTrue(englishLanguage.isDefaultLanguage());
        assertEquals(englishLanguage, properties.getDefaultLanguage());
    }

    @Test
    public void testSetDefaultLanguageNullPointer() {
        try {
            properties.setDefaultLanguage((ISupportedLanguage)null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSetDefaultLanguageUsingLocale() {
        properties.setDefaultLanguage(Locale.ENGLISH);
        assertEquals(properties.getSupportedLanguage(Locale.ENGLISH), properties.getDefaultLanguage());
    }

    @Test
    public void testSetDefaultLanguageUsingLocaleNullPointer() {
        try {
            properties.setDefaultLanguage((Locale)null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSetDefaultLanguageUsingLocaleIllegalLocale() {
        try {
            properties.setDefaultLanguage(Locale.TAIWAN);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testIsSupportedLanguage() {
        assertTrue(properties.isSupportedLanguage(Locale.ENGLISH));
        assertTrue(properties.isSupportedLanguage(Locale.US));
        assertFalse(properties.isSupportedLanguage(Locale.JAPANESE));
    }

    @Test
    public void testIsSupportedLanguageNullPointer() {
        try {
            properties.isSupportedLanguage(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void readFormulaLanguageLocaleFromAdditionalSettings() {
        IpsProjectProperties props = initPropertiesWithDocumentElement();
        assertEquals(Locale.KOREAN, props.getFormulaLanguageLocale());
    }

    @Test
    public void readDuplicateProductComponentSeverityFromAdditionalSettings() {
        IpsProjectProperties props = initPropertiesWithDocumentElement();
        assertEquals(DesignTimeSeverity.ERROR, props.getDuplicateProductComponentSeverity());
    }

    @Test
    public void readPersistenceColumnSizeChecksSeverityFromAdditionalSettings() {
        IpsProjectProperties props = initPropertiesWithDocumentElement();
        assertEquals(DesignTimeSeverity.ERROR, props.getPersistenceColumnSizeChecksSeverity());
    }

    @Test
    public void readTableContentFormatFromAdditionalSettings() {
        IpsProjectProperties props = initPropertiesWithDocumentElement();
        assertEquals(TableContentFormat.CSV, props.getTableContentFormat());
    }

    @Test
    public void ignoreIncompleteSettings() {
        IpsProjectProperties props = initPropertiesWithDocumentElement();
        assertFalse(props.isDerivedUnionIsImplementedRuleEnabled());
    }

    protected IpsProjectProperties initPropertiesWithDocumentElement() {
        Element docEl = getTestDocument().getDocumentElement();
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);
        props.initFromXml(ipsProject, docEl);
        return props;
    }
}
