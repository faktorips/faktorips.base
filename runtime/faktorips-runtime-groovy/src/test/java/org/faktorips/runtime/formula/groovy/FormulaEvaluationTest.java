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

import static org.junit.Assert.assertEquals;

import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.internal.AbstractRuntimeRepository;
import org.faktorips.runtime.internal.ProductComponent;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class FormulaEvaluationTest extends XmlAbstractTestCase {

    private AbstractRuntimeRepository repository;
    private ProductComponent pc;
    private TestProductCmptGeneration gen;

    @Before
    public void setUp() {
        repository = new InMemoryRuntimeRepository();
        repository.setFormulaEvaluatorFactory(new GroovyFormulaEvaluatorFactory());
        pc = new TestProductComponent(repository, "TestProduct", "TestProductKind", "TestProductVersion");
        gen = new TestProductCmptGeneration(pc);
    }

    @Test
    public void testFormulaEvaluation() throws Exception {
        Element genEl = getTestDocument().getDocumentElement();
        gen.initFromXml(genEl);
        int result = gen.computeTestFormula(123, "abc");
        assertEquals(1, result);
    }

}
