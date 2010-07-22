/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.fl.functions;

import java.util.Locale;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;

/**
 * 
 * @author Jan Ortmann
 */
public class NotTest extends FunctionAbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        compiler.setEnsureResultIsObject(false);
        registerFunction(new Not("NOT", ""));
        registerFunction(new NotBoolean("NOT", ""));
        compiler.setIdentifierResolver(new IdentifierResolver() {

            public CompilationResult compile(String identifier, ExprCompiler exprCompiler, Locale locale) {
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

    public void testPrimitiveBoolean() throws Exception {
        execAndTestSuccessfull("NOT(FALSE)", true);
        execAndTestSuccessfull("NOT(TRUE)", false);
    }

    public void testBoolean() throws Exception {
        execAndTestSuccessfull("NOT( beTrue )", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("NOT( beFalse )", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("NOT( beNull )", null, Datatype.BOOLEAN);
    }

}
