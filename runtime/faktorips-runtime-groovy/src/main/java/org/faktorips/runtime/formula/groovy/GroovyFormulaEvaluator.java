/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.formula.groovy;

import java.util.Map;
import java.util.stream.Collectors;

import org.faktorips.runtime.formula.AbstractFormulaEvaluator;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * This formula evaluator evaluates the formulas using groovy. The implementation expects the
 * formula compiled as java code. Each compiled expression should be a complete java method
 * including the method signature. It expects the product component or product component generation
 * in which context the method should run. These are referenced as 'this' within the method. That
 * means the method is expected to run within the context of the product component or product
 * component generation.
 *
 * @author dirmeier
 */
public class GroovyFormulaEvaluator extends AbstractFormulaEvaluator {

    public static final String THIS_CLASS_VAR = "thizProductCmptGeneration"; //$NON-NLS-1$

    private final Binding binding = new Binding();

    private final Script groovyScript;

    /**
     * The constructor expects the product component or product component generation and a list of
     * java method, one for every formula that should be evaluated by this formula evaluator. The
     * methods should be designed to run in the context of the product component generation. The
     * product component generation should be referenced as 'this' within the methods body.
     *
     * @param object the product component or product component generation in which context the
     *            compiled expression methods would run
     * @param nameToExpressionMap a list of compiled expression methods, one for each formula
     */
    public GroovyFormulaEvaluator(Object object, Map<String, String> nameToExpressionMap) {
        super(object, nameToExpressionMap);
        binding.setVariable(THIS_CLASS_VAR, object);
        GroovyShell groovyShell = new GroovyShell(binding);
        String sourceCode = getSourceCode(getNameToExpressionMap());
        groovyScript = groovyShell.parse(sourceCode);
    }

    private String getSourceCode(Map<String, String> expressionMap) {
        return expressionMap.values()
                .stream()
                .map(f -> f.replaceAll("this([\\.\\s])", THIS_CLASS_VAR + "$1"))
                .collect(Collectors.joining("\n"));
    }

    @Override
    protected Object evaluateInternal(String formularName, Object... parameters) {
        return groovyScript.invokeMethod(formularName, parameters);
    }

}
