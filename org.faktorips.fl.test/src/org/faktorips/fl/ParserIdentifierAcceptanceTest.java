/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ParserIdentifierAcceptanceTest {

    private ExprCompiler compiler;

    @Before
    public void setUp() {
        compiler = new ExprCompiler();
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
        CompilationResult result = compiler.compile("Altersgruppe_1980-01-01");
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
        compiler = new ExprCompiler();
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        resolver.register("ä", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("Ä", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("ü", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("Ü", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("ö", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("Ö", new JavaCodeFragment("a"), Datatype.INTEGER);
        compiler.setIdentifierResolver(resolver);

        CompilationResult result = compiler.compile("1 + ä + Ä + ü + Ü + ö + Ö");
        MessageList msgList = result.getMessages();
        assertTrue(msgList.isEmpty());
    }
}
