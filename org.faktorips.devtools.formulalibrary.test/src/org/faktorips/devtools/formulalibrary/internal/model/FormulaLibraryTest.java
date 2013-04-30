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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;
import org.faktorips.fl.FlFunction;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class FormulaLibraryTest extends AbstractIpsPluginTest {

    private IIpsProject project;

    @Override
    @Before
    public void setUp() throws CoreException {
        project = newIpsProject("Project");
    }

    @Test
    public void testFormulaFunctions() throws CoreException {
        FormulaLibrary library = newFormulaLibrary("folder");

        assertTrue(library.getFormulaFunctions().isEmpty());

        IFormulaFunction function1 = library.newFormulaFunction();

        assertNotNull(function1);

        IFormulaFunction function2 = library.newFormulaFunction();

        assertNotNull(function2);

        List<IFormulaFunction> formulaFunctions = library.getFormulaFunctions();
        assertEquals(2, formulaFunctions.size());
        assertTrue(formulaFunctions.contains(function1));

        library.removeFormulaFunction(function1);
        library.removeFormulaFunction(function2);

        formulaFunctions = library.getFormulaFunctions();
        assertTrue(formulaFunctions.isEmpty());

    }

    @Test
    public void testInitFromXml() throws CoreException {
        FormulaLibrary formulaLibrary = newFormulaLibrary("folder.");
        Element element = getTestDocument().getDocumentElement();
        formulaLibrary.initFromXml(element);

        List<IFormulaFunction> formulaFunctions = formulaLibrary.getFormulaFunctions();

        assertEquals(1, formulaFunctions.size());
    }

    @Test
    public void testToXml() throws CoreException {
        FormulaLibrary formulaLibrary = newFormulaLibrary("folder");
        formulaLibrary.newFormulaFunction();

        Element element = formulaLibrary.toXml(newDocument());
        FormulaLibrary copy = newFormulaLibrary("folder2");
        copy.initFromXml(element);

        List<IFormulaFunction> formulaFunctions = copy.getFormulaFunctions();
        assertEquals(1, formulaFunctions.size());
    }

    @Test
    public void testValidateSameLibraryFunctionFormulaName() throws CoreException {
        FormulaLibrary formulaLibrary = newFormulaLibrary("folder");

        IFormulaFunction formulaFunction1 = formulaLibrary.newFormulaFunction();
        IFormulaMethod formulaMethod1 = formulaFunction1.getFormulaMethod();

        formulaMethod1.setName("Formula1");
        formulaMethod1.setDatatype("Boolean");
        formulaMethod1.setFormulaName("NewFormula1");
        formulaMethod1.newParameter("Integer", "param1");
        formulaMethod1.newParameter("Boolean", "param2");

        formulaFunction1.getExpression().setExpression("true");

        IFormulaFunction formulaFunction2 = formulaLibrary.newFormulaFunction();
        IFormulaMethod formulaMethod2 = formulaFunction2.getFormulaMethod();

        formulaMethod2.setName("Formula2");
        formulaMethod2.setDatatype("Boolean");
        formulaMethod2.setFormulaName("NewFormula2");
        formulaMethod2.newParameter("Integer", "param1");
        formulaMethod2.newParameter("Boolean", "param2");

        formulaFunction2.getExpression().setExpression("true");

        MessageList list = formulaLibrary.validate(project);

        assertNull(list.toString(), list.getMessageByCode(IFormulaLibrary.MSGCODE_DUPLICATE_FUNCTION));

        formulaMethod2.setFormulaName(formulaMethod1.getFormulaName());

        getIpsModel().clearValidationCache();
        list = formulaLibrary.validate(project);

        assertNotNull(list.toString(), list.getMessageByCode(IFormulaLibrary.MSGCODE_DUPLICATE_FUNCTION));
    }

    @Test
    public void testValidateSameLibraryFunctionSignatureName() throws CoreException {
        FormulaLibrary formulaLibrary = newFormulaLibrary("folder");

        IFormulaFunction formulaFunction1 = formulaLibrary.newFormulaFunction();
        IFormulaMethod formulaMethod1 = formulaFunction1.getFormulaMethod();

        formulaMethod1.setName("Formula1");
        formulaMethod1.setDatatype("Boolean");
        formulaMethod1.setFormulaName("NewFormula1");
        formulaMethod1.newParameter("Integer", "param1");
        formulaMethod1.newParameter("Boolean", "param2");

        formulaFunction1.getExpression().setExpression("true");

        IFormulaFunction formulaFunction2 = formulaLibrary.newFormulaFunction();
        IFormulaMethod formulaMethod2 = formulaFunction2.getFormulaMethod();

        formulaMethod2.setName("Formula2");
        formulaMethod2.setDatatype("Boolean");
        formulaMethod2.setFormulaName("NewFormula2");
        formulaMethod2.newParameter("Integer", "param1");
        formulaMethod2.newParameter("Boolean", "param2");

        formulaFunction2.getExpression().setExpression("true");

        MessageList list = formulaLibrary.validate(project);

        assertNull(list.toString(), list.getMessageByCode(IFormulaLibrary.MSGCODE_DUPLICATE_SIGNATURE));

        formulaMethod2.setName(formulaMethod1.getName());

        getIpsModel().clearValidationCache();
        list = formulaLibrary.validate(project);

        assertNotNull(list.toString(), list.getMessageByCode(IFormulaLibrary.MSGCODE_DUPLICATE_SIGNATURE));
    }

    @Test
    public void testGetFlFunctions() throws CoreException {

        project.getProperties().getBuilderSetConfig();

        IIpsPackageFragmentRoot packageFragmentRoot = newIpsPackageFragmentRoot(project, null, "folder");
        String qualifiedName = "pack.subpack.FormLib";
        IIpsSrcFile file = newIpsObject(packageFragmentRoot, FormulaLibraryIpsObjectType.getInstance(), qualifiedName)
                .getIpsSrcFile();
        FormulaLibrary formulaLibrary = new FormulaLibrary(file);
        IFormulaFunction formulaFunction = formulaLibrary.newFormulaFunction();
        formulaFunction.getFormulaMethod().setName("computeFormula1");
        formulaFunction.getFormulaMethod().setDatatype(Datatype.INTEGER.getQualifiedName());
        formulaFunction.getFormulaMethod().newParameter(Datatype.BOOLEAN.getQualifiedName(), "param1");
        formulaFunction.getFormulaMethod().newParameter(Datatype.INTEGER.getQualifiedName(), "param2");

        formulaLibrary.newFormulaFunction();

        List<FlFunction> flFunctions = formulaLibrary.getFlFunctions();
        assertNotNull(flFunctions);
        assertEquals(2, flFunctions.size());

        FlFunction flFunction1 = flFunctions.get(0);
        assertEquals(qualifiedName + ".computeFormula1", flFunction1.getName());
        assertEquals(Datatype.INTEGER, flFunction1.getType());
        assertEquals(Datatype.BOOLEAN, flFunction1.getArgTypes()[0]);
        assertEquals(Datatype.INTEGER, flFunction1.getArgTypes()[1]);
    }

    @Test
    public void testSameName() throws CoreException {
        IIpsPackageFragmentRoot packageFragmentRoot = newIpsPackageFragmentRoot(project, null, "folder");

        String qualifiedName1 = "pack.subpack.FormLib";
        IIpsSrcFile file1 = newIpsObject(packageFragmentRoot, FormulaLibraryIpsObjectType.getInstance(), qualifiedName1)
                .getIpsSrcFile();
        FormulaLibrary formulaLibrary1 = new FormulaLibrary(file1);

        String qualifiedName2 = "pack.FormLib";
        IIpsSrcFile file2 = newIpsObject(packageFragmentRoot, FormulaLibraryIpsObjectType.getInstance(), qualifiedName2)
                .getIpsSrcFile();
        FormulaLibrary formulaLibrary2 = new FormulaLibrary(file2);

        MessageList validation = new MessageList();
        formulaLibrary1.validateThis(validation, project);
        assertFalse(validation.toString(), validation.containsErrorMsg());

        validation.clear();
        formulaLibrary2.validateThis(validation, project);
        assertFalse(validation.toString(), validation.containsErrorMsg());

    }

    private FormulaLibrary newFormulaLibrary(String folder) throws CoreException {
        IFolder parentFolder = null;
        IIpsPackageFragmentRoot packageFragmentRoot = newIpsPackageFragmentRoot(project, parentFolder, folder);

        IIpsSrcFile file = new IpsSrcFile(packageFragmentRoot, "lib."
                + FormulaLibraryIpsObjectType.getInstance().getFileExtension());
        FormulaLibrary library = new FormulaLibrary(file);
        return library;
    }

}
