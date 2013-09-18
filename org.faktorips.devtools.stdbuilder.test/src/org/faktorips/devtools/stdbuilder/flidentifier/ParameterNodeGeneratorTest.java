/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.fl.CompilationResult;
import org.faktorips.util.message.Message;
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

    private ParameterNodeGenerator parameterNodeJavaGenerator;

    private ParameterNode parameterNode;

    @Before
    public void createParameterNodeJavaGenerator() throws Exception {
        parameterNodeJavaGenerator = new ParameterNodeGenerator(factory, builderSet);
    }

    private void setUpParameterNode() throws Exception {
        IParameter parameter = mock(IParameter.class);
        when(parameter.findDatatype(ipsProject)).thenReturn(Datatype.STRING);
        when(parameter.getName()).thenReturn("ParamName");
        parameterNode = new ParameterNode(parameter, ipsProject);
    }

    @Test
    public void testGetCompilationResult() throws Exception {
        setUpParameterNode();
        CompilationResult<JavaCodeFragment> compilationResult = parameterNodeJavaGenerator.getCompilationResult(
                parameterNode, null);
        assertNotNull(compilationResult);
        assertNotNull(compilationResult.getCodeFragment());
        assertEquals("ParamName", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCompilationResult_NoInteractionToContextCompilationResult() throws Exception {
        setUpParameterNode();
        CompilationResult<JavaCodeFragment> contextCompilationResult = mock(CompilationResult.class);
        parameterNodeJavaGenerator.getCompilationResult(parameterNode, contextCompilationResult);
        verifyZeroInteractions(contextCompilationResult);
    }

    @Test
    public void testGetErrorCompilationResult() throws Exception {
        Message errorMessage = new Message("code", "errorMessage", Message.ERROR);
        InvalidIdentifierNode invalidIdentifierNode = new InvalidIdentifierNode(errorMessage);
        CompilationResult<JavaCodeFragment> errorCompilationResult = parameterNodeJavaGenerator
                .getErrorCompilationResult(invalidIdentifierNode);
        assertEquals(errorMessage, errorCompilationResult.getMessages().getMessageByCode("code"));
    }
}
