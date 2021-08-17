/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers.java8;

import static org.junit.Assert.assertEquals;

import org.faktorips.codegen.JavaCodeFragment;
import org.junit.Test;

public class ParseHelperTest {

    @Test
    public void testParse() throws Exception {
        JavaCodeFragment fragment = ParseHelper.parse("expression", "className");
        assertEquals("className.parse(expression)", fragment.getSourcecode());
    }

}
