/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.stdbuilder.JavaGeneratorForIpsPart;
import org.faktorips.util.LocalizedStringsSet;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JavaGeneratorForIpsPartTest {

    private JavaGeneratorForIpsPart generator;

    @Before
    public void setUp() {
        generator = new StubGenerator(mock(IIpsObjectPartContainer.class), new LocalizedStringsSet(getClass()));
    }

    @Test
    public void testGetJavaParameterTypeSignatureVoid() {
        assertEquals(String.valueOf(Signature.C_VOID), generator.getJavaTypeSignature(Datatype.VOID, false));
    }

    @Test
    public void testGetJavaParameterTypeSignatureInt() {
        assertEquals(String.valueOf(Signature.C_INT), generator.getJavaTypeSignature(Datatype.PRIMITIVE_INT, false));
    }

    @Test
    public void testGetJavaParameterTypeSignatureBoolean() {
        assertEquals(String.valueOf(Signature.C_BOOLEAN),
                generator.getJavaTypeSignature(Datatype.PRIMITIVE_BOOLEAN, false));
    }

    @Test
    public void testGetJavaParameterTypeSignatureLong() {
        assertEquals(String.valueOf(Signature.C_LONG), generator.getJavaTypeSignature(Datatype.PRIMITIVE_LONG, false));
    }

    @Test
    public void testGetJavaParameterTypeSignatureNonPrimitive() {
        assertEquals(Signature.createTypeSignature(Datatype.INTEGER.getName(), false),
                generator.getJavaTypeSignature(Datatype.INTEGER, false));
    }

    @Test
    public void testGetJavaTypeSignaturePolicyCmptTypeNotResolveToPublished() {
        String name = "Foo";

        // Create package as parent to test that only the unqualified name is used
        IIpsPackageFragment mockPackageFragment = mock(IIpsPackageFragment.class);
        when(mockPackageFragment.getName()).thenReturn("bar");

        IIpsSrcFile ipsSrcFile = new IpsSrcFile(mockPackageFragment, name);
        Datatype policyCmptType = new PolicyCmptType(ipsSrcFile);

        String expectedSignature = Signature.createTypeSignature(name, false);
        assertEquals(expectedSignature, generator.getJavaTypeSignature(policyCmptType, false));
    }

    // TODO AW work in progress
    @Ignore
    @Test
    public void testGetJavaTypeSignaturePolicyCmptTypeResolveToPublished() {
        String name = "Foo";

        // Create package as parent to test that only the unqualified name is used
        IIpsPackageFragment mockPackageFragment = mock(IIpsPackageFragment.class);
        when(mockPackageFragment.getName()).thenReturn("bar");

        IIpsSrcFile ipsSrcFile = new IpsSrcFile(mockPackageFragment, name);
        Datatype policyCmptType = new PolicyCmptType(ipsSrcFile);

        String expectedSignature = Signature.createTypeSignature("I" + name, false);
        assertEquals(expectedSignature, generator.getJavaTypeSignature(policyCmptType, true));
    }

    private static class StubGenerator extends JavaGeneratorForIpsPart {

        public StubGenerator(IIpsObjectPartContainer part, LocalizedStringsSet localizedStringsSet) {
            super(part, localizedStringsSet);
        }

        @Override
        public Locale getLanguageUsedInGeneratedSourceCode() {
            return null;
        }

        @Override
        public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
                IType generatedJavaType,
                IIpsElement ipsElement) {

        }

        @Override
        public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
                IType generatedJavaType,
                IIpsElement ipsElement) {

        }

    }

}
