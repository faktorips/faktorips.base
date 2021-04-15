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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.functions.AbstractFlFunction;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class DefaultFunctionResolverTest {

    private DefaultFunctionResolver<JavaCodeFragment> resolver;

    @Before
    public void setUp() throws Exception {
        resolver = new DefaultFunctionResolver<>();
    }

    @Test
    public void testAdd() {
        FlFunction<JavaCodeFragment> fct = new AbstractTestFlFunction("fct1", Datatype.DECIMAL, new Datatype[0]);
        resolver.add(fct);
        assertEquals(1, resolver.getFunctions().length);
        assertSame(fct, resolver.getFunctions()[0]);
    }

    @Test
    public void testRemove() {
        AbstractTestFlFunction fct1 = new AbstractTestFlFunction("fct1", Datatype.DECIMAL, new Datatype[0]);
        resolver.add(fct1);
        resolver.remove(fct1);
        assertEquals(0, resolver.getFunctions().length);

        resolver.remove(fct1); // should do nothing
    }

    static class AbstractTestFlFunction extends AbstractFlFunction {

        // result to be returned.
        private CompilationResult<JavaCodeFragment> result;

        AbstractTestFlFunction(String name, Datatype type, Datatype[] argTypes) {
            super(name, "", type, argTypes);
        }

        @Override
        public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
            return result;
        }
    }
}
