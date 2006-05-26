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

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;

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
    
}
