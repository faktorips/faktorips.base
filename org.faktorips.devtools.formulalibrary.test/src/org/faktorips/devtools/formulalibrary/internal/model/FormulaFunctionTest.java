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
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Element;

public class FormulaFunctionTest extends AbstractIpsPluginTest {

    @Mock
    private FormulaLibrary formulaLibrary;

    @Mock
    private IIpsSrcFile ipsSrcFile;

    @Mock
    private IIpsObject ipsObject;

    private FormulaFunction formulaFunction;

    @Override
    @Before
    public void setUp() throws CoreException {
        MockitoAnnotations.initMocks(this);
        formulaFunction = new FormulaFunction(formulaLibrary, "1");
        when(formulaLibrary.getIpsSrcFile()).thenReturn(ipsSrcFile);
        when(formulaLibrary.getIpsObject()).thenReturn(ipsObject);
    }

    @Test
    public void testInitFromXml() {
        Element element = getTestDocument().getDocumentElement();

        formulaFunction.initFromXml(element);

        assertEquals("1a", formulaFunction.getFormulaMethod().getId());
        assertEquals("formula1", formulaFunction.getFormulaMethod().getFormulaName());
        assertEquals("computeFormula1", formulaFunction.getFormulaMethod().getName());
        assertEquals("Integer", formulaFunction.getFormulaMethod().getDatatype());

        assertEquals("english", formulaFunction.getFormulaMethod().getDescription(Locale.ENGLISH).getText());
        assertEquals("deutsch", formulaFunction.getFormulaMethod().getDescription(Locale.GERMAN).getText());

        IParameter[] params = formulaFunction.getFormulaMethod().getParameters();
        assertEquals(2, params.length);
        assertEquals("3a", params[0].getId());
        assertEquals("param1", params[0].getName());
        assertEquals("Boolean", params[0].getDatatype());
        assertEquals("3b", params[1].getId());
        assertEquals("param2", params[1].getName());
        assertEquals("Integer", params[1].getDatatype());

        assertEquals("formula1", formulaFunction.getExpression().getFormulaSignature());
        assertEquals("4a", formulaFunction.getExpression().getId());
        assertEquals("1 + ABS(3.4)", formulaFunction.getExpression().getExpression());

    }

    @Test
    public void testToXml() {
        formulaFunction.getFormulaMethod().setFormulaName("NewFormula");
        formulaFunction.getFormulaMethod().setName("computeNewFormula");
        formulaFunction.getFormulaMethod().setDatatype("Boolean");
        formulaFunction.getFormulaMethod().newParameter("Integer", "param1");
        formulaFunction.getFormulaMethod().newParameter("Boolean", "param2");

        IFormulaMethod method = formulaFunction.getFormulaMethod();
        IDescription newDescription = method.newDescription();
        newDescription.setLocale(Locale.GERMAN);
        newDescription.setText("deutsch");

        IExpression newExpression = formulaFunction.getExpression();
        newExpression.setFormulaSignature("NewFormula");
        newExpression.setExpression("1 + ABS(3.5)");

        Element element = formulaFunction.toXml(newDocument());

        IFormulaFunction copy = new FormulaFunction(formulaLibrary, "2");
        copy.initFromXml(element);

        assertEquals("Boolean", copy.getFormulaMethod().getDatatype());
        assertEquals("NewFormula", copy.getFormulaMethod().getFormulaName());
        assertEquals("computeNewFormula", copy.getFormulaMethod().getName());

        IParameter[] params = copy.getFormulaMethod().getParameters();
        assertEquals(2, params.length);
        assertEquals("param1", params[0].getName());
        assertEquals("Integer", params[0].getDatatype());
        assertEquals("param2", params[1].getName());
        assertEquals("Boolean", params[1].getDatatype());

        assertEquals("deutsch", copy.getFormulaMethod().getDescription(Locale.GERMAN).getText());

        assertEquals("NewFormula", copy.getExpression().getFormulaSignature());
        assertEquals("1 + ABS(3.5)", copy.getExpression().getExpression());
    }

    @Test
    public void testReinitPartCollectionsThis() {
        formulaFunction.reinitPartCollectionsThis();
        assertNull(formulaFunction.getFormulaMethod());
        assertNull(formulaFunction.getExpression());
    }

    @Test
    public void testAddPartThis() {
        assertFalse(formulaFunction.getFormulaMethod().getId().equals("1234"));

        IFormulaMethod formulaMethod = new FormulaMethod(formulaFunction, "1234");
        assertTrue(formulaFunction.addPartThis(formulaMethod));
        assertEquals("1234", formulaFunction.getFormulaMethod().getId());

        assertFalse(formulaFunction.getExpression().getId().equals("4567"));
        FormulaFunctionExpression expression = new FormulaFunctionExpression(formulaFunction, "4567");
        assertTrue(formulaFunction.addPartThis(expression));
        assertEquals("4567", formulaFunction.getExpression().getId());

        assertFalse(formulaFunction.addPartThis(formulaFunction));
    }

    @Test
    public void testRemovePartThis() {
        assertNotNull(formulaFunction.getFormulaMethod());
        assertNotNull(formulaFunction.getExpression());
        assertTrue(formulaFunction.removePartThis(new FormulaMethod(formulaFunction, "1234")));
        assertTrue(formulaFunction.removePartThis(new FormulaFunctionExpression(formulaFunction, "4567")));
        assertNull(formulaFunction.getFormulaMethod());
        assertNull(formulaFunction.getExpression());
        assertFalse(formulaFunction.removePartThis(formulaFunction));
    }

    @Test
    public void testValidateNoName() throws CoreException {
        IIpsProject project = newIpsProject("Project");
        FormulaLibrary library = newFormulaLibrary(project);
        IFormulaFunction function = library.newFormulaFunction();

        MessageList list = function.validate(project);

        assertNotNull(list.toString(), list.getMessageByCode(IBaseMethod.MSGCODE_NO_NAME));

    }

    @Test
    public void testValidateNoFormulaName() throws CoreException {
        IIpsProject project = newIpsProject("Project");
        FormulaLibrary library = newFormulaLibrary(project);
        IFormulaFunction function = library.newFormulaFunction();

        function.getFormulaMethod().setDatatype("Boolean");
        function.getFormulaMethod().setName("computeNewFormula");

        MessageList list = function.validate(project);

        assertNotNull(list.toString(), list.getMessageByCode(IFormulaMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));

    }

    @Test
    public void testValidateInvalidFormulaName() throws CoreException {
        IIpsProject project = newIpsProject("Project");
        FormulaLibrary library = newFormulaLibrary(project);
        IFormulaFunction function = library.newFormulaFunction();

        IFormulaMethod method = function.getFormulaMethod();
        method.setFormulaName("formulaName WithWhitespace");
        method.setName("name");
        method.setDatatype("Boolean");

        function.getExpression().setExpression("true");

        MessageList list = function.validate(project);
        assertNotNull(list.toString(), list.getMessageByCode(IFormulaMethod.MSGCODE_INVALID_FORMULA_NAME));

        method.setFormulaName("formulaNameWithInv@lidCharacter");

        ((IpsModel)project.getIpsModel()).clearValidationCache();
        list = function.validate(project);
        assertNotNull(list.toString(), list.getMessageByCode(IFormulaMethod.MSGCODE_INVALID_FORMULA_NAME));

        method.setFormulaName("formulaName");

        ((IpsModel)project.getIpsModel()).clearValidationCache();
        list = function.validate(project);
        assertFalse(list.toString(), list.containsErrorMsg());
    }

    @Test
    public void testValidateNoDatatype() throws CoreException {
        IIpsProject project = newIpsProject("Project");
        FormulaLibrary library = newFormulaLibrary(project);
        IFormulaFunction function = library.newFormulaFunction();

        function.getFormulaMethod().setName("anyMethodName");
        function.getFormulaMethod().setFormulaName("NewFormula");

        MessageList list = function.validate(project);

        assertNotNull(list.toString(),
                list.getMessageByCode(IFormulaMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));
    }

    private FormulaLibrary newFormulaLibrary(IIpsProject project) throws CoreException {
        IFolder parentFolder = null;
        IIpsPackageFragmentRoot packageFragmentRoot = newIpsPackageFragmentRoot(project, parentFolder, "folder");

        IIpsSrcFile file = new IpsSrcFile(packageFragmentRoot, "lib."
                + FormulaLibraryIpsObjectType.getInstance().getFileExtension());
        FormulaLibrary library = new FormulaLibrary(file);
        return library;
    }

}
