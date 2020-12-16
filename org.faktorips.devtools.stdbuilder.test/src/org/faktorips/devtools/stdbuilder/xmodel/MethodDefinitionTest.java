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

import org.junit.Test;

public class MethodDefinitionTest {

    @Test
    public void testGetTypeSignatures_noParam() throws Exception {
        MethodDefinition methodSignature = new MethodDefinition("testName");
        assertEquals(0, methodSignature.getTypeSignatures().length);
    }

    @Test
    public void testGetTypeSignatures_oneParam() throws Exception {
        MethodParameter parameter = new MethodParameter("paramType", "paramName");
        MethodDefinition methodSignature = new MethodDefinition("testName", parameter);
        String[] parameterTypes = methodSignature.getTypeSignatures();
        assertEquals(1, parameterTypes.length);
        assertEquals(parameter.getTypeSignature(), parameterTypes[0]);
    }

    @Test
    public void testGetTypeSignatures_twoParam() throws Exception {
        MethodParameter parameter1 = new MethodParameter("paramType", "paramName");
        MethodParameter parameter2 = new MethodParameter("paramType", "paramName");
        MethodDefinition methodSignature = new MethodDefinition("testName", parameter1, parameter2);
        String[] parameterTypes = methodSignature.getTypeSignatures();
        assertEquals(2, parameterTypes.length);
        assertEquals(parameter1.getTypeSignature(), parameterTypes[0]);
        assertEquals(parameter2.getTypeSignature(), parameterTypes[1]);
    }

    @Test
    public void testGetDefinition() throws Exception {
        MethodParameter parameter1 = new MethodParameter("paramType", "paramName");
        MethodParameter parameter2 = new MethodParameter("paramType", "paramName");
        MethodDefinition methodSignature = new MethodDefinition("testName", parameter1, parameter2);
        String definition = methodSignature.getDefinition();
        assertEquals("testName(paramType paramName, paramType paramName)", definition);
    }

    @Test
    public void testGetDefinition_HasFinalFlag() throws Exception {
        MethodParameter parameter1 = new MethodParameter("paramType", "paramName", true);
        MethodParameter parameter2 = new MethodParameter("paramType", "paramName", true);
        MethodDefinition methodSignature = new MethodDefinition("testName", parameter1, parameter2);
        String definition = methodSignature.getDefinition();
        System.out.println(definition);
        assertEquals("testName(final paramType paramName, final paramType paramName)", definition);
    }
}
