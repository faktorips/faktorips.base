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
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Element;

public class FormulaMethodTest extends AbstractIpsPluginTest {

    @Mock
    private FormulaFunction formulaFunction;

    private FormulaMethod formulaMethod;

    private IIpsProject project;

    @Override
    @Before
    public void setUp() throws CoreException {
        MockitoAnnotations.initMocks(this);
        formulaMethod = new FormulaMethod(formulaFunction, "1");
        project = newIpsProject("Project");
    }

    @Test
    public void testInitFromXml() {
        Element element = getTestDocument().getDocumentElement();

        formulaMethod.initFromXml(element);

        assertEquals("1a", formulaMethod.getId());
        assertEquals("formula1", formulaMethod.getFormulaName());
        assertEquals("computeFormula1", formulaMethod.getName());
        assertEquals("computeFormula1", formulaMethod.getDefaultMethodName());
        assertEquals("Integer", formulaMethod.getDatatype());

        assertEquals("english", formulaMethod.getDescription(Locale.ENGLISH).getText());
        assertEquals("deutsch", formulaMethod.getDescription(Locale.GERMAN).getText());

        IParameter[] params = formulaMethod.getParameters();
        assertEquals(2, params.length);
        assertEquals("3a", params[0].getId());
        assertEquals("param1", params[0].getName());
        assertEquals("Boolean", params[0].getDatatype());
        assertEquals("3b", params[1].getId());
        assertEquals("param2", params[1].getName());
        assertEquals("Integer", params[1].getDatatype());

        Element element2 = formulaMethod.toXml(newDocument());

        IFormulaMethod copy = new FormulaMethod(formulaFunction, "2");
        copy.initFromXml(element2);

        assertEquals("Integer", copy.getDatatype());
        assertEquals("formula1", copy.getFormulaName());
        assertEquals("computeFormula1", copy.getName());
        assertEquals("computeFormula1", copy.getDefaultMethodName());

        IParameter[] params2 = copy.getParameters();
        assertEquals(2, params2.length);
        assertEquals("param1", params2[0].getName());
        assertEquals("Boolean", params2[0].getDatatype());
        assertEquals("param2", params2[1].getName());
        assertEquals("Integer", params2[1].getDatatype());

        assertEquals("deutsch", copy.getDescription(Locale.GERMAN).getText());
    }

    @Test
    public void testValidateSameFormulaName() throws CoreException {
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

        MessageList list = formulaMethod1.validate(project);

        assertNull(list.toString(), list.getMessageByCode(IFormulaMethod.MSGCODE_DUPLICATE_FUNCTION));

        formulaMethod2.setFormulaName(formulaMethod1.getFormulaName());

        getIpsModel().clearValidationCache();
        list = formulaMethod2.validate(project);

        assertNotNull(list.toString(), list.getMessageByCode(IFormulaMethod.MSGCODE_DUPLICATE_FUNCTION));
    }

    @Test
    public void testValidateSameSignatureName() throws CoreException {
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

        MessageList list = formulaMethod1.validate(project);

        assertNull(list.toString(), list.getMessageByCode(IFormulaMethod.MSGCODE_DUPLICATE_SIGNATURE));

        formulaMethod2.setName(formulaMethod1.getName());

        getIpsModel().clearValidationCache();
        list = formulaMethod1.validate(project);

        assertNotNull(list.toString(), list.getMessageByCode(IFormulaMethod.MSGCODE_DUPLICATE_SIGNATURE));
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
