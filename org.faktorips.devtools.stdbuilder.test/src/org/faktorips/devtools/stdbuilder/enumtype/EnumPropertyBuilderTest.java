/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.BuilderKindIds;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumPropertyBuilderTest extends AbstractStdBuilderTest {

    private EnumPropertyBuilder enumPropertyBuilder;

    private EnumType enumType;

    private IIpsSrcFile ipsSrcFile;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.getBuilderSetConfig().setPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_GENERATOR_LOCALE,
                Locale.GERMAN.toString(), null);
        ipsProject.setProperties(properties);
        enumType = newEnumType(ipsProject, "AnyEnumType");
        enumType.setExtensible(false);
        ipsSrcFile = enumType.getIpsSrcFile();
        enumPropertyBuilder = (EnumPropertyBuilder)builderSet.getBuilderById(BuilderKindIds.ENUM_PROPERTY);
    }

    @Test
    public void testGetPropertyFile() throws Exception {
        enumPropertyBuilder.build(enumType.getIpsSrcFile());
        IFile propertyFile = enumPropertyBuilder.getPropertyFile(enumType.getIpsSrcFile(), Locale.GERMAN);

        String folder = ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination(true).getResource()
                .getFullPath().toPortableString();
        assertEquals(folder + '/' + BASE_PACKAGE_NAME_MERGABLE.replace('.', '/') + "/AnyEnumType_de.properties",
                propertyFile.getFullPath().toPortableString());
    }

    @Test
    public void testBuild_emptyEnum() throws Exception {
        enumPropertyBuilder.build(enumType.getIpsSrcFile());
        IFile propertyFile = enumPropertyBuilder.getPropertyFile(enumType.getIpsSrcFile(), Locale.GERMAN);

        assertFalse(propertyFile.exists());
    }

    @Test
    public void testBuild_emptyEnumValues() throws Exception {
        createEnumAttributes();

        enumPropertyBuilder.build(enumType.getIpsSrcFile());
        IFile propertyFile = enumPropertyBuilder.getPropertyFile(enumType.getIpsSrcFile(), Locale.GERMAN);

        assertFalse(propertyFile.exists());
    }

    private void createEnumAttributes() throws CoreException {
        IEnumAttribute idAttribute = enumType.newEnumAttribute();
        idAttribute.setName("id");
        idAttribute.setIdentifier(true);
        IEnumAttribute multilingualAttribute = enumType.newEnumAttribute();
        multilingualAttribute.setName("name");
        multilingualAttribute.setMultilingual(true);
        multilingualAttribute.setDatatype(Datatype.STRING.getQualifiedName());
    }

    @Test
    public void testBuild_withEnumValues() throws Exception {
        createEnumAttributes();
        enumType.newEnumValue();

        enumPropertyBuilder.build(enumType.getIpsSrcFile());
        IFile propertyFile = enumPropertyBuilder.getPropertyFile(enumType.getIpsSrcFile(), Locale.GERMAN);

        assertTrue(propertyFile.exists());
    }

    @Test
    public void testGeneratePropertyFile_twice() throws Exception {
        createEnumAttributes();
        enumType.newEnumValue();
        enumPropertyBuilder.build(enumType.getIpsSrcFile());
        IFile propertyFile = enumPropertyBuilder.getPropertyFile(enumType.getIpsSrcFile(), Locale.GERMAN);
        long modificationStamp = propertyFile.getModificationStamp();

        enumPropertyBuilder.build(enumType.getIpsSrcFile());

        assertEquals(modificationStamp, propertyFile.getModificationStamp());
    }

    @Test
    public void testGeneratePropertyFile_changes() throws Exception {
        EnumPropertyGenerator enumPropertyGenerator = mock(EnumPropertyGenerator.class);
        when(enumPropertyGenerator.getLocale()).thenReturn(Locale.GERMAN);
        createEnumAttributes();
        enumType.newEnumValue();
        enumPropertyBuilder.build(ipsSrcFile);
        when(enumPropertyGenerator.generatePropertyFile()).thenReturn(true);

        enumPropertyBuilder.generatePropertyFile(enumPropertyGenerator);

        verify(enumPropertyGenerator, times(1)).getStream();
    }

    @Test
    public void testGeneratePropertyFile_noChanges() throws Exception {
        EnumPropertyGenerator enumPropertyGenerator = mock(EnumPropertyGenerator.class);
        when(enumPropertyGenerator.getLocale()).thenReturn(Locale.GERMAN);
        createEnumAttributes();
        enumType.newEnumValue();
        enumPropertyBuilder.build(ipsSrcFile);

        enumPropertyBuilder.generatePropertyFile(enumPropertyGenerator);

        verify(enumPropertyGenerator, times(0)).getStream();
    }

    @Test
    public void testGetPropertyFile_NotNull() throws CoreException {
        assertNotNull(enumPropertyBuilder.getPropertyFile(enumType.getIpsSrcFile(), Locale.GERMAN));
    }
}
