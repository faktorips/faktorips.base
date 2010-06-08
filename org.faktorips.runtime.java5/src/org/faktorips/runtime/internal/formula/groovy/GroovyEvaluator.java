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

package org.faktorips.runtime.internal.formula.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.List;

import org.faktorips.runtime.internal.formula.AbstractFormulaEvaluator;

public class GroovyEvaluator extends AbstractFormulaEvaluator {

    public static final String THIS_CLASS_VAR = "thiz"; //$NON-NLS-1$

    private Binding binding = new Binding();

    private Script groovyScript;

    public GroovyEvaluator(Object thiz, List<String> compiledExpressions) {
        super(thiz);
        setVariable(THIS_CLASS_VAR, thiz);
        GroovyShell groovyShell = new GroovyShell(binding);
        String sourceCode = getSourceCode(compiledExpressions);
        groovyScript = groovyShell.parse(sourceCode);
    }

    private String getSourceCode(List<String> formulaList) {
        StringBuilder sourceCode = new StringBuilder();
        for (String formula : formulaList) {
            formula = formula.replaceAll("this", THIS_CLASS_VAR);
            sourceCode.append(formula).append('\n');
        }
        return sourceCode.toString();
    }

    @Override
    public void setVariable(String name, Object value) {
        binding.setVariable(name, value);
    }

    @Override
    protected Object evaluateInternal(String formularName, Object... parameters) {
        return groovyScript.invokeMethod(formularName, parameters);
    }

}
