/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import static org.junit.Assert.assertEquals;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.junit.Before;
import org.junit.Test;

public class StringHelperTest {

    private StringHelper helper;

    @Before
    public void setUp() {
        helper = new StringHelper(Datatype.STRING);
    }

    @Test
    public void testDoNotEscapeForwardSlash() {
        JavaCodeFragment fragment = helper.newInstance("/"); //$NON-NLS-1$
        assertEquals("\"/\"", fragment.getSourcecode()); //$NON-NLS-1$
    }

}
