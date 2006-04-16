/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.util.Locale;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IParameterIdentifierResolver;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;

/**
 * 
 */
public class ParameterIdentifierResolverTest extends IpsPluginTest {

    private IIpsPackageFragmentRoot pdRootFolder;
    private IIpsPackageFragment pdFolder;
    private IIpsSrcFile pdSrcFile;
    private PolicyCmptType pcType;
    private IAttribute attribute;
    private IParameterIdentifierResolver resolver;
    private IIpsProject pdProject;

    protected void setUp() throws Exception {
        super.setUp();
        pdProject = this.newIpsProject("TestProject");
        IIpsProjectProperties props = pdProject.getProperties();
        props.setJavaSrcLanguage(Locale.GERMAN);
        props.setBuilderSetId(StdBuilderPlugin.STANDARD_BUILDER_EXTENSION_ID);
        pdProject.setProperties(props);
        pdProject.getValueDatatypes(false);
        pdRootFolder = pdProject.getIpsPackageFragmentRoots()[0];
        pdFolder = pdRootFolder.createPackageFragment("products.folder", true, null);
        pdSrcFile = pdFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)pdSrcFile.getIpsObject();
        attribute = pcType.newAttribute();
        attribute.setName("tax");
        attribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        resolver = pdProject.getArtefactBuilderSet().getFlParameterIdentifierResolver();
        resolver.setIpsProject(pdProject);
    }

    private PolicyCmptInterfaceBuilder getPolicyCmptInterfaceBuilder() throws Exception {
        IIpsArtefactBuilder[] builders = pdProject.getArtefactBuilderSet().getArtefactBuilders();
        for (int i = 0; i < builders.length; i++) {
            if (((JavaSourceFileBuilder)builders[i]).getKindId().equals(StandardBuilderSet.KIND_POLICY_CMPT_INTERFACE)) {
                return (PolicyCmptInterfaceBuilder)builders[i];
            }
        }
        return null;
    }

    public void testCompile() throws Exception {
        Locale locale = Locale.GERMAN;

        // no parameter registered => undefined identifier
        CompilationResult result = resolver.compile("identifier", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // parameter with a value datatype
        Parameter p1 = new Parameter(0, "rate", Datatype.MONEY.getQualifiedName());
        resolver.setParameters(new Parameter[] { p1 });
        result = resolver.compile("rate", locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.MONEY, result.getDatatype());
        assertEquals("rate", result.getCodeFragment().getSourcecode());

        // parameter with the datatype being a policy component type
        // => resolver can resolve identifiers with form paramName.attributeName
        // with attributeName is the name of one of the type's attributes
        Parameter p2 = new Parameter(1, "policy", pcType.getQualifiedName());
        resolver.setParameters(new Parameter[] { p2 });
        result = resolver.compile("policy.tax", locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.DECIMAL, result.getDatatype());
        String expected = "policy."
                + getPolicyCmptInterfaceBuilder().getMethodNameGetPropertyValue(attribute, result.getDatatype()) + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());

        // unkown parameter
        result = resolver.compile("unkownParameter", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // parameter with unkown datatype
        Parameter p3 = new Parameter(0, "p3", "UnkownDatatype");
        resolver.setParameters(new Parameter[] { p3 });
        result = resolver.compile("p3", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // unkown attribute
        result = resolver.compile("policy.unkownAttribute", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // attribute with unkown datatype
        attribute.setDatatype("UnknownDatatype");
        result = resolver.compile("policy.tax", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // no attributename given
        result = resolver.compile("policy.", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // unkown policy component type
        result = resolver.compile("unkownType.tax", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());
    }

}
