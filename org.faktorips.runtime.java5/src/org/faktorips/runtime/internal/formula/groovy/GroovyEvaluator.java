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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.runtime.internal.formula.AbstractFormulaEvaluator;
import org.faktorips.runtime.internal.formula.IFormulaEvaluator;
import org.faktorips.runtime.internal.formula.IFormulaEvaluatorBuilder;

public class GroovyEvaluator extends AbstractFormulaEvaluator {

    public static final String EXPRESSION_XML_TAG = "javaExpression";

    public static final String THIS_CLASS_VAR = "thiz"; //$NON-NLS-1$

    private Binding binding;

    private Script groovyScript;

    private GroovyEvaluator(Builder builder) {
        super(builder.thiz);
        setVariable(THIS_CLASS_VAR, builder.thiz);
        GroovyShell groovyShell = new GroovyShell(binding);
        StringBuilder sourceCode = new StringBuilder();
        for (String formula : builder.formulaList) {
            sourceCode.append(formula).append('\n');
        }
        groovyScript = groovyShell.parse(sourceCode.toString());
    }

    @Override
    public void setVariable(String name, Object value) {
        if (binding == null) {
            binding = new Binding();
        }
        binding.setVariable(name, value);
    }

    @Override
    protected Object evaluateInternal(String formularName, Object... parameters) {
        return groovyScript.invokeMethod(formularName, parameters);
    }

    public static class Builder implements IFormulaEvaluatorBuilder {

        private Object thiz;

        private List<String> formulaList = new ArrayList<String>(1);

        public Builder thiz(Object thiz) {
            this.thiz = thiz;
            return this;
        }

        public IFormulaEvaluator build() {
            if (thiz == null) {
                throw new IllegalStateException("The variable thiz have to be set");
            }
            return new GroovyEvaluator(this);
        }

        public IFormulaEvaluatorBuilder addFormula(String formulaCode) {
            formulaList.add(formulaCode);
            return this;
        }

        public String getExpressionXmlTag() {
            return EXPRESSION_XML_TAG;
        }

    }

}
