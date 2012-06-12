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

package org.faktorips.devtools.core.builder.naming;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.naming.JavaPackageStructure;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.junit.Before;
import org.junit.Test;

public class JavaPackageStructureTest {

    private IIpsArtefactBuilder builder;
    private IIpsSrcFile ipsSrcFile;
    private JavaPackageStructure adaptor;

    @Before
    public void setUp() {
        builder = mock(IIpsArtefactBuilder.class);
        ipsSrcFile = mock(IIpsSrcFile.class);

        adaptor = spy(new JavaPackageStructure());
        doReturn("").when(adaptor).getPackageName(any(IIpsSrcFile.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void testGetPackageMergable() throws Exception {
        when(builder.buildsDerivedArtefacts()).thenReturn(true);
        adaptor.getPackage(builder, ipsSrcFile);
        verify(adaptor).getPackageName(ipsSrcFile, false, true);
    }

    @Test
    public void testGetPackage() throws Exception {
        when(builder.buildsDerivedArtefacts()).thenReturn(false);
        adaptor.getPackage(builder, ipsSrcFile);
        verify(adaptor).getPackageName(ipsSrcFile, false, false);
    }

    @Test
    public void testGetPackage_JavaBuilder() throws Exception {
        JavaSourceFileBuilder builder = mock(JavaSourceFileBuilder.class,
                withSettings().extraInterfaces(IIpsArtefactBuilder.class));

        when(builder.buildsDerivedArtefacts()).thenReturn(false);
        when(builder.isBuildingPublishedSourceFile()).thenReturn(false);
        adaptor.getPackage(builder, ipsSrcFile);
        verify(adaptor).getPackageName(ipsSrcFile, false, false);
    }

    @Test
    public void testGetPackage_JavaBuilder_publishedMergable() throws Exception {
        JavaSourceFileBuilder builder = mock(JavaSourceFileBuilder.class,
                withSettings().extraInterfaces(IIpsArtefactBuilder.class));

        when(builder.buildsDerivedArtefacts()).thenReturn(true);
        when(builder.isBuildingPublishedSourceFile()).thenReturn(true);
        adaptor.getPackage(builder, ipsSrcFile);
        verify(adaptor).getPackageName(ipsSrcFile, true, true);
    }

}
