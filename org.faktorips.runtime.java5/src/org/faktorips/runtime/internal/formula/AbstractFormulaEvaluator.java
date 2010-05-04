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

import java.util.HashMap;
import java.util.Map;

import org.faktorips.runtime.FormulaExecutionException;
import org.w3c.dom.Element;

/**
 * An abstract implementation of {@link IFormulaEvaluator}. This implementation handles the
 * expression encapsulated in an {@link Element} and forwarding the expression as String. T
 * 
 * @author dirmeier
 */
public abstract class AbstractFormulaEvaluator implements IFormulaEvaluator {

    private Map<String, String> formulaExpressionMap = new HashMap<String, String>(1);
    private final Object thiz;

    public AbstractFormulaEvaluator(Object thiz) {
        this.thiz = thiz;
    }

    public abstract void setVariable(String name, Object value);

    /**
     * Evaluates a formula that was added through the builder
     * 
     * @throws IllegalArgumentException if the formula signature is unknown
     */
    public Object evaluate(String formularName, Object... parameters) {
        try {
            return evaluateInternal(formularName, parameters);
        } catch (Exception e) {
            StringBuffer parameterValues = new StringBuffer();
            parameterValues.append("Parameters: ");
            for (Object param : parameters) {
                parameterValues.append("" + param);
                if (param != parameters[parameters.length - 1]) {
                    parameterValues.append(", ");
                }
            }
            throw new FormulaExecutionException(thiz.toString(), formulaExpressionMap.get(formularName),
                    parameterValues.toString(), e);
        }

    }

    protected abstract Object evaluateInternal(String formularName, Object... parameters);

}
