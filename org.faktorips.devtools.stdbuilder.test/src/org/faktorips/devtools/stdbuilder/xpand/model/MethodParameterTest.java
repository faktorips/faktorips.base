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

}
