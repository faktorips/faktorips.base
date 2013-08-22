/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Set;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.functions.If;
import org.faktorips.fl.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ExprCompilerTest {

    protected ExprCompiler<JavaCodeFragment> compiler;

    @Before
    public void setUp() throws Exception {
        compiler = new ExprCompiler<JavaCodeFragment>() {

            @Override
            protected void registerDefaults() {
                // no defaults
            }

            @Override
            public CompilationResult<JavaCodeFragment> compile(String expr) {
                // don't know how without a target language
                return null;
            }

            @Override
            protected CompilationResult<JavaCodeFragment> parseExceptionToResult(ParseException e) {
                // don't know how without a target language
                return null;
            }

        };
        Locale.setDefault(Locale.ENGLISH);
        compiler.setLocale(Locale.ENGLISH);
    }

    @Test
    public void testGetFunctions() {
        DefaultFunctionResolver<JavaCodeFragment> r1 = new DefaultFunctionResolver<JavaCodeFragment>();
        FlFunction<JavaCodeFragment> f1 = new If("IF1", "");
        r1.add(f1);
        DefaultFunctionResolver<JavaCodeFragment> r2 = new DefaultFunctionResolver<JavaCodeFragment>();
        FlFunction<JavaCodeFragment> f2 = new If("IF2", "");
        FlFunction<JavaCodeFragment> f3 = new If("IF3", "");
        r2.add(f2);
        r2.add(f3);
        FlFunction<JavaCodeFragment>[] functions = compiler.getFunctions();
        assertEquals(0, functions.length);
        compiler.add(r1);
        compiler.add(r2);
        functions = compiler.getFunctions();
        assertEquals(3, functions.length);
        assertEquals(f1, functions[0]);
        assertEquals(f2, functions[1]);
        assertEquals(f3, functions[2]);
    }

    @Test
    public void testIsValidIdentifier() {
        assertTrue(ExprCompiler.isValidIdentifier("a"));
        assertTrue(ExprCompiler.isValidIdentifier("a.a"));
        assertFalse(ExprCompiler.isValidIdentifier("/"));
        assertFalse(ExprCompiler.isValidIdentifier("true"));
        assertFalse(ExprCompiler.isValidIdentifier("-a"));
        assertFalse(ExprCompiler.isValidIdentifier("a-a"));
        assertFalse(ExprCompiler.isValidIdentifier("9"));
        assertTrue(ExprCompiler.isValidIdentifier("a9"));
    }

    @Test
    public void testGetAmbiguousFunctions() {

        Datatype[] argTypesDecimal = { Datatype.DECIMAL };
        Datatype[] argTypesString = { Datatype.STRING };

        String matchingFunctionName = "functionMatchingName";

        FlFunction<JavaCodeFragment> function1 = createFunction("function1", argTypesDecimal);
        FlFunction<JavaCodeFragment> function2 = createFunction(matchingFunctionName, argTypesDecimal);
        FlFunction<JavaCodeFragment> function3 = createFunction(matchingFunctionName, argTypesDecimal);
        FlFunction<JavaCodeFragment> function4 = createFunction(matchingFunctionName, argTypesString);
        FlFunction<JavaCodeFragment> function5 = createFunction("function5", argTypesDecimal);

        matchingFunctions(function2, function3);

        @SuppressWarnings("unchecked")
        FunctionResolver<JavaCodeFragment> fctResolver1 = createFunctionResolver(function1, function2);
        @SuppressWarnings("unchecked")
        FunctionResolver<JavaCodeFragment> fctResolver2 = createFunctionResolver(function3, function4, function5);

        compiler.add(fctResolver1);
        compiler.add(fctResolver2);

        Set<FlFunction<JavaCodeFragment>> ambiguousFunctions = compiler.getAmbiguousFunctions(compiler.getFunctions());

        assertEquals(2, ambiguousFunctions.size());

        assertTrue(ambiguousFunctions.contains(function2));
        assertTrue(ambiguousFunctions.contains(function3));
    }

    private FunctionResolver<JavaCodeFragment> createFunctionResolver(FlFunction<JavaCodeFragment>... functions) {
        @SuppressWarnings("unchecked")
        FunctionResolver<JavaCodeFragment> newFctResolver = mock(FunctionResolver.class);

        when(newFctResolver.getFunctions()).thenReturn(functions);

        return newFctResolver;
    }

    private FlFunction<JavaCodeFragment> createFunction(String name, Datatype[] argTypes) {
        @SuppressWarnings("unchecked")
        FlFunction<JavaCodeFragment> newFunction = mock(FlFunction.class);

        when(newFunction.getName()).thenReturn(name);
        when(newFunction.getArgTypes()).thenReturn(argTypes);
        when(newFunction.getType()).thenReturn(Datatype.INTEGER);

        return newFunction;
    }

    private void matchingFunctions(FlFunction<JavaCodeFragment> matchingFunction,
            FlFunction<JavaCodeFragment> newFunction) {
        when(newFunction.isSame(matchingFunction)).thenReturn(true);
        when(matchingFunction.isSame(newFunction)).thenReturn(true);

        Datatype[] matchingArgTypes = matchingFunction.getArgTypes();
        String matchingName = matchingFunction.getName();
        when(newFunction.match(eq(matchingName), aryEq(matchingArgTypes))).thenReturn(true);

        String name = newFunction.getName();
        Datatype[] argTypes = newFunction.getArgTypes();
        when(matchingFunction.match(eq(name), aryEq(argTypes))).thenReturn(true);
    }
}
