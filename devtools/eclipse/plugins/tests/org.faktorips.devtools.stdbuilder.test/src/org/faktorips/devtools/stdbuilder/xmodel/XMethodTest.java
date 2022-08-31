/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Modifier;

import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet.FormulaCompiling;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class XMethodTest {

    @Mock
    private GeneratorModelContext context;

    @Mock
    private GeneratorConfig generatorConfig;

    @Mock
    private IProductCmptTypeMethod method;

    @Mock
    private ModelService modelService;

    private XMethod xMethod;

    @Before
    public void createXMethod() throws Exception {
        xMethod = new XMethod(method, context, modelService);
        when(context.getBaseGeneratorConfig()).thenReturn(generatorConfig);
    }

    @Test
    public void testGetModifier_normalAnyModifier() throws Exception {
        when(method.getJavaModifier()).thenReturn(Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.FINAL);

        String resultModifier = xMethod.getModifier(false);

        assertEquals("public abstract final", resultModifier);
    }

    @Test
    public void testGetModifier_abstractNotInInterface() throws Exception {
        when(method.getJavaModifier()).thenReturn(Modifier.PUBLIC | Modifier.ABSTRACT);

        String resultModifier = xMethod.getModifier(false);

        assertEquals("public abstract", resultModifier);
    }

    @Test
    public void testGetModifier_abstractInInInterface() throws Exception {
        when(method.getJavaModifier()).thenReturn(Modifier.PUBLIC | Modifier.ABSTRACT);

        String resultModifier = xMethod.getModifier(true);

        assertEquals("public", resultModifier);
    }

    @Test
    public void testGetModifier_notAbstractFormulaGeneratingInSubclasses() throws Exception {
        when(method.isFormulaMandatory()).thenReturn(true);
        when(method.isFormulaSignatureDefinition()).thenReturn(true);
        when(generatorConfig.getFormulaCompiling()).thenReturn(FormulaCompiling.Subclass);
        when(method.getJavaModifier()).thenReturn(Modifier.PUBLIC);

        String resultModifier = xMethod.getModifier(false);

        assertEquals("public abstract", resultModifier);
    }

    @Test
    public void testIsGenerateMethodBody_interface() throws Exception {
        boolean generateMethodBody = xMethod.isGenerateMethodBody(true);

        assertFalse(generateMethodBody);
    }

    @Test
    public void testIsGenerateMethodBody_abstract() throws Exception {
        when(method.isAbstract()).thenReturn(true);

        boolean generateMethodBody = xMethod.isGenerateMethodBody(false);

        assertFalse(generateMethodBody);
    }

    @Test
    public void testIsGenerateMethodBody_formulaXmlGen() throws Exception {
        when(method.isFormulaSignatureDefinition()).thenReturn(true);
        when(generatorConfig.getFormulaCompiling()).thenReturn(FormulaCompiling.XML);

        boolean generateMethodBody = xMethod.isGenerateMethodBody(false);

        assertTrue(generateMethodBody);
    }

    @Test
    public void testIsGenerateMethodBody_formulaBothGen() throws Exception {
        when(method.isFormulaSignatureDefinition()).thenReturn(true);
        when(generatorConfig.getFormulaCompiling()).thenReturn(FormulaCompiling.Both);

        boolean generateMethodBody = xMethod.isGenerateMethodBody(false);

        assertTrue(generateMethodBody);
    }

    @Test
    public void testIsGenerateMethodBody_optionalFormulaBothGen() throws Exception {
        when(method.isFormulaSignatureDefinition()).thenReturn(true);
        when(method.isFormulaSignatureDefinition()).thenReturn(true);
        when(generatorConfig.getFormulaCompiling()).thenReturn(FormulaCompiling.Both);

        boolean generateMethodBody = xMethod.isGenerateMethodBody(false);

        assertTrue(generateMethodBody);
    }

    @Test
    public void testIsGenerateMethodBody_formulaSubclassGen() throws Exception {
        when(method.isFormulaMandatory()).thenReturn(true);
        when(method.isFormulaSignatureDefinition()).thenReturn(true);
        when(generatorConfig.getFormulaCompiling()).thenReturn(FormulaCompiling.Subclass);

        boolean generateMethodBody = xMethod.isGenerateMethodBody(false);

        assertFalse(generateMethodBody);
    }

    @Test
    public void testIsGenerateMethodBody_optionalFormulaSubclassGen() throws Exception {
        when(method.isFormulaSignatureDefinition()).thenReturn(true);
        when(method.isFormulaMandatory()).thenReturn(false);
        when(generatorConfig.getFormulaCompiling()).thenReturn(FormulaCompiling.Subclass);

        boolean generateMethodBody = xMethod.isGenerateMethodBody(false);

        assertTrue(generateMethodBody);
    }

}
