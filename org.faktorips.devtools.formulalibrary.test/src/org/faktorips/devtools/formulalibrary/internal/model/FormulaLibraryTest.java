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
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
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

    private FormulaLibrary newFormulaLibrary(String folder) throws CoreException {
        IFolder parentFolder = null;
        IIpsPackageFragmentRoot packageFragmentRoot = newIpsPackageFragmentRoot(project, parentFolder, folder);

        IIpsSrcFile file = new IpsSrcFile(packageFragmentRoot, "lib."
                + FormulaLibraryIpsObjectType.getInstance().getFileExtension());
        FormulaLibrary library = new FormulaLibrary(file);
        return library;
    }

    @Test
    public void testGetFlFunctions() throws CoreException {

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

        MessageList validation = formulaLibrary1.validate(project);
        assertFalse(validation.toString(), validation.containsErrorMsg());

        validation.clear();
        validation = formulaLibrary2.validate(project);
        assertFalse(validation.toString(), validation.containsErrorMsg());

    }

}
