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
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.builder.AbstractParameterIdentifierResolver;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.product.IFormula;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;

/**
 * 
 */
public class AbstractParameterIdentifierResolverTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IFormula formula;
    private IProductCmptTypeMethod method;
    private IPolicyCmptTypeAttribute attribute;
    private AbstractParameterIdentifierResolver resolver;
    private IIpsProject ipsProject;

    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setJavaSrcLanguage(Locale.GERMAN);
        props.setBuilderSetId(StdBuilderPlugin.STANDARD_BUILDER_EXTENSION_ID);
        ipsProject.setProperties(props);
        ipsProject.getValueDatatypes(false);
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
        resolver = (AbstractParameterIdentifierResolver)ipsProject.getIpsArtefactBuilderSet().createFlIdentifierResolver(formula);
    }

    private PolicyCmptInterfaceBuilder getPolicyCmptInterfaceBuilder() throws Exception {
        IIpsArtefactBuilder[] builders = ipsProject.getIpsArtefactBuilderSet().getArtefactBuilders();
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
        method.newParameter(Datatype.MONEY.getQualifiedName(), "rate");
        result = resolver.compile("rate", locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.MONEY, result.getDatatype());
        assertEquals("rate", result.getCodeFragment().getSourcecode());

        // parameter with the datatype being a policy component type
        // => resolver can resolve identifiers with form paramName.attributeName
        // with attributeName is the name of one of the type's attributes
        method.newParameter(policyCmptType.getQualifiedName(), "policy");

        result = resolver.compile("policy.tax", locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.DECIMAL, result.getDatatype());
        String expected = "policy."
                + getPolicyCmptInterfaceBuilder().getMethodNameGetPropertyValue(attribute.getName(), result.getDatatype()) + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());

        // unkown parameter
        result = resolver.compile("unkownParameter", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // parameter with unkown datatype
        method.newParameter("UnknownDatatye", "p3");
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
        
        //attribute of the product component type can be accessed without specifing the product component type
        IAttribute attribute = productCmptType.newAttribute();
        attribute.setName("a");
        attribute.setDatatype(Datatype.INTEGER.getName());
        result = resolver.compile("a", locale);
        assertTrue(result.successfull());
        assertEquals("getA()", result.getCodeFragment().getSourcecode());
        
    }

}
