/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ParserIdentifierAcceptanceTest {

    private JavaExprCompiler compiler;

    @Before
    public void setUp() {
        compiler = new JavaExprCompiler();
        Locale.setDefault(Locale.ENGLISH);
        compiler.setLocale(Locale.ENGLISH);
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        resolver.register("Altersgruppe_1980-01-01", new JavaCodeFragment("Altersgruppe_1980-01-01"), Datatype.STRING);
        resolver.register("Altersgruppe_1980-01", new JavaCodeFragment("Altersgruppe_1980-01"), Datatype.STRING);
        resolver.register("Altersgruppe_1980", new JavaCodeFragment("Altersgruppe_1980"), Datatype.STRING);
        compiler.setIdentifierResolver(resolver);
    }

    @Test
    public void testParserIdentifiers() {
        CompilationResult<JavaCodeFragment> result = compiler.compile("Altersgruppe_1980-01-01");
        assertTrue(result.successfull());

        result = compiler.compile("Altersgruppe_1980-01");
        assertTrue(result.successfull());

        result = compiler.compile("Altersgruppe_1980");
        assertTrue(result.successfull());

        result = compiler.compile("Altersgruppe_1980-01-01-01");
        assertFalse(result.successfull());
    }

    @Test
    public void testParserWithUmlaut() throws Exception {
        compiler = new JavaExprCompiler();
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        resolver.register("ä", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("Ä", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("ü", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("Ü", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("ö", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("Ö", new JavaCodeFragment("a"), Datatype.INTEGER);
        compiler.setIdentifierResolver(resolver);

        CompilationResult<JavaCodeFragment> result = compiler.compile("1 + ä + Ä + ü + Ü + ö + Ö");
        MessageList msgList = result.getMessages();
        assertTrue(msgList.isEmpty());
    }
}
