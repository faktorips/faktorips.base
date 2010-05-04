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

public class FormulaEvaluatorBuilderFactory {

    public static IFormulaEvaluatorBuilder createFormulaEvaluatorBuilder(ClassLoader classLoader, String className)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = classLoader.loadClass(className);
        if (IFormulaEvaluatorBuilder.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            Class<? extends IFormulaEvaluatorBuilder> builderClass = (Class<? extends IFormulaEvaluatorBuilder>)clazz;
            Constructor<? extends IFormulaEvaluatorBuilder> constructor = builderClass.getConstructor();
            IFormulaEvaluatorBuilder builder = constructor.newInstance();
            return builder;
        }
        throw new IllegalArgumentException("Class " + className + " does not implements "
                + IFormulaEvaluatorBuilder.class.getName());
    }

}
