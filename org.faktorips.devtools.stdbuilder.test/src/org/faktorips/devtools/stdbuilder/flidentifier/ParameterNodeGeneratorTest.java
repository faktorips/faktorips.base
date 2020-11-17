/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParameterNodeGeneratorTest {

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private StandardBuilderSet builderSet;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private CompilationResult<JavaCodeFragment> contextCompilationResult;

    private ParameterNodeGenerator parameterNodeJavaGenerator;

    private ParameterNode parameterNode;

    @Before
    public void createParameterNodeJavaGenerator() throws Exception {
        parameterNodeJavaGenerator = new ParameterNodeGenerator(factory, builderSet);
        setUpParameterNode();
    }

    private void setUpParameterNode() throws Exception {
        IParameter parameter = mock(IParameter.class);
        when(parameter.findDatatype(ipsProject)).thenReturn(Datatype.STRING);
        when(parameter.getName()).thenReturn("ParamName");
        parameterNode = (ParameterNode)new IdentifierNodeFactory(new TextRegion(parameter.getName(), 0, parameter
                .getName().length()), ipsProject).createParameterNode(parameter);
    }

    @Test
    public void testGetCompilationResult() throws Exception {
        CompilationResult<JavaCodeFragment> compilationResult = parameterNodeJavaGenerator
                .getCompilationResultForCurrentNode(parameterNode, null);
        assertNotNull(compilationResult);
        assertNotNull(compilationResult.getCodeFragment());
        assertEquals("ParamName", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @Test
    public void testGetCompilationResult_NoInteractionToContextCompilationResult() throws Exception {
        parameterNodeJavaGenerator.getCompilationResultForCurrentNode(parameterNode, contextCompilationResult);
        verifyZeroInteractions(contextCompilationResult);
    }
}
