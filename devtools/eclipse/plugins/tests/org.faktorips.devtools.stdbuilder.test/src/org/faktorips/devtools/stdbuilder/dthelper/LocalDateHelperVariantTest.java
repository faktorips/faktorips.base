/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.dthelper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class LocalDateHelperVariantTest {

    @Test
    public void testFromString() {
        assertThat(LocalDateHelperVariant.fromString(null), is(LocalDateHelperVariant.JODA));
        assertThat(LocalDateHelperVariant.fromString(""), is(LocalDateHelperVariant.JODA));
        assertThat(LocalDateHelperVariant.fromString("bla"), is(LocalDateHelperVariant.JODA));
        assertThat(LocalDateHelperVariant.fromString("joda"), is(LocalDateHelperVariant.JODA));
        assertThat(LocalDateHelperVariant.fromString("JODA"), is(LocalDateHelperVariant.JODA));
        assertThat(LocalDateHelperVariant.fromString("java8"), is(LocalDateHelperVariant.JAVA8));
        assertThat(LocalDateHelperVariant.fromString("JAVA8"), is(LocalDateHelperVariant.JAVA8));
    }

}
