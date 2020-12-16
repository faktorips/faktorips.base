/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.core.Signature;
import org.junit.Test;

public class MethodParameterTest {

    @Test
    public void testGetDefinition() throws Exception {
        MethodParameter methodParameter = new MethodParameter("testtype", "param");
        assertEquals("testtype param", methodParameter.getDefinition());
    }

    @Test
    public void testGetTypeSignature() throws Exception {
        MethodParameter methodParameter = new MethodParameter("testtype", "param");
        assertEquals(Signature.createTypeSignature("testtype", false), methodParameter.getTypeSignature());
    }

    @Test
    public void testGetTypeSignature_HasFinalFlag() throws Exception {
        MethodParameter methodParameter = new MethodParameter("testtype", "param", true);
        assertEquals("final testtype param", methodParameter.getDefinition());
        assertEquals("param", methodParameter.getName());
        assertEquals("testtype", methodParameter.getType());
    }
}
