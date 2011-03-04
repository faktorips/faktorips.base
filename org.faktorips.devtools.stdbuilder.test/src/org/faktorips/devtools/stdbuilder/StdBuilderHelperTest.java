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

import org.eclipse.jdt.core.Signature;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.junit.Ignore;
import org.junit.Test;

public class StdBuilderHelperTest {

    @Test
    public void testGetJavaParameterTypeSignatureVoid() {
        assertEquals(String.valueOf(Signature.C_VOID),
                StdBuilderHelper.transformDatatypeToJdtTypeSignature(Datatype.VOID, false));
    }

    @Test
    public void testGetJavaParameterTypeSignatureInt() {
        assertEquals(String.valueOf(Signature.C_INT),
                StdBuilderHelper.transformDatatypeToJdtTypeSignature(Datatype.PRIMITIVE_INT, false));
    }

    @Test
    public void testGetJavaParameterTypeSignatureBoolean() {
        assertEquals(String.valueOf(Signature.C_BOOLEAN),
                StdBuilderHelper.transformDatatypeToJdtTypeSignature(Datatype.PRIMITIVE_BOOLEAN, false));
    }

    @Test
    public void testGetJavaParameterTypeSignatureLong() {
        assertEquals(String.valueOf(Signature.C_LONG),
                StdBuilderHelper.transformDatatypeToJdtTypeSignature(Datatype.PRIMITIVE_LONG, false));
    }

    @Test
    public void testGetJavaParameterTypeSignatureNonPrimitive() {
        assertEquals(Signature.createTypeSignature(Datatype.INTEGER.getName(), false),
                StdBuilderHelper.transformDatatypeToJdtTypeSignature(Datatype.INTEGER, false));
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
        assertEquals(expectedSignature, StdBuilderHelper.transformDatatypeToJdtTypeSignature(policyCmptType, false));
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
        assertEquals(expectedSignature, StdBuilderHelper.transformDatatypeToJdtTypeSignature(policyCmptType, true));
    }

}
