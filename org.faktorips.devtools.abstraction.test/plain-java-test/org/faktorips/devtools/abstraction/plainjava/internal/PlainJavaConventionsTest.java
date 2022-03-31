/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import static org.faktorips.testsupport.IpsMatchers.hasErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.hasWarningMessage;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class PlainJavaConventionsTest {

    @Test
    public void testValidatePackageName_Valid_ThisPackage() {
        assertThat(PlainJavaConventions.validatePackageName(getClass().getPackageName()), isEmpty());
    }

    @Test
    public void testValidatePackageName_Valid_Single() {
        assertThat(PlainJavaConventions.validatePackageName("test"), isEmpty());
    }

    @Test
    public void testValidatePackageName_Invalid_IllegalCharacter() {
        assertThat(PlainJavaConventions.validatePackageName("foo.§bar"),
                hasErrorMessage(PlainJavaConventions.MSG_CODE_INVALID_PACKAGE_NAME));
    }

    @Test
    public void testValidatePackageName_Invalid_DoublePeriod() {
        assertThat(PlainJavaConventions.validatePackageName("foo..bar"),
                hasErrorMessage(PlainJavaConventions.MSG_CODE_INVALID_PACKAGE_NAME));
    }

    @Test
    public void testValidatePackageName_Invalid_Empty() {
        assertThat(PlainJavaConventions.validatePackageName(""),
                hasErrorMessage(PlainJavaConventions.MSG_CODE_INVALID_PACKAGE_NAME));
    }

    @Test
    public void testValidatePackageName_Invalid_ForbiddenName() {
        assertThat(PlainJavaConventions.validatePackageName("foo.new.bar"),
                hasErrorMessage(PlainJavaConventions.MSG_CODE_INVALID_PACKAGE_NAME));
    }

    @Test
    public void testValidatePackageName_Discouraged_UpperCase() {
        assertThat(PlainJavaConventions.validatePackageName("Foo.Bar"),
                hasWarningMessage(PlainJavaConventions.MSG_CODE_DISCOURAGED_PACKAGE_NAME));
    }

    @Test
    public void testValidateTypeName_Valid_Unqualified() {
        assertThat(PlainJavaConventions.validateTypeName("Foo"), isEmpty());
    }

    @Test
    public void testValidateTypeName_Valid_Qualified() {
        assertThat(PlainJavaConventions.validateTypeName("foo.bar.Baz"), isEmpty());
    }

    @Test
    public void testValidateTypeName_Valid_NonAscii() {
        assertThat(PlainJavaConventions.validateTypeName("foo.bar.Ä_µß"), isEmpty());
    }

    @Test
    public void testValidateTypeName_Invalid_IllegalPackage() {
        assertThat(PlainJavaConventions.validateTypeName("foo..Bar"),
                hasErrorMessage(PlainJavaConventions.MSG_CODE_INVALID_PACKAGE_NAME));
    }

    @Test
    public void testValidateTypeName_Discouraged_UpperCasePackage() {
        assertThat(PlainJavaConventions.validateTypeName("foo.Bar.baz"),
                hasWarningMessage(PlainJavaConventions.MSG_CODE_DISCOURAGED_PACKAGE_NAME));
    }

    @Test
    public void testValidateTypeName_Invalid_LeadingDigit() {
        assertThat(PlainJavaConventions.validateTypeName("1foo"),
                hasErrorMessage(PlainJavaConventions.MSG_CODE_INVALID_TYPE_NAME));
    }

    @Test
    public void testValidateTypeName_Invalid_NonJavaCharacter() {
        assertThat(PlainJavaConventions.validateTypeName("Error@Test"),
                hasErrorMessage(PlainJavaConventions.MSG_CODE_INVALID_TYPE_NAME));
    }

    @Test
    public void testValidateTypeName_Discouraged_LeadingLowerCase() {
        assertThat(PlainJavaConventions.validateTypeName("foo"),
                hasWarningMessage(PlainJavaConventions.MSG_CODE_DISCOURAGED_TYPE_NAME));
    }

    @Test
    public void testValidateTypeName_Discouraged_Leading$() {
        assertThat(PlainJavaConventions.validateTypeName("$Foo"),
                hasWarningMessage(PlainJavaConventions.MSG_CODE_DISCOURAGED_TYPE_NAME));
    }
}
