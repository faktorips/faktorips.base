/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.codegen;

import java.lang.reflect.Modifier;

import junit.framework.TestCase;

import org.apache.commons.lang.SystemUtils;

public class JavaCodeFragmentBuilderTest extends TestCase {

    public final void testMethodBeginIntStringStringStringArrayStringArray() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.methodBegin(Modifier.PUBLIC, String[].class, "validate", new String[0], new Class[0]);
        StringBuffer buf = new StringBuffer();
        buf.append("public String[] validate()");
        buf.append(SystemUtils.LINE_SEPARATOR);
        buf.append("{");
        assertEquals(buf.toString(), builder.toString().trim());
    }

}
