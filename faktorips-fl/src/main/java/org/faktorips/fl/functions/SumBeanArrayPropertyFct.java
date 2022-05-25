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

import java.lang.reflect.Method;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BeanDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.fl.PropertyDatatype;
import org.faktorips.runtime.Message;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.values.Decimal;

/**
 * A function that given an array of objects from the same class or interface allows to sum up one
 * of the properties. E.g.: sum(coverages, netPremium) where coverages is an array of coverage
 * objects with a property netPremium.
 * 
 * @author Jan Ortmann
 */
public class SumBeanArrayPropertyFct extends AbstractFlFunction {

    public static final String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "SUM-BEAN-ARRAY-PROPERTYFCT"; //$NON-NLS-1$

    public SumBeanArrayPropertyFct() {
        super("SUM", "", FunctionSignatures.SumBeanArrayPropertyFct); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Overridden Method.
     * 
     * @see org.faktorips.fl.FlFunction#compile(org.faktorips.fl.CompilationResult[])
     */
    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.notNull(argResults[0]);
        ArgumentCheck.notNull(argResults[1]);
        ArrayOfValueDatatype array = (ArrayOfValueDatatype)argResults[0].getDatatype();
        BeanDatatype bean = (BeanDatatype)array.getBasicDatatype();
        PropertyDatatype property = (PropertyDatatype)argResults[1].getDatatype();
        if (bean.getProperty(property.getName()) == null) {
            String text = Messages.INSTANCE.getString(ERROR_MESSAGE_CODE, bean, property);
            Message msg = Message.newError(ERROR_MESSAGE_CODE, text);
            return new CompilationResultImpl(msg);
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(SumBeanArrayPropertyFct.class);
        fragment.append('.');
        fragment.append("sumDecimal("); //$NON-NLS-1$
        fragment.append(argResults[0].getCodeFragment());
        fragment.append(", "); //$NON-NLS-1$
        fragment.appendQuoted(property.getGetterMethod());
        fragment.append(")"); //$NON-NLS-1$
        CompilationResultImpl result = new CompilationResultImpl(fragment, Datatype.DECIMAL);
        result.addMessages(argResults[0].getMessages());
        result.addMessages(argResults[1].getMessages());
        return result;
    }

    public static final Decimal sumDecimal(Object[] beans, String getterMethod) {
        if (beans == null) {
            return Decimal.NULL;
        }
        if (beans.length == 0) {
            return Decimal.ZERO;
        }
        try {
            Decimal sum = Decimal.ZERO;
            Method method = beans[0].getClass().getMethod(getterMethod);
            Object[] params = new Object[0];
            for (Object bean : beans) {
                Decimal value = (Decimal)method.invoke(bean, params);
                sum = sum.add(value);
            }
            return sum;
            // CSOFF: Illegal Catch
        } catch (Exception e) {
            // CSON: Illegal Catch
            throw new RuntimeException(e);
        }
    }

}
