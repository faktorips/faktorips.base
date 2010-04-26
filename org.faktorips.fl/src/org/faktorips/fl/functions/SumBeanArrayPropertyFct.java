/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl.functions;

import java.lang.reflect.Method;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BeanDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.PropertyDatatype;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.values.Decimal;

/**
 * A function that given an array of objects from the same class or interface allows to sum up one
 * of the properties. E.g.: sum(coverages, netPremium) where coverages is an array of coverage
 * objects with a property netPremium.
 * 
 * @author Jan Ortmann
 */
public class SumBeanArrayPropertyFct extends AbstractFlFunction {

    public final static String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "SUM-BEAN-ARRAY-PROPERTYFCT"; //$NON-NLS-1$

    public SumBeanArrayPropertyFct() {
        super("SUM", "", AnyDatatype.INSTANCE, new Datatype[] { AnyDatatype.INSTANCE, AnyDatatype.INSTANCE }); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Overridden Method.
     * 
     * @see org.faktorips.fl.FlFunction#compile(org.faktorips.fl.CompilationResult[])
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        ArgumentCheck.notNull(argResults[0]);
        ArgumentCheck.notNull(argResults[1]);
        ArrayOfValueDatatype array = (ArrayOfValueDatatype)argResults[0].getDatatype();
        BeanDatatype bean = (BeanDatatype)array.getBasicDatatype();
        PropertyDatatype property = (PropertyDatatype)argResults[1].getDatatype();
        if (bean.getProperty(property.getName()) == null) {
            String text = Messages.INSTANCE.getString(ERROR_MESSAGE_CODE, new Object[] { bean, property });
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

    public final static Decimal sumDecimal(Object[] beans, String getterMethod) {
        if (beans == null) {
            return Decimal.NULL;
        }
        if (beans.length == 0) {
            return Decimal.ZERO;
        }
        try {
            Decimal sum = Decimal.ZERO;
            Method method = beans[0].getClass().getMethod(getterMethod, new Class[0]);
            Object[] params = new Object[0];
            for (Object bean : beans) {
                Decimal value = (Decimal)method.invoke(bean, params);
                sum = sum.add(value);
            }
            return sum;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
