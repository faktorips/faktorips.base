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

package org.faktorips.runtime.pds.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.runtime.pds.AbstractFormulaEvaluator;
import org.faktorips.runtime.pds.IFormulaEvaluator;
import org.faktorips.runtime.pds.IFormulaEvaluatorBuilder;

public class GroovyEvaluator extends AbstractFormulaEvaluator {

    public static final String THIS_CLASS_VAR = "thisClass"; //$NON-NLS-1$

    private GroovyShell groovyShell;

    private Map<String, Script> scriptMap;

    private Binding binding;

    public GroovyEvaluator(Builder builder) {
        super();
        setVariable(THIS_CLASS_VAR, builder.thiz);
        scriptMap = new HashMap<String, Script>(2);
        groovyShell = new GroovyShell(binding);
    }

    @Override
    public void setVariable(String name, Object value) {
        if (binding == null) {
            binding = new Binding();
        }
        binding.setVariable(name, value);
    }

    @Override
    public void parseFormula(String name, String content) {
        groovyShell = new GroovyShell(binding);
        Script script = groovyShell.parse(content);
        scriptMap.put(name, script);
    }

    @Override
    public Object evaluate(String formularName, Object... parameters) {
        return scriptMap.get(formularName).invokeMethod(formularName, parameters);
    }

    public static class Builder implements IFormulaEvaluatorBuilder {

        private Object thiz;

        public void thiz(Object thiz) {
            this.thiz = thiz;
        }

        public IFormulaEvaluator build() {
            if (thiz == null) {
                throw new IllegalStateException("The variable thiz have to be set");
            }
            return new GroovyEvaluator(this);
        }

    }

}
