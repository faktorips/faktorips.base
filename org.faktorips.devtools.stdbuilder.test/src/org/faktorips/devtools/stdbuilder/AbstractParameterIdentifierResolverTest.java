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

import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.AbstractParameterIdentifierResolver;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
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
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.formula.FormulaEvaluatorUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author erzberger
 * @author schwering
 */
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
    private IPolicyCmptTypeAssociation associationTreeToBranches;
    private IPolicyCmptTypeAssociation associationBranchToLeaf;
    private IPolicyCmptTypeAssociation associationTreeToLeaf;
    private PolicyCmptType twigPolicyCmptType;
    private IPolicyCmptTypeAssociation associationBranchToTwig;
    private IPolicyCmptTypeAssociation associationTwigToLeaf;
    private IPolicyCmptTypeAttribute attributeColor;
    private StandardBuilderSet standardBuilderSet;
    private PolicyCmptType treePolicyCmptType;

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

        treePolicyCmptType = newPolicyAndProductCmptType(ipsProject, "Tree", "TreeType");
        branchPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "Branch", "BranchType");
        twigPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "Twig", "TwigType");
        leafPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "Leaf", "LeafType");
        associationTreeToBranches = treePolicyCmptType.newPolicyCmptTypeAssociation();
        associationTreeToBranches.setTarget(branchPolicyCmptType.getQualifiedName());
        associationTreeToBranches.setTargetRoleSingular("branch");
        associationTreeToBranches.setTargetRolePlural("branches");
        associationTreeToBranches.setMaxCardinality(IAssociation.CARDINALITY_MANY);
        associationTreeToLeaf = treePolicyCmptType.newPolicyCmptTypeAssociation();
        associationTreeToLeaf.setTarget(leafPolicyCmptType.getQualifiedName());
        associationTreeToLeaf.setTargetRoleSingular("leaf");
        associationTreeToLeaf.setTargetRolePlural("leafs");
        associationTreeToLeaf.setMaxCardinality(IAssociation.CARDINALITY_ONE);
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

        standardBuilderSet = (StandardBuilderSet)getPolicyCmptInterfaceBuilder().getBuilderSet();
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

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "tree"} of the {@link IPolicyCmptType}
     * {@code "Tree"} which has a 1toMany association to the {@link IPolicyCmptType}
     * {@code "Branch"} with the name {@code "branch"}. The resolver is called with
     * {@code "tree.branch"}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result is a {@link ListOfTypeDatatype} with the basic type {@code IBranch}
     * <li>the result's sourcecode uses the method the
     * {@link GenAssociation#getMethodNameGetAllRefObjects()} method returns
     * </ul>
     */
    @Test
    public void testCompileAssociations_1toMany() throws CoreException {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        CompilationResult result = resolver.compile("tree.branch", null, locale);
        assertTrue(result.successfull());
        assertEquals(new ListOfTypeDatatype(branchPolicyCmptType), result.getDatatype());
        String expected = "tree."
                + standardBuilderSet.getGenerator(policyCmptType).getGenerator(associationTreeToBranches)
                        .getMethodNameGetAllRefObjects() + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "tree"} of the {@link IPolicyCmptType}
     * {@code "Tree"} which has a 1toMany association to the {@link IPolicyCmptType}
     * {@code "Branch"} with the name {@code "branch"}. The resolver is called with
     * {@code "tree.branch[1]"}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result datatype is {@code IBranch}
     * <li>the result's sourcecode uses the method the
     * {@link GenAssociation#getMethodNameGetRefObjectAtIndex()} method returns and the index
     * {@code 1}
     * </ul>
     */
    @Test
    public void testCompileAssociations_1toManyIndexed() throws CoreException {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        CompilationResult result = resolver.compile("tree.branch[1]", null, locale);
        assertTrue(result.successfull());
        assertEquals(branchPolicyCmptType, result.getDatatype());
        String expected = "tree."
                + standardBuilderSet.getGenerator(policyCmptType).getGenerator(associationTreeToBranches)
                        .getMethodNameGetRefObjectAtIndex() + "(1)";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "tree"} of the {@link IPolicyCmptType}
     * {@code "Tree"} which has a 1toMany association to the {@link IPolicyCmptType}
     * {@code "Branch"} with the name {@code "branch"}. The {@code "Branch"} has a 1to1 association
     * to the {@link IPolicyCmptType} {@code "Leaf"} with the name {@code "leaf"}. The resolver is
     * called with {@code "tree.branch.leaf"}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result is a {@link ListOfTypeDatatype} with the basic type {@code ILeaf}
     * <li>the result's sourcecode uses the method the
     * {@link GenAssociation#getMethodNameGetRefObjectAtIndex()} method returns and the
     * {@link FormulaEvaluatorUtil#getTargets(java.util.List, String, Class, org.faktorips.runtime.IRuntimeRepository)}
     * method with the association name {@code "leaf"} and the unqualified class name for
     * {@code ILeaf}
     * </ul>
     */
    @Test
    public void testCompileAssociations_1toMany1to1() throws CoreException {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        CompilationResult result = resolver.compile("tree.branch.leaf", null, locale);
        assertTrue(result.successfull());
        assertEquals(new ListOfTypeDatatype(leafPolicyCmptType), result.getDatatype());
        String expected = "FormulaEvaluatorUtil.getTargets(tree."
                + standardBuilderSet.getGenerator(policyCmptType).getGenerator(associationTreeToBranches)
                        .getMethodNameGetAllRefObjects() + "(), \"leaf\", "
                + standardBuilderSet.getGenerator(leafPolicyCmptType).getUnqualifiedClassName(true)
                + ".class, this.getRepository())";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "tree"} of the {@link IPolicyCmptType}
     * {@code "Tree"} which has a 1to1 association to the {@link IPolicyCmptType} {@code "Leaf"}
     * with the name {@code "leaf"}. The resolver is called with {@code "tree.leaf"}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result datatype is {@code ILeaf}
     * <li>the result's sourcecode uses the method the
     * {@link GenAssociation#getMethodNameGetRefObject()} method returns
     * </ul>
     */
    @Test
    public void testCompileAssociations_1to1() throws CoreException {
        method.newParameter(twigPolicyCmptType.getQualifiedName(), "twig");
        CompilationResult result = resolver.compile("twig.leaf", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        String expected = "twig."
                + standardBuilderSet.getGenerator(twigPolicyCmptType).getGenerator(associationTwigToLeaf)
                        .getMethodNameGetRefObject() + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "tree"} of the {@link IPolicyCmptType}
     * {@code "Tree"} which has a 1to1 association to the {@link IPolicyCmptType} {@code "Leaf"}
     * with the name {@code "leaf"}. The resolver is called with {@code "tree.leaf[0]"}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>failed compilation
     * <li>the result contains an error message with the message code
     * {@link ExprCompiler#NO_INDEX_FOR_1TO1_ASSOCIATION}
     * </ul>
     */
    @Test
    public void testCompileAssociations_1to1Indexed() {
        method.newParameter(twigPolicyCmptType.getQualifiedName(), "twig");
        CompilationResult result = resolver.compile("twig.leaf[0]", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.NO_INDEX_FOR_1TO1_ASSOCIATION, result.getMessages().getMessage(0).getCode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "branch"} of the {@link IPolicyCmptType}
     * {@code "Branch"} which has a 1to1 association to the {@link IPolicyCmptType} {@code "Twig"}
     * with the name {@code "twig"}. The {@code "Twig"} has a 1to1 association to the
     * {@link IPolicyCmptType} {@code "Leaf"} with the name {@code "leaf"}. The resolver is called
     * with {@code "branch.twig.leaf"}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result datatype is {@code ILeaf}
     * <li>the result's sourcecode uses the methods the
     * {@link GenAssociation#getMethodNameGetRefObject()} method returns for both associations
     * </ul>
     */
    @Test
    public void testCompileAssociations_1to1Chain() throws CoreException {
        method.newParameter(branchPolicyCmptType.getQualifiedName(), "branch");
        CompilationResult result = resolver.compile("branch.twig.leaf", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        String expected = "branch."
                + standardBuilderSet.getGenerator(branchPolicyCmptType).getGenerator(associationBranchToTwig)
                        .getMethodNameGetRefObject()
                + "()."
                + standardBuilderSet.getGenerator(branchPolicyCmptType).getGenerator(associationTwigToLeaf)
                        .getMethodNameGetRefObject() + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "tree"} of the {@link IPolicyCmptType}
     * {@code "Tree"} which has a 1toMany association to the {@link IPolicyCmptType}
     * {@code "Branch"} with the name {@code "branch"}.The {@code "Branch"} has a 1to1 association
     * to the {@link IPolicyCmptType} {@code "Twig"} with the name {@code "twig"}. The
     * {@code "Twig"} has a 1to1 association to the {@link IPolicyCmptType} {@code "Leaf"} with the
     * name {@code "leaf"}. The resolver is called with {@code "tree.branch[0].twig.leaf"}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result datatype is {@code ILeaf}
     * <li>the result's sourcecode uses the methods the
     * {@link GenAssociation#getMethodNameGetRefObjectAtIndex()} method returns for the first
     * association and the methods the {@link GenAssociation#getMethodNameGetRefObject()} method
     * returns for the other two associations
     * </ul>
     */
    @Test
    public void testCompileAssociations_fullChainWithIndexInBetween() throws CoreException {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        CompilationResult result = resolver.compile("tree.branch[0].twig.leaf", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        String expected = "tree."
                + standardBuilderSet.getGenerator(policyCmptType).getGenerator(associationTreeToBranches)
                        .getMethodNameGetRefObjectAtIndex()
                + "(0)."
                + standardBuilderSet.getGenerator(branchPolicyCmptType).getGenerator(associationBranchToTwig)
                        .getMethodNameGetRefObject()
                + "()."
                + standardBuilderSet.getGenerator(twigPolicyCmptType).getGenerator(associationTwigToLeaf)
                        .getMethodNameGetRefObject() + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "tree"} of the {@link IPolicyCmptType}
     * {@code "Tree"} which has a 1toMany association to the {@link IPolicyCmptType}
     * {@code "Branch"} with the name {@code "branch"}.The {@code "Branch"} has a 1to1 association
     * to the {@link IPolicyCmptType} {@code "Twig"} with the name {@code "twig"}. The
     * {@code "Twig"} has a 1to1 association to the {@link IPolicyCmptType} {@code "Leaf"} with the
     * name {@code "leaf"}. The resolver is called with {@code "tree.branch.twig.leaf[2]"}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result datatype is {@code ILeaf}
     * <li>the result's sourcecode uses the methods the
     * {@link GenAssociation#getMethodNameGetAllRefObjects()} method returns for the first
     * association and the
     * {@link FormulaEvaluatorUtil#getTargets(java.util.List, String, Class, org.faktorips.runtime.IRuntimeRepository)}
     * method with the association names {@code "twig"} and {@code "leaf"} and the unqualified class
     * names for {@code ITwig} and {@code ILeaf} for the other two associations as well as the
     * {@link List#get(int)} method with index {@code 2}
     * </ul>
     */
    @Test
    public void testCompileAssociations_fullChainWithIndexAtEnd() throws CoreException {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        CompilationResult result = resolver.compile("tree.branch.twig.leaf[2]", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        String expected = "FormulaEvaluatorUtil.getTargets(FormulaEvaluatorUtil.getTargets(tree."
                + standardBuilderSet.getGenerator(policyCmptType).getGenerator(associationTreeToBranches)
                        .getMethodNameGetAllRefObjects() + "(), \"twig\", "
                + standardBuilderSet.getGenerator(twigPolicyCmptType).getUnqualifiedClassName(true)
                + ".class, this.getRepository()), \"leaf\", "
                + standardBuilderSet.getGenerator(leafPolicyCmptType).getUnqualifiedClassName(true)
                + ".class, this.getRepository()).get(2)";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
        result.getCodeFragment().getImportDeclaration().isCovered(FormulaEvaluatorUtil.class);
        result.getCodeFragment().getImportDeclaration().isCovered(IModelObject.class);
        result.getCodeFragment().getImportDeclaration().isCovered("ITwig");
        result.getCodeFragment().getImportDeclaration().isCovered("ILeaf");
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "twig"} of the {@link IPolicyCmptType}
     * {@code "Twig"} which has a 1to1 association to the {@link IPolicyCmptType} {@code "Leaf"}
     * with the name {@code "leaf"}. The {@code "Leaf"} has an attribute {@code "color"} of type
     * String. The resolver is called with {@code "twig.leaf.color"}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result datatype is {@link Datatype#STRING}
     * <li>the result's sourcecode uses the method the
     * {@link GenAssociation#getMethodNameGetRefObject()} method returns
     * </ul>
     */
    @Test
    public void testCompileAssociations_associationAndAttribute() throws CoreException {
        method.newParameter(twigPolicyCmptType.getQualifiedName(), "twig");
        CompilationResult result = resolver.compile("twig.leaf.color", null, locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.STRING, result.getDatatype());
        String expected = "twig."
                + standardBuilderSet.getGenerator(twigPolicyCmptType).getGenerator(associationTwigToLeaf)
                        .getMethodNameGetRefObject()
                + "()."
                + standardBuilderSet.getGenerator(leafPolicyCmptType).getMethodNameGetPropertyValue(
                        attributeColor.getName(), Datatype.STRING) + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "twig"} of the {@link IPolicyCmptType}
     * {@code "Twig"} which has a 1to1 association to an unknown target with the name {@code "leaf"}
     * . The resolver is called with {@code "twig.leaf"}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>failed compilation
     * <li>the result contains an error message with the message code
     * {@link ExprCompiler#NO_ASSOCIATION_TARGET}
     * </ul>
     */
    @Test
    public void testCompileAssociations_associationTargetNotFound() {
        method.newParameter(twigPolicyCmptType.getQualifiedName(), "twig");
        associationTwigToLeaf.setTarget("unknownTarget");
        CompilationResult result = resolver.compile("twig.leaf", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.NO_ASSOCIATION_TARGET, result.getMessages().getMessage(0).getCode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "twig"} of the {@link IPolicyCmptType}
     * {@code "Twig"} which has no association or attribute called {@code "thorn"}. The resolver is
     * called with {@code "twig.thorn"}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>failed compilation
     * <li>the result contains an error message with the message code
     * {@link ExprCompiler#UNDEFINED_IDENTIFIER}
     * </ul>
     */
    @Test
    public void testCompileAssociations_associationTargetUndefined() {
        method.newParameter(twigPolicyCmptType.getQualifiedName(), "twig");
        CompilationResult result = resolver.compile("twig.thorn", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "tree"} of the {@link IPolicyCmptType}
     * {@code "Tree"} which has a 1toMany association to the {@link IPolicyCmptType}
     * {@code "Branch"} with the name {@code "branch"}. The resolver is called with
     * {@code tree.branch["Branch"]} where {@code "Branch"} is the runtime ID of a
     * {@link IProductCmpt} configuring {@code "Branch"}.
     * <p>
     * 
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result datatype is {@code IBranch}
     * <li>the result's sourcecode uses the method the
     * {@link GenAssociation#getMethodNameGetAllRefObjects()} method returns and the
     * {@link FormulaEvaluatorUtil#getModelObjectById(List, String)} method with the ID
     * {@code "Branch"}
     * </ul>
     */
    @Test
    public void testCompileAssociations_1toManyQualified() throws CoreException {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        newProductCmpt(branchPolicyCmptType.findProductCmptType(ipsProject), "my.Branch");
        CompilationResult result = resolver.compile("tree.branch[\"Branch\"]", null, locale);
        assertTrue(result.successfull());
        assertEquals(branchPolicyCmptType, result.getDatatype());
        String expected = "FormulaEvaluatorUtil.getModelObjectById(tree."
                + standardBuilderSet.getGenerator(policyCmptType).getGenerator(associationTreeToBranches)
                        .getMethodNameGetAllRefObjects() + "(), \"Branch\")";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "tree"} of the {@link IPolicyCmptType}
     * {@code "Tree"} which has a 1toMany association to the {@link IPolicyCmptType}
     * {@code "Branch"} with the name {@code "branch"}. The resolver is called with
     * {@code tree.branch["Branch"]} where {@code "Branch"} is the runtime ID of a
     * {@link IProductCmpt} configuring {@code "Branch"}.
     * <p>
     * 
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result datatype is {@code IBranch}
     * <li>the result's sourcecode uses the method the
     * {@link GenAssociation#getMethodNameGetAllRefObjects()} method returns and the
     * {@link FormulaEvaluatorUtil#getModelObjectById(List, String)} method with the ID
     * {@code "Branch"}
     * </ul>
     */
    @Test
    public void testCompileAssociations_1toManyChainQualified() throws CoreException {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        newProductCmpt(leafPolicyCmptType.findProductCmptType(ipsProject), "pack.ALeaf");
        CompilationResult result = resolver.compile("tree.branch.leaf[\"ALeaf\"]", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        String expected = "FormulaEvaluatorUtil.getModelObjectById(FormulaEvaluatorUtil.getTargets(tree."
                + standardBuilderSet.getGenerator(policyCmptType).getGenerator(associationTreeToBranches)
                        .getMethodNameGetAllRefObjects() + "(), \"leaf\", "
                + standardBuilderSet.getGenerator(leafPolicyCmptType).getUnqualifiedClassName(true)
                + ".class, this.getRepository()), \"ALeaf\")";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "twig"} of the {@link IPolicyCmptType}
     * {@code "Twig"} which has a 1to1 association to the {@link IPolicyCmptType} {@code "Leaf"}
     * with the name {@code "leaf"}. The resolver is called with {@code twig.leaf["ALeaf"]} where
     * {@code "ALeaf"} is the runtime ID of a {@link IProductCmpt} configuring {@code "Leaf"}.
     * <p>
     * 
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result datatype is {@code IBranch}
     * <li>the result's sourcecode uses the method the
     * {@link GenAssociation#getMethodNameGetRefObject()} method returns and the
     * {@link FormulaEvaluatorUtil#getModelObjectById(IModelObject, String)} method with the ID
     * {@code "ALeaf"}
     * </ul>
     */
    @Test
    public void testCompileAssociations_1to1Qualified() throws CoreException {
        method.newParameter(twigPolicyCmptType.getQualifiedName(), "twig");
        newProductCmpt(leafPolicyCmptType.findProductCmptType(ipsProject), "pack.ALeaf");
        CompilationResult result = resolver.compile("twig.leaf[\"ALeaf\"]", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        Object expected = "FormulaEvaluatorUtil.getModelObjectById(twig."
                + standardBuilderSet.getGenerator(twigPolicyCmptType).getGenerator(associationTwigToLeaf)
                        .getMethodNameGetRefObject() + "(), \"ALeaf\")";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "twig"} of the {@link IPolicyCmptType}
     * {@code "Twig"} which has a 1to1 association to the {@link IPolicyCmptType} {@code "Leaf"}
     * with the name {@code "leaf"}. The resolver is called with {@code twig.leaf["UnknownLeaf"]}
     * where {@code "UnknownLeaf"} is no known runtime ID.
     * <p>
     * 
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>failed compilation
     * <li>the result contains an error message with the message code
     * {@link ExprCompiler#UNKNOWN_QUALIFIER}
     * </ul>
     */
    @Test
    public void testCompileAssociations_1to1QualifiedWithUnknownProduct() {
        method.newParameter(twigPolicyCmptType.getQualifiedName(), "twig");
        CompilationResult result = resolver.compile("twig.leaf[\"UnknownLeaf\"]", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.UNKNOWN_QUALIFIER, result.getMessages().getMessage(0).getCode());
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
