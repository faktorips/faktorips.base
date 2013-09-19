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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.codegen.JavaCodeFragment;
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
public class TestAssociationNodeGeneratorTest {
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
    @Mock
    JavaCodeFragment codeFragment;

    private AssociationNode node;

    private AssociationNodeGenerator gen;

    @Before
    public void setUp() throws CoreException {
        setUpMockAssociation();
        setUpMockCompulationResult();
        node = new AssociationNode(association, true, ipsProject);
        setUpSpyGenerator();
    }

    private void setUpMockAssociation() throws CoreException {
        when(association.findTarget(any(IIpsProject.class))).thenReturn(target);
    }

    private void setUpMockCompulationResult() {
        when(contextCompilationResult.getCodeFragment()).thenReturn(codeFragment);
        when(codeFragment.getSourcecode()).thenReturn("contextCode");
        when(codeFragment.getImportDeclaration()).thenReturn(new ImportDeclaration());
    }

    private void setUpSpyGenerator() {
        gen = spy(new AssociationNodeGenerator(factory, builderSet));
        doReturn("targetGetter").when(gen).getAssociationTargetGetterName(association);
        doReturn("targetsGetter").when(gen).getAssociationTargetsGetterName(association);
    }

    @Test
    public void testGetCompilationResult_1to1Association() {
        when(association.is1To1()).thenReturn(true);
        setUpSpyGenerator();

        gen.getCompilationResult(node, contextCompilationResult);

        verify(gen).compileTypeAssociationTo1(contextCompilationResult, association, target);
        verify(gen, never()).compileTypeAssociationToMany(contextCompilationResult, association, target);
    }

    @Test
    public void testGetCompilationResult_1toManyAssociation() {
        when(association.is1To1()).thenReturn(false);

        gen.getCompilationResult(node, contextCompilationResult);

        verify(gen, never()).compileTypeAssociationTo1(contextCompilationResult, association, target);
        verify(gen).compileTypeAssociationToMany(contextCompilationResult, association, target);
    }

    @Test
    public void testGetCompilationResult_1to1() {
        when(association.is1To1()).thenReturn(true);

        CompilationResult<JavaCodeFragment> compilationResult = gen
                .getCompilationResult(node, contextCompilationResult);

        assertEquals("contextCode.targetGetter()", compilationResult.getCodeFragment().getSourcecode());
    }

    @Test
    public void testGetCompilationResult_1toMany() {
        when(association.is1To1()).thenReturn(false);

        CompilationResult<JavaCodeFragment> compilationResult = gen
                .getCompilationResult(node, contextCompilationResult);

        assertEquals("contextCode.targetsGetter()", compilationResult.getCodeFragment().getSourcecode());
    }
}
