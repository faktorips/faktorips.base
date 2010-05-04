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

package org.faktorips.runtime.pds;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.internal.TocEntryGeneration;
import org.faktorips.runtime.internal.formula.FormulaEvaluatorBuilderFactory;
import org.faktorips.runtime.internal.formula.IFormulaEvaluatorBuilder;
import org.faktorips.runtime.internal.formula.groovy.GroovyEvaluator;

public class PdsRuntimeRepository extends ClassloaderRuntimeRepository {

    public PdsRuntimeRepository(ClassLoader cl, String basePackage) throws ParserConfigurationException {
        super(cl, basePackage);
    }

    private IFormulaEvaluatorBuilder formulaEvaluator;

    @Override
    public IFormulaEvaluatorBuilder getFormulaEvaluatorBuilder() {
        // TODO load classname by property?
        if (formulaEvaluator == null) {
            try {
                formulaEvaluator = FormulaEvaluatorBuilderFactory.createFormulaEvaluatorBuilder(getClassLoader(),
                        GroovyEvaluator.Builder.class.getName());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return formulaEvaluator;
    }

    @Override
    protected String getProductComponentGenerationImplClass(TocEntryGeneration tocEntry) {
        return tocEntry.getParent().getGenerationImplClassName();
    }

}
