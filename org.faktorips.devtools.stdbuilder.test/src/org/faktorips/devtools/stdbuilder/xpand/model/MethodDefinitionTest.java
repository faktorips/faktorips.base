/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MethodDefinitionTest {

    @Test
    public void testGetrTypeSignatures_noParam() throws Exception {
        MethodDefinition methodSignature = new MethodDefinition("testName");
        assertEquals(0, methodSignature.getrTypeSignatures().length);
    }

    @Test
    public void testGetrTypeSignatures_oneParam() throws Exception {
        MethodParameter parameter = new MethodParameter("paramType", "paramName");
        MethodDefinition methodSignature = new MethodDefinition("testName", parameter);
        String[] parameterTypes = methodSignature.getrTypeSignatures();
        assertEquals(1, parameterTypes.length);
        assertEquals(parameter.getTypeSignature(), parameterTypes[0]);
    }

    @Test
    public void testGetrTypeSignatures_twoParam() throws Exception {
        MethodParameter parameter1 = new MethodParameter("paramType", "paramName");
        MethodParameter parameter2 = new MethodParameter("paramType", "paramName");
        MethodDefinition methodSignature = new MethodDefinition("testName", parameter1, parameter2);
        String[] parameterTypes = methodSignature.getrTypeSignatures();
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
}
