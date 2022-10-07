/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.util;

import static org.faktorips.testsupport.IpsMatchers.hasErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.hasWarningMessage;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class JavaConventionsTest {

    @Test
    public void testValidatePackageName_Valid_ThisPackage() {
        assertThat(JavaConventions.validatePackageName(getClass().getPackageName()), isEmpty());
    }

    @Test
    public void testValidatePackageName_Valid_Single() {
        assertThat(JavaConventions.validatePackageName("test"), isEmpty()); //$NON-NLS-1$
    }

    @Test
    public void testValidatePackageName_Invalid_IllegalCharacter() {
        assertThat(JavaConventions.validatePackageName("foo.§bar"), //$NON-NLS-1$
                hasErrorMessage(JavaConventions.MSG_CODE_INVALID_PACKAGE_NAME));
    }

    @Test
    public void testValidatePackageName_Invalid_DoublePeriod() {
        assertThat(JavaConventions.validatePackageName("foo..bar"), //$NON-NLS-1$
                hasErrorMessage(JavaConventions.MSG_CODE_INVALID_PACKAGE_NAME));
    }

    @Test
    public void testValidatePackageName_Invalid_Empty() {
        assertThat(JavaConventions.validatePackageName(""), //$NON-NLS-1$
                hasErrorMessage(JavaConventions.MSG_CODE_INVALID_PACKAGE_NAME));
    }

    @Test
    public void testValidatePackageName_Invalid_ForbiddenName() {
        assertThat(JavaConventions.validatePackageName("foo.new.bar"), //$NON-NLS-1$
                hasErrorMessage(JavaConventions.MSG_CODE_INVALID_PACKAGE_NAME));
    }

    @Test
    public void testValidatePackageName_Discouraged_UpperCase() {
        assertThat(JavaConventions.validatePackageName("Foo.Bar"), //$NON-NLS-1$
                hasWarningMessage(JavaConventions.MSG_CODE_DISCOURAGED_PACKAGE_NAME));
    }

    @Test
    public void testValidateTypeName_Valid_Unqualified() {
        assertThat(JavaConventions.validateTypeName("Foo"), isEmpty()); //$NON-NLS-1$
    }

    @Test
    public void testValidateTypeName_Valid_Qualified() {
        assertThat(JavaConventions.validateTypeName("foo.bar.Baz"), isEmpty()); //$NON-NLS-1$
    }

    @Test
    public void testValidateTypeName_Valid_NonAscii() {
        assertThat(JavaConventions.validateTypeName("foo.bar.Ä_µß"), isEmpty()); //$NON-NLS-1$
    }

    @Test
    public void testValidateTypeName_Invalid_IllegalPackage() {
        assertThat(JavaConventions.validateTypeName("foo..Bar"), //$NON-NLS-1$
                hasErrorMessage(JavaConventions.MSG_CODE_INVALID_PACKAGE_NAME));
    }

    @Test
    public void testValidateTypeName_Discouraged_UpperCasePackage() {
        assertThat(JavaConventions.validateTypeName("foo.Bar.baz"), //$NON-NLS-1$
                hasWarningMessage(JavaConventions.MSG_CODE_DISCOURAGED_PACKAGE_NAME));
    }

    @Test
    public void testValidateTypeName_Invalid_LeadingDigit() {
        assertThat(JavaConventions.validateTypeName("1foo"), //$NON-NLS-1$
                hasErrorMessage(JavaConventions.MSG_CODE_INVALID_TYPE_NAME));
    }

    @Test
    public void testValidateTypeName_Invalid_NonJavaCharacter() {
        assertThat(JavaConventions.validateTypeName("Error@Test"), //$NON-NLS-1$
                hasErrorMessage(JavaConventions.MSG_CODE_INVALID_TYPE_NAME));
    }

    @Test
    public void testValidateTypeName_Discouraged_LeadingLowerCase() {
        assertThat(JavaConventions.validateTypeName("foo"), //$NON-NLS-1$
                hasWarningMessage(JavaConventions.MSG_CODE_DISCOURAGED_TYPE_NAME));
    }

    @Test
    public void testValidateTypeName_Discouraged_Leading$() {
        assertThat(JavaConventions.validateTypeName("$Foo"), //$NON-NLS-1$
                hasWarningMessage(JavaConventions.MSG_CODE_DISCOURAGED_TYPE_NAME));
    }
}
