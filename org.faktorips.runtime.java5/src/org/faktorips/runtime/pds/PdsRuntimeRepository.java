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

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.TocEntryGeneration;
import org.faktorips.runtime.internal.TocEntryObject;
import org.faktorips.runtime.internal.formula.FormulaEvaluatorBuilderFactory;
import org.faktorips.runtime.internal.formula.IFormulaEvaluatorBuilder;
import org.faktorips.runtime.internal.formula.groovy.GroovyEvaluator;
import org.faktorips.runtime.test.IpsTestCaseBase;

public class PdsRuntimeRepository extends ClassloaderRuntimeRepository {

    public PdsRuntimeRepository(ClassLoader cl, String basePackage) throws ParserConfigurationException {
        super(cl, basePackage);
    }

    private IFormulaEvaluatorBuilder formulaEvaluator;

    @Override
    public IFormulaEvaluatorBuilder getFormulaEvaluatorBuilder() {
        // TODO load classname by property
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
    protected <T> List<T> createEnumValues(TocEntryObject tocEntry, Class<T> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IProductComponent createProductCmpt(TocEntryObject tocEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IProductComponentGeneration createProductCmptGeneration(TocEntryGeneration tocEntryGeneration) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ITable createTable(TocEntryObject tocEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IpsTestCaseBase createTestCase(TocEntryObject tocEntry, IRuntimeRepository runtimeRepository) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected AbstractReadonlyTableOfContents loadTableOfContents() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isModifiable() {
        // TODO Auto-generated method stub
        return false;
    }

}
