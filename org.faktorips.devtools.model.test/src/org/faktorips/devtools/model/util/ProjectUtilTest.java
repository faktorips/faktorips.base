/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.builder.AbstractBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.plugin.IpsClasspathContainerInitializer;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.runtime.MessageList;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProjectUtilTest extends AbstractIpsPluginTest {

    private static IJavaProject javaProject;
    private static IpsProjectCreationProperties creationProperties;

    // Property templates for checking

    private static DateBasedProductCmptNamingStrategy namingStrategyTemplate;
    private static String[] predefinedValueDatatypesTemplate;
    private static String changesOverTimeNamingConventionIdForGeneratedCodeTemplate;
    private static String minRequiredVersionTemplate;
    private static String supportedLanguage;

    @BeforeClass
    public static void init() {
        IIpsModel ipsModel = IIpsModel.get();

        // Property templates for checking

        namingStrategyTemplate = new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", true);
        predefinedValueDatatypesTemplate = Arrays.stream(ipsModel.getPredefinedValueDatatypes())
                .map(ValueDatatype::getQualifiedName)
                .toArray(String[]::new);
        changesOverTimeNamingConventionIdForGeneratedCodeTemplate = IIpsModelExtensions.get().getModelPreferences()
                .getChangesOverTimeNamingConvention().getId();
        minRequiredVersionTemplate = IpsModelActivator.getInstalledFaktorIpsVersion();
        supportedLanguage = "en";
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IProject platformProject = newPlatformProject("TestProject");
        javaProject = addJavaCapabilities(platformProject);
        creationProperties = new IpsProjectCreationProperties();
        creationProperties.getLocales().add(new Locale(supportedLanguage));
    }

    @Test
    public void testAddIpsNature_And_hasIpsNature() throws CoreException {
        assertFalse(ProjectUtil.hasIpsNature(javaProject));
        ProjectUtil.addIpsNature(javaProject.getProject());
        assertTrue(ProjectUtil.hasIpsNature(javaProject));
    }

    @Test
    public void testCreateIpsProject() throws CoreException {
        IIpsProject ipsProject = ProjectUtil.createIpsProject(javaProject, creationProperties);

        createIpsProjectCheckRequiredFolders(ipsProject);
        createIpsProjectCheckIpsProjectProperties(ipsProject.getProperties());
        createIpsProjectCheckIpsObjectPath(ipsProject.getIpsObjectPath());

        // Proves that the classpath entries has been set using the StandardJavaProjectConfigurator
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        boolean containsIpsClasspathEntry = Arrays.stream(entries)
                .anyMatch(entry -> IpsClasspathContainerInitializer.CONTAINER_ID.equals(entry.getPath().segment(0)));
        assertThat(containsIpsClasspathEntry, is(true));
    }

    @Test
    public void testCreateIpsProject_setsFormulaLanguageLocaleToEnglishIfDefaultIsNotGerman() throws CoreException {
        creationProperties.setLocales(Arrays.asList(Locale.ITALIAN, Locale.GERMAN));
        IIpsProject ipsProject = ProjectUtil.createIpsProject(javaProject, creationProperties);
        assertThat(ipsProject.getFormulaLanguageLocale(), is(Locale.ENGLISH));
    }

    @Test
    public void testCreateIpsProject_setsFormulaLanguageLocaleToGermanIfDefaultIsGerman() throws CoreException {
        creationProperties.setLocales(Arrays.asList(Locale.GERMAN, Locale.ITALIAN, Locale.ENGLISH));
        IIpsProject ipsProject = ProjectUtil.createIpsProject(javaProject, creationProperties);
        assertThat(ipsProject.getFormulaLanguageLocale(), is(Locale.GERMAN));
    }

    @Test(expected = CoreException.class)
    public void testCreateIpsProject_missingCreationProperties() throws CoreException {
        creationProperties = mock(IpsProjectCreationProperties.class);
        when(creationProperties.validate(javaProject)).thenReturn(MessageList.ofErrors("error"));
        ProjectUtil.createIpsProject(javaProject, creationProperties);
    }

    @Test
    public void testCreateIpsProject_noModelProject() throws CoreException {
        creationProperties.setModelProject(false);
        creationProperties.setProductDefinitionProject(true);
        IIpsProject ipsProject = ProjectUtil.createIpsProject(javaProject, creationProperties);
        assertNull(ipsProject.getProperties().getBuilderSetConfig()
                .getPropertyValue(AbstractBuilderSet.CONFIG_MARK_NONE_MERGEABLE_RESOURCES_AS_DERIVED));
        assertThat(ipsProject.getIpsObjectPath().getOutputFolderForDerivedSources().getName(), is("derived"));
    }

    @Test
    public void testCreateIpsProject_withExistingSourceFolder() throws CoreException {
        IFolder sourceFolder = javaProject.getProject().getFolder(creationProperties.getSourceFolderName());
        sourceFolder.create(true, true, new NullProgressMonitor());

        IIpsProject ipsProject = ProjectUtil.createIpsProject(javaProject, creationProperties);
        IIpsSrcFolderEntry[] srcFolderEntries = ipsProject.getIpsObjectPath().getSourceFolderEntries();
        assertNotNull(srcFolderEntries);
        assertThat(srcFolderEntries.length, is(1));
        assertThat(srcFolderEntries[0].getSourceFolder().getName(), is(creationProperties.getSourceFolderName()));
    }

    @Test
    public void testCreateIpsProject_withExistingDerivcedSrcFolder() throws CoreException {
        IFolder derivedFolder = javaProject.getProject().getFolder("resources");
        derivedFolder.create(true, true, new NullProgressMonitor());

        IIpsProject ipsProject = ProjectUtil.createIpsProject(javaProject, creationProperties);
        assertThat(ipsProject.getIpsObjectPath().getOutputFolderForDerivedSources().getName(), is("resources"));
    }

    @Test
    public void testCreateIpsProject_useConfigurationExtension() throws CoreException {
        List<IIpsProjectConfigurator> projectConfigurators = new ArrayList<>(
                Arrays.asList(mock(IIpsProjectConfigurator.class)));
        IIpsProjectConfigurator configurator = mock(IIpsProjectConfigurator.class);
        when(configurator.canConfigure(any())).thenReturn(true);
        projectConfigurators.add(configurator);
        try (TestIpsModelExtensions extension = new TestIpsModelExtensions()) {
            extension.setIpsProjectConfigurators(projectConfigurators);
            IIpsProject ipsProject = ProjectUtil.createIpsProject(javaProject, creationProperties);

            verify(configurator, times(1)).configureIpsProject(ipsProject, creationProperties);

            // Proves that the classpath entries has not been set by using the
            // StandardJavaProjectConfigurator
            IClasspathEntry[] entries = javaProject.getRawClasspath();
            boolean containsIpsClasspathEntry = Arrays.stream(entries)
                    .anyMatch(
                            entry -> IpsClasspathContainerInitializer.CONTAINER_ID.equals(entry.getPath().segment(0)));
            assertThat(containsIpsClasspathEntry, is(false));
        }
    }

    private void createIpsProjectCheckRequiredFolders(IIpsProject ipsProject) {
        IProject project = ipsProject.getProject();
        assertThat(project.getFolder("src").exists(), is(true));
        assertThat(project.getFolder(creationProperties.getSourceFolderName()).exists(), is(true));
    }

    private void createIpsProjectCheckIpsProjectProperties(IIpsProjectProperties projectProperties) {
        // Creation properties
        assertThat(projectProperties.getRuntimeIdPrefix(), is(creationProperties.getRuntimeIdPrefix()));
        assertThat(projectProperties.isProductDefinitionProject(), is(creationProperties.isProductDefinitionProject()));
        assertThat(projectProperties.isModelProject(), is(creationProperties.isModelProject()));
        assertThat(projectProperties.isPersistenceSupportEnabled(), is(creationProperties.isPersistentProject()));

        // Predefined data types
        assertThat(projectProperties.getPredefinedDatatypesUsed(), is(predefinedValueDatatypesTemplate));

        // Product naming strategy
        IProductCmptNamingStrategy namingStrategy = projectProperties.getProductCmptNamingStrategy();
        assertNotNull(namingStrategy);
        assertThat(namingStrategy, CoreMatchers.instanceOf(DateBasedProductCmptNamingStrategy.class));
        DateBasedProductCmptNamingStrategy dataNamingStrategy = (DateBasedProductCmptNamingStrategy)namingStrategy;
        assertThat(dataNamingStrategy.getDateFormatPattern(), is(namingStrategyTemplate.getDateFormatPattern()));
        assertThat(dataNamingStrategy.getVersionIdSeparator(), is(namingStrategyTemplate.getVersionIdSeparator()));
        assertThat(dataNamingStrategy.isPostfixAllowed(), is(namingStrategyTemplate.isPostfixAllowed()));

        // Generated code naming convention
        assertThat(projectProperties.getChangesOverTimeNamingConventionIdForGeneratedCode(),
                is(changesOverTimeNamingConventionIdForGeneratedCodeTemplate));

        // Minimum required version
        assertThat(projectProperties.getMinRequiredVersionNumber("org.faktorips.feature"),
                is(minRequiredVersionTemplate));

        // Builder set configuration
        IIpsArtefactBuilderSetConfigModel builderSetConfig = projectProperties.getBuilderSetConfig();
        assertNotNull(builderSetConfig);
        assertThat(builderSetConfig.getPropertyValue(PersistenceSupportNames.STD_BUILDER_PROPERTY_PERSISTENCE_PROVIDER),
                is(creationProperties.getPersistenceSupport()));
        assertThat(
                builderSetConfig.getPropertyValue(AbstractBuilderSet.CONFIG_MARK_NONE_MERGEABLE_RESOURCES_AS_DERIVED),
                is("false"));

        // Supported languages and formula language
        Set<String> supportedLanguages = projectProperties.getSupportedLanguages().stream()
                .map(language -> language.getLocale().getLanguage())
                .collect(Collectors.toSet());
        assertThat(supportedLanguages.size(), is(1));
        assertThat(supportedLanguages, hasItem(supportedLanguage));
        assertThat(projectProperties.getDefaultLanguage().getLocale().getLanguage(), is(supportedLanguage));
        assertThat(projectProperties.getFormulaLanguageLocale().getLanguage(), is(supportedLanguage));
    }

    private void createIpsProjectCheckIpsObjectPath(IIpsObjectPath path) {
        assertThat(path.isOutputDefinedPerSrcFolder(), is(false));

        // Base package
        assertThat(path.getBasePackageNameForMergableJavaClasses(), is(creationProperties.getBasePackageName()));
        assertThat(path.getBasePackageNameForDerivedJavaClasses(), is(creationProperties.getBasePackageName()));

        // Output folder
        assertNotNull(path.getOutputFolderForMergableSources());
        assertThat(path.getOutputFolderForMergableSources().getName(), is("src"));
        assertNotNull(path.getOutputFolderForDerivedSources());
        assertThat(path.getOutputFolderForDerivedSources().getName(), is("resources"));

        // Source folder
        IIpsSrcFolderEntry[] srcFolderEntries = path.getSourceFolderEntries();
        assertNotNull(srcFolderEntries);
        assertThat(srcFolderEntries.length, is(1));
        assertThat(srcFolderEntries[0].getSourceFolder().getName(), is(creationProperties.getSourceFolderName()));
    }
}
