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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssociationNodeGeneratorTest {
    @Mock
    IAssociation association;

    @Mock
    IType target;

    @Mock
    IIpsProject ipsProject;

    @Mock
    IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    StandardBuilderSet builderSet;

    @Mock
    CompilationResult<JavaCodeFragment> contextCompilationResult;

    JavaCodeFragment codeFragment;

    @Mock
    ListOfTypeDatatype listDatatype;

    @Mock
    IType elementDatatype;

    @Mock
    Datatype normalDatatype;

    private AssociationNode node;

    private AssociationNodeGenerator gen;

    private IdentifierNodeFactory nodeFactory;

    @Before
    public void setUp() throws CoreRuntimeException {
        nodeFactory = new IdentifierNodeFactory(new TextRegion("AssociationNodeGeneratorTest", 0,
                "AssociationNodeGeneratorTest".length()), ipsProject);
        setUpMockAssociation();
        setUpCompilationResult();
        setUpBuilderSet();
        node = (AssociationNode)nodeFactory.createAssociationNode(association, true);
        setUpSpyGenerator();
    }

    private void setUpBuilderSet() {
        when(builderSet.getJavaClassName(target, true)).thenReturn("Coverage");
        when(builderSet.getJavaClassName(elementDatatype, true)).thenReturn("Policy");
    }

    private void setUpMockAssociation() throws CoreRuntimeException {
        when(association.findTarget(any(IIpsProject.class))).thenReturn(target);
    }

    private void setUpCompilationResult() {
        codeFragment = new JavaCodeFragment("contextCode", new ImportDeclaration());
        when(contextCompilationResult.getCodeFragment()).thenReturn(codeFragment);
        when(contextCompilationResult.getDatatype()).thenReturn(normalDatatype);
    }

    private void configureCompilatioResultWithListDatatype() {
        when(contextCompilationResult.getDatatype()).thenReturn(listDatatype);
        when(listDatatype.getBasicDatatype()).thenReturn(elementDatatype);
    }

    private void setUpSpyGenerator() {
        gen = spy(new AssociationNodeGenerator(factory, builderSet));
        doReturn("getCoverages").when(gen).getAssociationTargetGetterName(association);
    }

    @Test
    public void testGetCompilationResult_singleContext_1to1() {
        when(association.is1To1()).thenReturn(true);

        gen.getCompilationResultForCurrentNode(node, contextCompilationResult);

        verify(gen).compileSingleObjectContext(contextCompilationResult.getCodeFragment(), node);
    }

    @Test
    public void testGetCompilationResult_singleContext_1toMany() {
        when(association.is1To1()).thenReturn(false);

        gen.getCompilationResultForCurrentNode(node, contextCompilationResult);

        verify(gen).compileSingleObjectContext(contextCompilationResult.getCodeFragment(), node);
    }

    @Test
    public void testGetCompilationResult_singleContext_1to1Code() {
        setUpAssociation1to1();

        CompilationResult<JavaCodeFragment> compilationResult = gen.getCompilationResultForCurrentNode(node,
                contextCompilationResult);

        assertEquals("contextCode.getCoverages()", compilationResult.getCodeFragment().getSourcecode());
    }

    /**
     * No sense in mocking ModelService and XClasses. Expect mocked result ("getCoverages") for both
     * to1 and toMany associations.
     */
    @Test
    public void testGetCompilationResult_singleContext_1toManyCode() {
        setUpAssociation1toMany();

        CompilationResult<JavaCodeFragment> compilationResult = gen.getCompilationResultForCurrentNode(node,
                contextCompilationResult);

        assertEquals("contextCode.getCoverages()", compilationResult.getCodeFragment().getSourcecode());
    }

    @Test
    public void testGetCompilationResult_listContext() {
        configureCompilatioResultWithListDatatype();

        gen.getCompilationResultForCurrentNode(node, contextCompilationResult);

        verify(gen).compileListContext(contextCompilationResult, node);
    }

    @Test
    public void testGetCompilationResult_listContext_1to1Code() {
        setUpAssociation1to1();
        configureCompilatioResultWithListDatatype();

        CompilationResult<JavaCodeFragment> compilationResult = gen.getCompilationResultForCurrentNode(node,
                contextCompilationResult);

        assertEquals(
                "new AssociationTo1Helper<Policy, Coverage>(){@Override protected Coverage getTargetInternal(Policy sourceObject){return sourceObject.getCoverages();}}.getTargets(contextCode)",
                compilationResult.getCodeFragment().getSourcecode());
    }

    @Test
    public void testGetCompilationResult_listContext_1toManyCode() {
        setUpAssociation1toMany();
        configureCompilatioResultWithListDatatype();

        CompilationResult<JavaCodeFragment> compilationResult = gen.getCompilationResultForCurrentNode(node,
                contextCompilationResult);

        assertEquals(
                "new AssociationToManyHelper<Policy, Coverage>(){@Override protected List<? extends Coverage> getTargetsInternal(Policy sourceObject){return sourceObject.getCoverages();}}.getTargets(contextCode)",
                compilationResult.getCodeFragment().getSourcecode());
    }

    private void setUpAssociation1to1() {
        setUpAssociation(true);
    }

    private void setUpAssociation1toMany() {
        setUpAssociation(false);
    }

    private void setUpAssociation(boolean oneToOne) {
        when(association.is1To1()).thenReturn(oneToOne);
        when(association.is1ToManyIgnoringQualifier()).thenReturn(!oneToOne);
    }
}
