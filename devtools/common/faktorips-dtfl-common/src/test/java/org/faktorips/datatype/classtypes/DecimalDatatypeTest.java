/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class DecimalDatatypeTest {

    @Test
    public void testCompare() {
        DecimalDatatype datatype = new DecimalDatatype();
        assertThat(datatype.compare(null, null), is(0));
        assertThat(datatype.compare(null, ""), is(0));
        assertThat(datatype.compare(null, "1.23"), is(-1));
        assertThat(datatype.compare("1.0", null), is(1));
        assertThat(datatype.compare("1.0", "1.01"), is(-1));
        assertThat(datatype.compare("2", "1.99"), is(1));
    }

}
