/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.AbstractParameterIdentifierResolver;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.formula.FormulaEvaluatorUtil;
import org.junit.Before;
import org.junit.Test;

public class AbstractParameterIdentifierResolverTest extends AbstractStdBuilderTest {

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IFormula formula;
    private IProductCmptTypeMethod method;
    private IPolicyCmptTypeAttribute attribute;
    private AbstractParameterIdentifierResolver resolver;
    private Locale locale;
    private PolicyCmptType branchPolicyCmptType;
    private PolicyCmptType leafPolicyCmptType;
    private IPolicyCmptTypeAssociation associationToBranches;
    private IPolicyCmptTypeAssociation associationBranchToLeaf;
    private IPolicyCmptTypeAssociation associationToLeaf;
    private PolicyCmptType twigPolicyCmptType;
    private IPolicyCmptTypeAssociation associationBranchToTwig;
    private IPolicyCmptTypeAssociation associationTwigToLeaf;
    private IPolicyCmptTypeAttribute attributeColor;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        locale = Locale.GERMAN;
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setBuilderSetId(StdBuilderPlugin.STANDARD_BUILDER_EXTENSION_ID);
        ipsProject.setProperties(props);
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("tax");
        attribute.setDatatype(Datatype.DECIMAL.getQualifiedName());

        branchPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "Branch", "BranchType");
        twigPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "Twig", "TwigType");
        leafPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "Leaf", "LeafType");
        associationToBranches = policyCmptType.newPolicyCmptTypeAssociation();
        associationToBranches.setTarget(branchPolicyCmptType.getQualifiedName());
        associationToBranches.setTargetRoleSingular("branch");
        associationToBranches.setTargetRolePlural("branches");
        associationToBranches.setMaxCardinality(IAssociation.CARDINALITY_MANY);
        associationToLeaf = policyCmptType.newPolicyCmptTypeAssociation();
        associationToLeaf.setTarget(leafPolicyCmptType.getQualifiedName());
        associationToLeaf.setTargetRoleSingular("leaf");
        associationToLeaf.setTargetRolePlural("leafs");
        associationToLeaf.setMaxCardinality(IAssociation.CARDINALITY_ONE);
        associationBranchToLeaf = branchPolicyCmptType.newPolicyCmptTypeAssociation();
        associationBranchToLeaf.setTarget(leafPolicyCmptType.getQualifiedName());
        associationBranchToLeaf.setTargetRoleSingular("leaf");
        associationBranchToLeaf.setMaxCardinality(IAssociation.CARDINALITY_ONE);
        associationBranchToTwig = branchPolicyCmptType.newPolicyCmptTypeAssociation();
        associationBranchToTwig.setTarget(twigPolicyCmptType.getQualifiedName());
        associationBranchToTwig.setTargetRoleSingular("twig");
        associationBranchToTwig.setMaxCardinality(IAssociation.CARDINALITY_ONE);
        associationTwigToLeaf = twigPolicyCmptType.newPolicyCmptTypeAssociation();
        associationTwigToLeaf.setTarget(leafPolicyCmptType.getQualifiedName());
        associationTwigToLeaf.setTargetRoleSingular("leaf");
        associationTwigToLeaf.setMaxCardinality(IAssociation.CARDINALITY_ONE);
        attributeColor = leafPolicyCmptType.newPolicyCmptTypeAttribute();
        attributeColor.setName("color");
        attributeColor.setDatatype(Datatype.STRING.getQualifiedName());

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
            if (builder instanceof PolicyCmptInterfaceBuilder) {
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
        assertEquals(1, result.getMessages().size());
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
        GenPolicyCmptType genPolicyCmptType = ((StandardBuilderSet)getPolicyCmptInterfaceBuilder().getBuilderSet())
                .getGenerator(policyCmptType);
        String expected = "policy."
                + genPolicyCmptType.getMethodNameGetPropertyValue(attribute.getName(), result.getDatatype()) + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());

        // unknown parameter
        result = resolver.compile("unkownParameter", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // parameter with unknown datatype
        method.newParameter("UnknownDatatye", "p3");
        result = resolver.compile("p3", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // unknown attribute
        result = resolver.compile("policy.unkownAttribute", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // attribute with unknown datatype
        attribute.setDatatype("UnknownDatatype");
        result = resolver.compile("policy.tax", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // no attribute name given
        result = resolver.compile("policy.", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // unknown policy component type
        result = resolver.compile("unkownType.tax", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());

        // default
        attribute.setProductRelevant(true);
        attribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        result = resolver.compile("policy.tax@default", null, locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.DECIMAL, result.getDatatype());
        expected = "policy." + genPolicyCmptType.getGenProductCmptType().getMethodNameGetProductCmptGeneration()
                + "()."
                + ((GenChangeableAttribute)genPolicyCmptType.getGenerator(attribute)).getMethodNameGetDefaultValue()
                + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());

        // attribute of the product component type can be accessed without specifying the product
        // component type
        IAttribute attribute = productCmptType.newAttribute();
        attribute.setName("a");
        attribute.setDatatype(Datatype.INTEGER.getName());
        result = resolver.compile("a", null, locale);
        assertTrue(result.successfull());
        assertEquals("this.getA()", result.getCodeFragment().getSourcecode());
    }

    @Test
    public void testCompileAssociations() throws CoreException, Exception {
        // parameter with the datatype being a policy component type
        // => resolver can resolve identifiers with form paramName.associationName.x
        // with associationName being the name of one of the type's associations
        method.newParameter(policyCmptType.getQualifiedName(), "policy");

        // 1toMany
        CompilationResult result = resolver.compile("policy.branch", null, locale);
        assertTrue(result.successfull());
        assertEquals(new ArrayOfValueDatatype(branchPolicyCmptType, 1), result.getDatatype());
        StandardBuilderSet standardBuilderSet = (StandardBuilderSet)getPolicyCmptInterfaceBuilder().getBuilderSet();
        String expected = "policy."
                + standardBuilderSet.getGenerator(policyCmptType).getGenerator(associationToBranches)
                        .getMethodNameGetAllRefObjects() + "().toArray(new IModelObject[0])";
        assertEquals(expected, result.getCodeFragment().getSourcecode());

        // 1toMany, indexed
        result = resolver.compile("policy.branch[1]", null, locale);
        assertTrue(result.successfull());
        assertEquals(branchPolicyCmptType, result.getDatatype());
        expected = "policy."
                + standardBuilderSet.getGenerator(policyCmptType).getGenerator(associationToBranches)
                        .getMethodNameGetRefObject() + "(1)";
        assertEquals(expected, result.getCodeFragment().getSourcecode());

        // 1toMany, chained with 1to1
        result = resolver.compile("policy.branch.leaf", null, locale);
        assertTrue(result.successfull());
        assertEquals(new ArrayOfValueDatatype(leafPolicyCmptType, 1), result.getDatatype());
        expected = "FormulaEvaluatorUtil.getTargets(policy."
                + standardBuilderSet.getGenerator(policyCmptType).getGenerator(associationToBranches)
                        .getMethodNameGetAllRefObjects()
                + "().toArray(new IModelObject[0]), \"leaf\", this.getRepository())";
        assertEquals(expected, result.getCodeFragment().getSourcecode());

        // 1to1
        method.newParameter(twigPolicyCmptType.getQualifiedName(), "twig");
        result = resolver.compile("twig.leaf", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        expected = "twig."
                + standardBuilderSet.getGenerator(twigPolicyCmptType).getGenerator(associationTwigToLeaf)
                        .getMethodNameGetRefObject() + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());

        // 1to1, chained with 1to1
        method.newParameter(branchPolicyCmptType.getQualifiedName(), "branch");
        result = resolver.compile("branch.twig.leaf", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        expected = "branch."
                + standardBuilderSet.getGenerator(branchPolicyCmptType).getGenerator(associationBranchToTwig)
                        .getMethodNameGetRefObject()
                + "()."
                + standardBuilderSet.getGenerator(branchPolicyCmptType).getGenerator(associationTwigToLeaf)
                        .getMethodNameGetRefObject() + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());

        // full chain with index in between
        result = resolver.compile("policy.branch[0].twig.leaf", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        expected = "policy."
                + standardBuilderSet.getGenerator(policyCmptType).getGenerator(associationToBranches)
                        .getMethodNameGetRefObject()
                + "(0)."
                + standardBuilderSet.getGenerator(branchPolicyCmptType).getGenerator(associationBranchToTwig)
                        .getMethodNameGetRefObject()
                + "()."
                + standardBuilderSet.getGenerator(twigPolicyCmptType).getGenerator(associationTwigToLeaf)
                        .getMethodNameGetRefObject() + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());

        // full chain with index at end
        result = resolver.compile("policy.branch.twig.leaf[2]", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        expected = "((ILeaf)FormulaEvaluatorUtil.getTargets(FormulaEvaluatorUtil.getTargets(policy."
                + standardBuilderSet.getGenerator(policyCmptType).getGenerator(associationToBranches)
                        .getMethodNameGetAllRefObjects()
                + "().toArray(new IModelObject[0]), \"twig\", this.getRepository()), \"leaf\", this.getRepository())[2])";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
        result.getCodeFragment().getImportDeclaration().isCovered(FormulaEvaluatorUtil.class);
        result.getCodeFragment().getImportDeclaration().isCovered(IModelObject.class);
        result.getCodeFragment().getImportDeclaration().isCovered("ILeaf");

        // association + attribute
        result = resolver.compile("twig.leaf.color", null, locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.STRING, result.getDatatype());
        expected = "twig."
                + standardBuilderSet.getGenerator(twigPolicyCmptType).getGenerator(associationTwigToLeaf)
                        .getMethodNameGetRefObject()
                + "()."
                + standardBuilderSet.getGenerator(leafPolicyCmptType).getMethodNameGetPropertyValue(
                        attributeColor.getName(), Datatype.STRING) + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());

        // association target not found
        associationTwigToLeaf.setTarget("unknownTarget");
        result = resolver.compile("twig.leaf", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.NO_ASSOCIATION_TARGET, result.getMessages().getMessage(0).getCode());

        // association target not found
        result = resolver.compile("twig.thorn", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());
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
