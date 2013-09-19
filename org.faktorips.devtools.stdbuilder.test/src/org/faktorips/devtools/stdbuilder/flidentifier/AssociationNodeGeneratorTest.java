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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.internal.model.type.Association;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
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
    Association association;

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

    @Before
    public void setUp() throws CoreException {
        setUpMockAssociation();
        setUpCompilationResult();
        setUpBuilderSet();
        node = new AssociationNode(association, true, ipsProject);
        setUpSpyGenerator();
    }

    private void setUpBuilderSet() {
        when(builderSet.getJavaClassName(any(Datatype.class), eq(true))).thenCallRealMethod();
    }

    private void setUpMockAssociation() throws CoreException {
        when(association.findTarget(any(IIpsProject.class))).thenReturn(target);
        when(target.getJavaClassName()).thenReturn("Coverage");
    }

    private void setUpCompilationResult() {
        codeFragment = new JavaCodeFragment("contextCode", new ImportDeclaration());
        when(contextCompilationResult.getCodeFragment()).thenReturn(codeFragment);
        when(contextCompilationResult.getDatatype()).thenReturn(normalDatatype);
    }

    private void configureCompilatioResultWithListDatatype() {
        when(contextCompilationResult.getDatatype()).thenReturn(listDatatype);
        when(listDatatype.getBasicDatatype()).thenReturn(elementDatatype);
        when(elementDatatype.getJavaClassName()).thenReturn("Policy");
    }

    private void setUpSpyGenerator() {
        gen = spy(new AssociationNodeGenerator(factory, builderSet));
        doReturn("getCoverages").when(gen).getAssociationTargetGetterName(association);
    }

    @Test
    public void testGetCompilationResult_singleContext_1to1() {
        when(association.is1To1()).thenReturn(true);

        gen.getCompilationResult(node, contextCompilationResult);

        verify(gen).compileAssociationTo1(contextCompilationResult, association, target);
        verify(gen, never()).compileAssociationToMany(contextCompilationResult, association, target);
    }

    @Test
    public void testGetCompilationResult_singleContext_1toMany() {
        when(association.is1To1()).thenReturn(false);

        gen.getCompilationResult(node, contextCompilationResult);

        verify(gen, never()).compileAssociationTo1(contextCompilationResult, association, target);
        verify(gen).compileAssociationToMany(contextCompilationResult, association, target);
    }

    @Test
    public void testGetCompilationResult_singleContext_1to1Code() {
        setUpAssociation1to1();

        CompilationResult<JavaCodeFragment> compilationResult = gen
                .getCompilationResult(node, contextCompilationResult);

        assertEquals("contextCode.getCoverages()", compilationResult.getCodeFragment().getSourcecode());
    }

    /**
     * No sense in mocking ModelService and XClasses. Expect mocked result ("getCoverages") for both
     * to1 and toMany associations.
     */
    @Test
    public void testGetCompilationResult_singleContext_1toManyCode() {
        setUpAssociation1toMany();

        CompilationResult<JavaCodeFragment> compilationResult = gen
                .getCompilationResult(node, contextCompilationResult);

        assertEquals("contextCode.getCoverages()", compilationResult.getCodeFragment().getSourcecode());
    }

    @Test
    public void testGetCompilationResult_listContext() {
        configureCompilatioResultWithListDatatype();

        gen.getCompilationResult(node, contextCompilationResult);

        verify(gen).compileListContext(contextCompilationResult, association, target);
    }

    @Test
    public void testGetCompilationResult_listContext_1to1Code() {
        setUpAssociation1to1();
        configureCompilatioResultWithListDatatype();

        CompilationResult<JavaCodeFragment> compilationResult = gen
                .getCompilationResult(node, contextCompilationResult);

        assertEquals(
                "new AssociationTo1Helper<Policy, Coverage>(){@Override protected Coverage getTargetInternal(Policy sourceObject){return sourceObject.getCoverages();}}.getTargets(contextCode)",
                compilationResult.getCodeFragment().getSourcecode());
    }

    @Test
    public void testGetCompilationResult_listContext_1toManyCode() {
        setUpAssociation1toMany();
        configureCompilatioResultWithListDatatype();

        CompilationResult<JavaCodeFragment> compilationResult = gen
                .getCompilationResult(node, contextCompilationResult);

        assertEquals(
                "new AssociationToManyHelper<Policy, Coverage>(){@Override protected List<Coverage> getTargetsInternal(Policy sourceObject){return sourceObject.getCoverages();}}.getTargets(contextCode)",
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
