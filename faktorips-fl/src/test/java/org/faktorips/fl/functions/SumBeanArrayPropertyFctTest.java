/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.values.Decimal;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class SumBeanArrayPropertyFctTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void test() throws Exception {
        registerFunction(new SumBeanArrayPropertyFct());
        getCompiler().setIdentifierResolver(new BeanIdentifierResolver());
        execAndTestSuccessfull("SUM(beans; value)", Decimal.valueOf("42"), Datatype.DECIMAL);
    }

    @Test
    public void testSumDecimal() {
        SimpleBean bean1 = new SimpleBean();
        bean1.setValue(Decimal.valueOf(10, 0));
        SimpleBean bean2 = new SimpleBean();
        bean2.setValue(Decimal.valueOf(32, 0));
        Decimal sum = SumBeanArrayPropertyFct.sumDecimal(new Object[] { bean1, bean2 }, "getValue");
        assertEquals(Decimal.valueOf(42, 0), sum);
    }

    class BeanIdentifierResolver implements IdentifierResolver<JavaCodeFragment> {

        @Override
        public CompilationResult<JavaCodeFragment> compile(String identifier,
                ExprCompiler<JavaCodeFragment> exprCompiler,
                Locale locale) {
            if (identifier.equals("beans")) {
                return getResultForBeans();
            } else if (identifier.equals("value")) {
                return new CompilationResultImpl("", new TestPropertyDatatype("value", Datatype.DECIMAL));
            }
            return null;
        }

        private CompilationResult<JavaCodeFragment> getResultForBeans() {
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment.append("new Object[]{");
            fragment.append("new ");
            fragment.appendClassName(SimpleBean.class);
            fragment.append('(');
            fragment.appendClassName(Decimal.class);
            fragment.append(".valueOf(10, 0)), ");
            fragment.append("new ");
            fragment.appendClassName(SimpleBean.class);
            fragment.append('(');
            fragment.appendClassName(Decimal.class);
            fragment.append(".valueOf(32, 0))");
            fragment.append("}");
            TestBeanDatatype beanDatatype = new TestBeanDatatype(SimpleBean.class.getName());
            beanDatatype.add(new TestPropertyDatatype("value", Datatype.DECIMAL));
            return new CompilationResultImpl(fragment, new ArrayOfValueDatatype(beanDatatype, 1));
        }
    }
}
