/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.fl.identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.fl.CompilationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParameterNodeGeneratorTest {

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private JavaBuilderSet builderSet;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private CompilationResult<JavaCodeFragment> contextCompilationResult;

    private ParameterNodeGenerator parameterNodeJavaGenerator;

    private ParameterNode parameterNode;

    @BeforeEach
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
        verifyNoInteractions(contextCompilationResult);
    }
}
