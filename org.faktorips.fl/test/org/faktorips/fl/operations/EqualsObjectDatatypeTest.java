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

package org.faktorips.fl.operations;

import java.util.Locale;

import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.CompilerAbstractTest;
import org.faktorips.fl.IdentifierResolver;

/**
 * 
 * @author Jan Ortmann
 */
public class EqualsObjectDatatypeTest extends CompilerAbstractTest {

    protected void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(new BinaryOperation[]{new EqualsObjectDatatype(AnyDatatype.INSTANCE)});
        compiler.setIdentifierResolver(new IdentifierResolver() {

            public CompilationResult compile(String identifier, Locale locale) {
                if (identifier.equals("beTrue")) {
                    return new CompilationResultImpl("Boolean.TRUE", Datatype.BOOLEAN);
                } else if (identifier.equals("beFalse")) {
                    return new CompilationResultImpl("Boolean.FALSE", Datatype.BOOLEAN);
                } else {
                    return new CompilationResultImpl("null", Datatype.BOOLEAN);
                }
            }
            
        });
    }
    
    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("beTrue=beTrue", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("beFalse=beFalse", Boolean.TRUE, Datatype.BOOLEAN);
    }

}
