/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumPropertyBuilderTest extends AbstractIpsPluginTest {

    @Mock
    private StandardBuilderSet builderSet;

    @Mock
    private EnumTypeBuilder enumTypeBuilder;

    private EnumPropertyBuilder enumPropertyBuilder;

    private IIpsProject ipsProject;

    private EnumType enumType;

    private IIpsSrcFile ipsSrcFile;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        when(builderSet.getEnumTypeBuilder()).thenReturn(enumTypeBuilder);
        when(builderSet.getIpsProject()).thenReturn(ipsProject);
        when(builderSet.getLanguageUsedInGeneratedSourceCode()).thenReturn(Locale.GERMAN);
        enumType = newEnumType(ipsProject, "AnyEnumType");
        enumType.setExtensible(false);
        ipsSrcFile = enumType.getIpsSrcFile();
        when(enumTypeBuilder.getRelativeJavaFile(ipsSrcFile)).thenReturn(new Path("/org/package/AnyEnumType.java"));
    }

    @Before
    public void createEnumPropertyBuilder() throws Exception {
        enumPropertyBuilder = new EnumPropertyBuilder(builderSet);
    }

    @Test
    public void testGetPropertyFile() throws Exception {
        enumPropertyBuilder.build(enumType.getIpsSrcFile());
        IFile propertyFile = enumPropertyBuilder.getPropertyFile(Locale.GERMAN);

        String folder = ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination(true).getFullPath()
                .toPortableString();
        assertEquals(folder + "/org/package/AnyEnumType_de.properties", propertyFile.getFullPath().toPortableString());
    }

    @Test
    public void testBuild_emptyEnum() throws Exception {
        enumPropertyBuilder.build(enumType.getIpsSrcFile());
        IFile propertyFile = enumPropertyBuilder.getPropertyFile(Locale.GERMAN);

        assertFalse(propertyFile.exists());
    }

    @Test
    public void testBuild_emptyEnumValues() throws Exception {
        createEnumAttributes();

        enumPropertyBuilder.build(enumType.getIpsSrcFile());
        IFile propertyFile = enumPropertyBuilder.getPropertyFile(Locale.GERMAN);

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
        IFile propertyFile = enumPropertyBuilder.getPropertyFile(Locale.GERMAN);

        assertTrue(propertyFile.exists());
    }

    @Test
    public void testGeneratePropertyFile_twice() throws Exception {
        createEnumAttributes();
        enumType.newEnumValue();
        enumPropertyBuilder.build(enumType.getIpsSrcFile());
        IFile propertyFile = enumPropertyBuilder.getPropertyFile(Locale.GERMAN);
        long modificationStamp = propertyFile.getModificationStamp();

        enumPropertyBuilder.build(enumType.getIpsSrcFile());

        assertEquals(modificationStamp, propertyFile.getModificationStamp());
    }

    @Test
    public void testGeneratePropertyFile_noChanges() throws Exception {
        EnumPropertyGenerator enumPropertyGenerator = mock(EnumPropertyGenerator.class);
        when(enumPropertyGenerator.getLocale()).thenReturn(Locale.GERMAN);
        createEnumAttributes();
        enumType.newEnumValue();
        enumPropertyBuilder.build(ipsSrcFile);

        enumPropertyBuilder.generatePropertyFile(enumPropertyGenerator);

        verify(enumPropertyGenerator, times(0)).getStream(anyString());
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

        verify(enumPropertyGenerator, times(1)).getStream(anyString());
    }
}
