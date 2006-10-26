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

package org.faktorips.fl;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;

import junit.framework.TestCase;

public class ParserIdentifierAcceptanceTest extends TestCase {

    private ExprCompiler compiler;
    
    public void setUp(){
        compiler = new ExprCompiler();
        Locale.setDefault(Locale.ENGLISH);
        compiler.setLocale(Locale.ENGLISH);
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        resolver.register("Altersgruppe_1980-01-01", new JavaCodeFragment("Altersgruppe_1980-01-01"), Datatype.STRING);
        resolver.register("Altersgruppe_1980-01", new JavaCodeFragment("Altersgruppe_1980-01"), Datatype.STRING);
        resolver.register("Altersgruppe_1980", new JavaCodeFragment("Altersgruppe_1980"), Datatype.STRING);
        compiler.setIdentifierResolver(resolver);
    }
    
    public void testParserIdentifiers(){
        CompilationResult result = compiler.compile("Altersgruppe_1980-01-01");
        assertTrue(result.successfull());
        
        result = compiler.compile("Altersgruppe_1980-01");
        assertTrue(result.successfull());

        result = compiler.compile("Altersgruppe_1980");
        assertTrue(result.successfull());

        result = compiler.compile("Altersgruppe_1980-01-01-01");
        assertFalse(result.successfull());
    }
    
    public void testParserWithUmlaut() throws UnsupportedEncodingException{
        compiler = new ExprCompiler();
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        resolver.register(new String("ä".getBytes("UTF-8"), "UTF-8"), new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register(new String("Ä".getBytes("UTF-8"), "UTF-8"), new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register(new String("ü".getBytes("UTF-8"), "UTF-8"), new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register(new String("Ü".getBytes("UTF-8"), "UTF-8"), new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register(new String("ö".getBytes("UTF-8"), "UTF-8"), new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register(new String("Ö".getBytes("UTF-8"), "UTF-8"), new JavaCodeFragment("a"), Datatype.INTEGER);
        compiler.setIdentifierResolver(resolver);

        CompilationResult result;
            result = compiler.compile(new String("1 + ä + Ä + ü + Ü + ö + Ö".getBytes("UTF-8"), "UTF-8"));
            MessageList msgList = result.getMessages();
            assertTrue(msgList.isEmpty());
    }
}
