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

package org.faktorips.devtools.formulalibrary.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.GenericBuilderKindId;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.formulalibrary.builder.xpand.FormulaLibraryClassBuilder;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FormulaLibraryFunctionFlFunctionAdapterTest {

    private static final String TEST_DESCRIPTION = "testDescription";
    private static final String METHOD_NAME = "computeValue";
    private static final String QUALIFIED_NAME = "pack.sub.Lib";

    @Mock
    private IIpsProject project;

    @Mock
    private IFormulaLibrary library;

    @Mock
    private IBaseMethod method;

    private FormulaLibraryFunctionFlFunctionAdapter adapter;

    @Before
    public void setUp() throws Exception {

        when(method.getIpsProject()).thenReturn(project);

        when(method.findDatatype(project)).thenReturn(Datatype.INTEGER);
        when(method.getParameterDatatypes()).thenReturn(Arrays.<Datatype> asList(Datatype.DECIMAL, Datatype.BOOLEAN));

        when(method.getIpsObject()).thenReturn(library);
        when(library.getQualifiedName()).thenReturn(QUALIFIED_NAME);

        when(method.getName()).thenReturn(METHOD_NAME);

        adapter = new FormulaLibraryFunctionFlFunctionAdapter(method, TEST_DESCRIPTION);
    }

    @Test
    public void testCompile() throws CoreException {

        IIpsArtefactBuilderSet builderSet = mock(IIpsArtefactBuilderSet.class);
        FormulaLibraryClassBuilder builder = mock(FormulaLibraryClassBuilder.class);

        when(
                builderSet.getBuilderById(new GenericBuilderKindId(FormulaLibraryClassBuilder.NAME),
                        FormulaLibraryClassBuilder.class)).thenReturn(builder);
        when(project.getIpsArtefactBuilderSet()).thenReturn(builderSet);

        String generationPackage = "de.test.internal";
        when(builder.getQualifiedClassName(library)).thenReturn(generationPackage + "." + QUALIFIED_NAME);

        CompilationResultImpl argResult1 = new CompilationResultImpl();
        argResult1.setJavaCodeFragment(new JavaCodeFragment("param1"));
        CompilationResultImpl argResult2 = new CompilationResultImpl();
        argResult2.setJavaCodeFragment(new JavaCodeFragment("param2"));
        CompilationResult[] argResults = { argResult1, argResult2 };
        CompilationResult compilationResult = adapter.compile(argResults);

        assertNotNull(compilationResult);
        JavaCodeFragment codeFragment = compilationResult.getCodeFragment();
        assertNotNull(codeFragment);
        assertEquals(generationPackage + "." + QUALIFIED_NAME + "." + METHOD_NAME + "(param1, param2)",
                codeFragment.getSourcecode());

        MessageList messageList = compilationResult.getMessages();

        assertTrue(messageList.isEmpty());
    }

    @Test
    public void testGetType() {
        assertEquals(Datatype.INTEGER, adapter.getType());
    }

    @Test
    public void testGetName() {
        assertEquals(QUALIFIED_NAME + "." + METHOD_NAME, adapter.getName());
    }

    @Test
    public void testGetArgTypes() {
        Datatype[] argTypes = adapter.getArgTypes();

        assertEquals(2, argTypes.length);
        assertEquals(Datatype.DECIMAL, argTypes[0]);
        assertEquals(Datatype.BOOLEAN, argTypes[1]);

    }

    @Test
    public void testGetDescription() throws Exception {
        assertEquals(TEST_DESCRIPTION, adapter.getDescription());
    }
}
