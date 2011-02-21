/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.AbstractParameterIdentifierResolver;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.junit.Before;
import org.junit.Test;

public class AbstractParameterIdentifierResolverTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IFormula formula;
    private IProductCmptTypeMethod method;
    private IPolicyCmptTypeAttribute attribute;
    private AbstractParameterIdentifierResolver resolver;
    private IIpsProject ipsProject;
    private Locale locale;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        locale = Locale.GERMAN;
        ipsProject = this.newIpsProject();
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setBuilderSetId(StdBuilderPlugin.STANDARD_BUILDER_EXTENSION_ID);
        ipsProject.setProperties(props);
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("tax");
        attribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        method = productCmptType.newFormulaSignature("formula");
        method.setDatatype(Datatype.INTEGER.getName());
        method.setFormulaSignatureDefinition(true);

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "aConfig");
        IProductCmptGeneration productCmptGeneration = (IProductCmptGeneration)productCmpt.newGeneration();
        formula = productCmptGeneration.newFormula();
        formula.setFormulaSignature(method.getFormulaName());
        resolver = (AbstractParameterIdentifierResolver)ipsProject.getIpsArtefactBuilderSet()
                .createFlIdentifierResolver(formula, formula.newExprCompiler(ipsProject));
    }

    private PolicyCmptInterfaceBuilder getPolicyCmptInterfaceBuilder() throws Exception {
        IIpsArtefactBuilder[] builders = ipsProject.getIpsArtefactBuilderSet().getArtefactBuilders();
        for (IIpsArtefactBuilder builder : builders) {
            if (((JavaSourceFileBuilder)builder).getKindId().equals(DefaultBuilderSet.KIND_POLICY_CMPT_TYPE_INTERFACE)) {
                return (PolicyCmptInterfaceBuilder)builder;
            }
        }
        return null;
    }

    @Test
    public void testCompile() throws Exception {
        // no parameter registered => undefined identifier
        CompilationResult result = resolver.compile("identifier", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // parameter with a value datatype
        method.newParameter(Datatype.MONEY.getQualifiedName(), "rate");
        result = resolver.compile("rate", null, locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.MONEY, result.getDatatype());
        assertEquals("rate", result.getCodeFragment().getSourcecode());

        // parameter with the datatype being a policy component type
        // => resolver can resolve identifiers with form paramName.attributeName
        // with attributeName is the name of one of the type's attributes
        method.newParameter(policyCmptType.getQualifiedName(), "policy");

        result = resolver.compile("policy.tax", null, locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.DECIMAL, result.getDatatype());
        String expected = "policy."
                + ((StandardBuilderSet)getPolicyCmptInterfaceBuilder().getBuilderSet()).getGenerator(policyCmptType)
                        .getMethodNameGetPropertyValue(attribute.getName(), result.getDatatype()) + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());

        // unkown parameter
        result = resolver.compile("unkownParameter", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // parameter with unkown datatype
        method.newParameter("UnknownDatatye", "p3");
        result = resolver.compile("p3", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // unkown attribute
        result = resolver.compile("policy.unkownAttribute", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // attribute with unkown datatype
        attribute.setDatatype("UnknownDatatype");
        result = resolver.compile("policy.tax", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // no attributename given
        result = resolver.compile("policy.", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // unkown policy component type
        result = resolver.compile("unkownType.tax", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // attribute of the product component type can be accessed without specifing the product
        // component type
        IAttribute attribute = productCmptType.newAttribute();
        attribute.setName("a");
        attribute.setDatatype(Datatype.INTEGER.getName());
        result = resolver.compile("a", null, locale);
        assertTrue(result.successfull());
        assertEquals("this.getA()", result.getCodeFragment().getSourcecode());
    }

    @Test
    public void testCompileWithEnumsInWorkspaceButNotInParameters() throws Exception {
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
        EnumDatatype testType = (EnumDatatype)ipsProject.findDatatype("TestEnumType");
        assertNotNull(testType);

        CompilationResult result = resolver.compile("TestEnumType.1", null, locale);
        assertTrue(result.failed());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        IParameter methodParam = method.newParameter("TestEnumType", "param0");
        result = resolver.compile("TestEnumType.1", null, locale);
        assertTrue(result.successfull());

        methodParam.delete();
        result = resolver.compile("TestEnumType.1", null, locale);
        assertTrue(result.failed());

        IAttribute attr = productCmptType.newAttribute();
        attr.setDatatype("TestEnumType");
        attr.setName("a");

        result = resolver.compile("TestEnumType.1", null, locale);
        assertTrue(result.successfull());
    }

}
