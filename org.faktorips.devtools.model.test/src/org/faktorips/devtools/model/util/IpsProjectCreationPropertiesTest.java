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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

public class IpsProjectCreationPropertiesTest {

    private IpsProjectCreationProperties properties;

    @Before
    public void setUp() {
        properties = new IpsProjectCreationProperties();
        properties.getLocales().add(new Locale("en"));
    }

    @Test
    public void testDefaults() {
        IpsProjectCreationProperties props = new IpsProjectCreationProperties();
        assertThat(props.getBasePackageName(), is(Messages.IpsProjectCreation_defaultBasePackageName));
        assertThat(props.getSourceFolderName(), is(Messages.IpsProjectCreation_defaultSourceFolderName));
        assertThat(props.getRuntimeIdPrefix(), is(Messages.IpsProjectCreation_defaultRuntimeIdPrefix));
        assertThat(props.getPersistenceSupport(), is(PersistenceSupportNames.ID_GENERIC_JPA_2));
        assertThat(props.isModelProject(), is(true));
        assertThat(props.isProductDefinitionProject(), is(false));
        assertThat(props.isPersistentProject(), is(false));
        assertThat(props.isGroovySupport(), is(true));
        assertThat(props.getLocales().isEmpty(), is(true));
    }

    @Test
    public void testCheckForRequiredProperties() {
        assertThat(properties.checkForRequiredProperties().isEmpty(), is(true));
    }

    @Test
    public void testCheckForRequiredProperties_emptyBasePackage() {
        properties.setBasePackageName("");
        assertThat(properties.checkForRequiredProperties().isEmpty(), is(false));
    }

    @Test
    public void testCheckForRequiredProperties_emptyRuntimeIdPrefix() {
        properties.setRuntimeIdPrefix("");
        assertThat(properties.checkForRequiredProperties().isEmpty(), is(false));
    }

    @Test
    public void testCheckForRequiredProperties_emptySourceFolder() {
        properties.setSourceFolderName("");
        assertThat(properties.checkForRequiredProperties().isEmpty(), is(false));
    }

    @Test
    public void testCheckForRequiredProperties_emptyPersistenceSupport() {
        properties.setPersistentProject(true);
        properties.setPersistenceSupport("");
        assertThat(properties.isPersistentProject(), is(true));
        assertThat(properties.checkForRequiredProperties().isEmpty(), is(false));
    }

    @Test
    public void testCheckForRequiredProperties_emptyLocales() {
        properties.getLocales().clear();
        assertThat(properties.checkForRequiredProperties().isEmpty(), is(false));
    }

    @Test
    public void testCheckForRequiredProperties_nullBasePackage() {
        properties.setBasePackageName(null);
        assertThat(properties.checkForRequiredProperties().isEmpty(), is(false));
    }

    @Test
    public void testCheckForRequiredProperties_nullRuntimeIdPrefix() {
        properties.setRuntimeIdPrefix(null);
        assertThat(properties.checkForRequiredProperties().isEmpty(), is(false));
    }

    @Test
    public void testCheckForRequiredProperties_nullSourceFolder() {
        properties.setSourceFolderName(null);
        assertThat(properties.checkForRequiredProperties().isEmpty(), is(false));
    }

    @Test
    public void testCheckForRequiredProperties_nullPersistenceSupport() {
        properties.setPersistentProject(true);
        properties.setPersistenceSupport(null);
        assertThat(properties.checkForRequiredProperties().isEmpty(), is(false));
    }

    @Test
    public void testCheckForRequiredProperties_nullLocales() {
        properties.setLocales(null);
        assertThat(properties.checkForRequiredProperties().isEmpty(), is(false));
    }

    @Test
    public void testGettersAndSetters() {
        String basePackageName = "test";
        String sourceFolderName = "source";
        String runtimeIdPrefix = "runtime.";
        String persistenceSupport = "Eclipse Link 2.5";
        boolean isModelProject = false;
        boolean isProductDefinitionProject = true;
        boolean isPersistenceProject = true;
        boolean isGroovySupport = false;
        List<Locale> locales = new ArrayList<>();
        locales.add(new Locale("en"));
        locales.add(new Locale("de"));

        properties.setBasePackageName(basePackageName);
        properties.setSourceFolderName(sourceFolderName);
        properties.setRuntimeIdPrefix(runtimeIdPrefix);
        properties.setPersistenceSupport(persistenceSupport);
        properties.setModelProject(isModelProject);
        properties.setProductDefinitionProject(isProductDefinitionProject);
        properties.setPersistentProject(isPersistenceProject);
        properties.setGroovySupport(isGroovySupport);
        properties.setLocales(locales);

        assertThat(properties.getBasePackageName(), is(basePackageName));
        assertThat(properties.getSourceFolderName(), is(sourceFolderName));
        assertThat(properties.getRuntimeIdPrefix(), is(runtimeIdPrefix));
        assertThat(properties.getPersistenceSupport(), is(persistenceSupport));
        assertThat(properties.isModelProject(), is(isModelProject));
        assertThat(properties.isProductDefinitionProject(), is(isProductDefinitionProject));
        assertThat(properties.isPersistentProject(), is(isPersistenceProject));
        assertThat(properties.isGroovySupport(), is(isGroovySupport));
        assertThat(properties.getLocales(), is(locales));
    }
}
