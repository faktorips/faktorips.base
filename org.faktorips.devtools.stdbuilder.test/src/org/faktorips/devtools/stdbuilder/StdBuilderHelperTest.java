/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.Signature;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.junit.Test;

public class StdBuilderHelperTest extends AbstractStdBuilderTest {

    @Test
    public void testGetJavaParameterTypeSignatureVoid() {
        assertEquals(String.valueOf(Signature.C_VOID),
                StdBuilderHelper.transformDatatypeToJdtTypeSignature(Datatype.VOID, false, builderSet, ipsProject));
    }

    @Test
    public void testGetJavaParameterTypeSignatureInt() {
        assertEquals(String.valueOf(Signature.C_INT), StdBuilderHelper.transformDatatypeToJdtTypeSignature(
                Datatype.PRIMITIVE_INT, false, builderSet, ipsProject));
    }

    @Test
    public void testGetJavaParameterTypeSignatureBoolean() {
        assertEquals(String.valueOf(Signature.C_BOOLEAN), StdBuilderHelper.transformDatatypeToJdtTypeSignature(
                Datatype.PRIMITIVE_BOOLEAN, false, builderSet, ipsProject));
    }

    @Test
    public void testGetJavaParameterTypeSignatureLong() {
        assertEquals(String.valueOf(Signature.C_LONG), StdBuilderHelper.transformDatatypeToJdtTypeSignature(
                Datatype.PRIMITIVE_LONG, false, builderSet, ipsProject));
    }

    @Test
    public void testGetJavaParameterTypeSignatureNonPrimitive() {
        assertEquals(Signature.createTypeSignature(Datatype.INTEGER.getName(), false),
                StdBuilderHelper.transformDatatypeToJdtTypeSignature(Datatype.INTEGER, false, builderSet, ipsProject));
    }

    @Test
    public void testGetJavaTypeSignaturePolicyCmptTypeNotResolveToPublished() {
        String packageName = "bar";
        String name = "Foo";
        String qualifiedName = packageName + '.' + name;

        // Create package as parent to test that only the unqualified name is used
        IIpsPackageFragment mockPackageFragment = mock(IIpsPackageFragment.class);
        when(mockPackageFragment.getName()).thenReturn("bar");

        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, qualifiedName);

        String expectedSignature = Signature.createTypeSignature(name, false);
        assertEquals(expectedSignature,
                StdBuilderHelper.transformDatatypeToJdtTypeSignature(policyCmptType, false, builderSet, ipsProject));
    }

    @Test
    public void testGetJavaTypeSignaturePolicyCmptTypeResolveToPublished() {
        String packageName = "bar";
        String name = "Foo";
        String qualifiedName = packageName + '.' + name;

        // Create package as parent to test that only the unqualified name is used
        IIpsPackageFragment mockPackageFragment = mock(IIpsPackageFragment.class);
        when(mockPackageFragment.getName()).thenReturn("bar");

        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, qualifiedName);

        String expectedSignature = Signature.createTypeSignature(
                getJavaNamingConvention().getPublishedInterfaceName(name), false);
        assertEquals(expectedSignature,
                StdBuilderHelper.transformDatatypeToJdtTypeSignature(policyCmptType, true, builderSet, ipsProject));
    }

}
