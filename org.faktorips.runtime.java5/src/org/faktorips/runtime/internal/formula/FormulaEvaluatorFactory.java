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

package org.faktorips.runtime.internal.formula;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class FormulaEvaluatorFactory {

    private Class<?> clazz;

    public FormulaEvaluatorFactory(ClassLoader classLoader, String className) throws ClassNotFoundException {
        clazz = classLoader.loadClass(className);
    }

    public IFormulaEvaluator createFormulaEvaluatorBuilder(Object thiz, List<String> compiledExpressions)
            throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        if (IFormulaEvaluator.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            Class<? extends IFormulaEvaluator> evaluatorClass = (Class<? extends IFormulaEvaluator>)clazz;
            Constructor<? extends IFormulaEvaluator> constructor = evaluatorClass.getConstructor(Object.class,
                    List.class);
            IFormulaEvaluator evaluator = constructor.newInstance(thiz, compiledExpressions);
            return evaluator;
        }
        throw new IllegalArgumentException("Class " + clazz.getName() + " does not implements "
                + IFormulaEvaluator.class.getName());
    }

}
