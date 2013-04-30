/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.method.IParameter;
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
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
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
    private IPolicyCmptTypeAssociation associationBranchToMoreLeafs;

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
        associationBranchToMoreLeafs = branchPolicyCmptType.newPolicyCmptTypeAssociation();
        associationBranchToMoreLeafs.setTarget(leafPolicyCmptType.getQualifiedName());
        associationBranchToMoreLeafs.setTargetRoleSingular("moreLeaf");
        associationBranchToMoreLeafs.setTargetRolePlural("moreLeafs");
        associationBranchToMoreLeafs.setMaxCardinality(IAssociation.CARDINALITY_MANY);
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

        standardBuilderSet = (StandardBuilderSet)ipsProject.getIpsArtefactBuilderSet();
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

        result = resolver.compile("policy", null, locale);
        assertTrue(result.successfull());
        assertEquals(policyCmptType, result.getDatatype());
        assertEquals("policy", result.getCodeFragment().getSourcecode());

        result = resolver.compile("policy.tax", null, locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.DECIMAL, result.getDatatype());
        XPolicyAttribute xPolicyAttribute = standardBuilderSet.getModelNode(attribute, XPolicyAttribute.class);
        String expected = "policy." + xPolicyAttribute.getMethodNameGetter() + "()";
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
        expected = "policy." + xPolicyAttribute.getMethodNameGetProductCmptGeneration() + "()."
                + xPolicyAttribute.getMethodNameGetDefaultValue() + "()";
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
    public void testCompilePrimitiveBoolean() throws Exception {
        method.newParameter(policyCmptType.getQualifiedName(), "policy");
        IPolicyCmptTypeAttribute primitiveBooleanAttribute = policyCmptType.newPolicyCmptTypeAttribute("bug");
        primitiveBooleanAttribute.setDatatype(Datatype.PRIMITIVE_BOOLEAN.getQualifiedName());

        CompilationResult result = resolver.compile("policy.bug", null, locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.PRIMITIVE_BOOLEAN, result.getDatatype());
        String expected = "policy.isBug()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
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
     * {@link XPolicyAssociation#getMethodNameGetter()} method returns
     * </ul>
     */
    @Test
    public void testCompileAssociations_1toMany() {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        CompilationResult result = resolver.compile("tree.branch", null, locale);
        assertTrue(result.successfull());
        assertEquals(new ListOfTypeDatatype(branchPolicyCmptType), result.getDatatype());
        XPolicyAssociation xPolicyAssociation = standardBuilderSet.getModelNode(associationTreeToBranches,
                XPolicyAssociation.class);
        String expected = "tree." + xPolicyAssociation.getMethodNameGetter() + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "tree"} of the {@link IPolicyCmptType}
     * {@code "Tree"} which has a 1toMany association to the {@link IPolicyCmptType}
     * {@code "Branch"} with the name {@code "branch"}. The resolver is called with
     * {@code "tree.branch[1]["Branch"]"}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * compilation fails because indexed and qualified access can't be combined
     */
    @Test
    public void testDontCompileAssociations_1toManyIndexedAndQualified() {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        CompilationResult result = resolver.compile("tree.branch[1][\"Branch\"]", null, locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.INDEX_AND_QUALIFIER_CAN_NOT_BE_COMBINED, result.getMessages().getMessage(0).getCode());
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
     * {@link XPolicyAssociation#getMethodNameGetSingle()} method returns and the index {@code 1}
     * </ul>
     */
    @Test
    public void testCompileAssociations_1toManyIndexed() {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        CompilationResult result = resolver.compile("tree.branch[1]", null, locale);
        assertTrue(result.successfull());
        assertEquals(branchPolicyCmptType, result.getDatatype());
        XPolicyAssociation xPolicyAssociation = standardBuilderSet.getModelNode(associationTreeToBranches,
                XPolicyAssociation.class);
        String expected = "tree." + xPolicyAssociation.getMethodNameGetSingle() + "(1)";
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
     * {@link XPolicyAssociation#getMethodNameGetSingle()} method returns and the
     * {@link org.faktorips.runtime.formula.FormulaEvaluatorUtil.AssociationTo1Helper}
     * </ul>
     */
    @Test
    public void testCompileAssociations_1toMany1to1() {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        CompilationResult result = resolver.compile("tree.branch.leaf", null, locale);
        assertTrue(result.successfull());
        assertEquals(new ListOfTypeDatatype(leafPolicyCmptType), result.getDatatype());
        XPolicyCmptClass xLeafPct = standardBuilderSet.getModelNode(leafPolicyCmptType, XPolicyCmptClass.class);
        String iLeaf = xLeafPct.getInterfaceName();
        XPolicyCmptClass xBranchPct = standardBuilderSet.getModelNode(branchPolicyCmptType, XPolicyCmptClass.class);
        String iBranch = xBranchPct.getInterfaceName();
        XPolicyAssociation xTreeToBranches = standardBuilderSet.getModelNode(associationTreeToBranches,
                XPolicyAssociation.class);
        String getBranches = xTreeToBranches.getMethodNameGetter();
        XPolicyAssociation xBranchToLeaf = standardBuilderSet.getModelNode(associationBranchToLeaf,
                XPolicyAssociation.class);
        String getLeaf = xBranchToLeaf.getMethodNameGetter();
        String expected = "new " + FormulaEvaluatorUtil.AssociationTo1Helper.class.getSimpleName() + "<" + iBranch
                + ", " + iLeaf + ">(){@Override protected " + iLeaf + " getTargetInternal(" + iBranch
                + " sourceObject){return sourceObject." + getLeaf + "();}}.getTargets(tree." + getBranches + "())";
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
     * {@link XPolicyAssociation#getMethodNameGetter()} method returns
     * </ul>
     */
    @Test
    public void testCompileAssociations_1to1() {
        method.newParameter(twigPolicyCmptType.getQualifiedName(), "twig");
        CompilationResult result = resolver.compile("twig.leaf", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        XPolicyAssociation xPolicyAssociation = standardBuilderSet.getModelNode(associationTwigToLeaf,
                XPolicyAssociation.class);
        String expected = "twig." + xPolicyAssociation.getMethodNameGetter() + "()";
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
     * {@link XPolicyAssociation#getMethodNameGetter()} method returns for both associations
     * </ul>
     */
    @Test
    public void testCompileAssociations_1to1Chain() {
        method.newParameter(branchPolicyCmptType.getQualifiedName(), "branch");
        CompilationResult result = resolver.compile("branch.twig.leaf", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        XPolicyAssociation xBranchToTwig = standardBuilderSet.getModelNode(associationBranchToTwig,
                XPolicyAssociation.class);
        XPolicyAssociation xTwigToLeaf = standardBuilderSet.getModelNode(associationTwigToLeaf,
                XPolicyAssociation.class);
        String expected = "branch." + xBranchToTwig.getMethodNameGetter() + "()." + xTwigToLeaf.getMethodNameGetter()
                + "()";
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
     * {@link XPolicyAssociation#getMethodNameGetSingle()} method returns for the first association
     * and the methods the {@link XPolicyAssociation#getMethodNameGetter()} method returns for the
     * other two associations
     * </ul>
     */
    @Test
    public void testCompileAssociations_fullChainWithIndexInBetween() {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        CompilationResult result = resolver.compile("tree.branch[0].twig.leaf", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        XPolicyAssociation xTreeToBranches = standardBuilderSet.getModelNode(associationTreeToBranches,
                XPolicyAssociation.class);
        XPolicyAssociation xBranchToTwig = standardBuilderSet.getModelNode(associationBranchToTwig,
                XPolicyAssociation.class);
        XPolicyAssociation xTwigToLeaf = standardBuilderSet.getModelNode(associationTwigToLeaf,
                XPolicyAssociation.class);

        String expected = "tree." + xTreeToBranches.getMethodNameGetSingle() + "(0)."
                + xBranchToTwig.getMethodNameGetter() + "()." + xTwigToLeaf.getMethodNameGetter() + "()";
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
     * {@link XPolicyAssociation#getMethodNameGetter()} method returns for the first association and
     * the {@link org.faktorips.runtime.formula.FormulaEvaluatorUtil.AssociationTo1Helper}
     * </ul>
     */
    @Test
    public void testCompileAssociations_fullChainWithIndexAtEnd() {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        CompilationResult result = resolver.compile("tree.branch.twig.leaf[2]", null, locale);
        assertTrue(result.successfull());
        assertEquals(leafPolicyCmptType, result.getDatatype());
        // new AssociationTo1Helper<ITwig, ILeaf>(){@Override
        // protected ILeaf getTargetInternal(ITwig sourceObject){return
        // sourceObject.getleaf();}}.getTargets(new AssociationTo1Helper<IBranch, ITwig>(){@Override
        // protected ITwig getTargetInternal(IBranch sourceObject){return
        // sourceObject.gettwig();}}.getTargets(tree.getBranches())).get(2)
        XPolicyCmptClass xBranch = standardBuilderSet.getModelNode(branchPolicyCmptType, XPolicyCmptClass.class);
        String iBranch = xBranch.getInterfaceName();
        XPolicyCmptClass xTwig = standardBuilderSet.getModelNode(twigPolicyCmptType, XPolicyCmptClass.class);
        String iTwig = xTwig.getInterfaceName();
        XPolicyCmptClass xLeaf = standardBuilderSet.getModelNode(leafPolicyCmptType, XPolicyCmptClass.class);
        String iLeaf = xLeaf.getInterfaceName();
        XPolicyAssociation xTreeToBranches = standardBuilderSet.getModelNode(associationTreeToBranches,
                XPolicyAssociation.class);
        String getBranches = xTreeToBranches.getMethodNameGetter();
        XPolicyAssociation xBranchToTwig = standardBuilderSet.getModelNode(associationBranchToTwig,
                XPolicyAssociation.class);
        String getTwig = xBranchToTwig.getMethodNameGetter();
        XPolicyAssociation xBranchToLeaf = standardBuilderSet.getModelNode(associationBranchToLeaf,
                XPolicyAssociation.class);
        String getLeaf = xBranchToLeaf.getMethodNameGetter();
        String expected = "new " + FormulaEvaluatorUtil.AssociationTo1Helper.class.getSimpleName() + "<" + iTwig + ", "
                + iLeaf + ">(){@Override protected " + iLeaf + " getTargetInternal(" + iTwig
                + " sourceObject){return sourceObject." + getLeaf + "();}}.getTargets(new "
                + FormulaEvaluatorUtil.AssociationTo1Helper.class.getSimpleName() + "<" + iBranch + ", " + iTwig
                + ">(){@Override protected " + iTwig + " getTargetInternal(" + iBranch
                + " sourceObject){return sourceObject." + getTwig + "();}}.getTargets(tree." + getBranches
                + "())).get(2)";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
        result.getCodeFragment().getImportDeclaration().isCovered(FormulaEvaluatorUtil.AssociationTo1Helper.class);
        result.getCodeFragment().getImportDeclaration().isCovered("IBranch");
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
     * {@link XPolicyAssociation#getMethodNameGetter()} method returns
     * </ul>
     */
    @Test
    public void testCompileAssociations_associationAndAttribute() {
        method.newParameter(twigPolicyCmptType.getQualifiedName(), "twig");
        CompilationResult result = resolver.compile("twig.leaf.color", null, locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.STRING, result.getDatatype());
        String expected = "twig."
                + standardBuilderSet.getModelNode(associationTwigToLeaf, XPolicyAssociation.class)
                        .getMethodNameGetter() + "()."
                + standardBuilderSet.getModelNode(attributeColor, XPolicyAttribute.class).getMethodNameGetter() + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "twig"} of the {@link IPolicyCmptType}
     * {@code "Twig"} which has a 1to1 association to the {@link IPolicyCmptType} {@code "Leaf"}
     * with the name {@code "leaf"}. The resolver is called with {@code twig.leaf["MyLeaf"].color2}
     * where {@code "MyLeaf"} is the name of a {@link IProductCmpt} configuring
     * {@code "MultiColoredLeaf"}, which is a subclass of {@code "Leaf"} and has an attribute
     * {@code "color2"} of type String.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result datatype is {@link Datatype#STRING}
     * <li>the result's sourcecode uses the method the
     * {@link XPolicyAssociation#getMethodNameGetter()} method returns and the
     * {@link FormulaEvaluatorUtil#getModelObjectById(IModelObject, String)} method with the ID
     * {@code "MyLeaf"}
     * </ul>
     */
    @Test
    public void testCompileAssociations_qualifiedAssociationAndAttribute() throws CoreException {
        method.newParameter(twigPolicyCmptType.getQualifiedName(), "twig");
        PolicyCmptType multiColoredLeafPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "MultiColoredLeaf",
                "MultiColoredLeafType");
        multiColoredLeafPolicyCmptType.setSupertype(leafPolicyCmptType.getQualifiedName());
        IProductCmptType multiColoredLeafProductCmptType = multiColoredLeafPolicyCmptType
                .findProductCmptType(ipsProject);
        multiColoredLeafProductCmptType.setSupertype(leafPolicyCmptType.findProductCmptType(ipsProject)
                .getQualifiedName());
        IPolicyCmptTypeAttribute attributeColor2 = multiColoredLeafPolicyCmptType.newPolicyCmptTypeAttribute("color2");
        attributeColor2.setDatatype(Datatype.STRING.getQualifiedName());
        newProductCmpt(multiColoredLeafProductCmptType, "pack.MyLeaf");
        CompilationResult result = resolver.compile("twig.leaf[\"MyLeaf\"].color2", null, locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.STRING, result.getDatatype());
        String expected = "(("
                + standardBuilderSet.getModelNode(multiColoredLeafPolicyCmptType, XPolicyCmptClass.class)
                        .getInterfaceName()
                + ")"
                + FormulaEvaluatorUtil.class.getSimpleName()
                + ".getModelObjectById(twig."
                + standardBuilderSet.getModelNode(associationTreeToLeaf, XPolicyAssociation.class)
                        .getMethodNameGetter() + "(), \"MyLeaf\"))."
                + standardBuilderSet.getModelNode(attributeColor2, XPolicyAttribute.class).getMethodNameGetter() + "()";
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
     * {@link XPolicyAssociation#getMethodNameGetter()} method returns and the
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
        String expected = FormulaEvaluatorUtil.class.getSimpleName()
                + ".getModelObjectById(tree."
                + standardBuilderSet.getModelNode(associationTreeToBranches, XPolicyAssociation.class)
                        .getMethodNameGetter() + "(), \"Branch\")";
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
     * {@link XPolicyAssociation#getMethodNameGetter()} method returns and the
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
        String iLeaf = standardBuilderSet.getModelNode(leafPolicyCmptType, XPolicyCmptClass.class).getInterfaceName();
        String iBranch = standardBuilderSet.getModelNode(branchPolicyCmptType, XPolicyCmptClass.class)
                .getInterfaceName();
        String getBranches = standardBuilderSet.getModelNode(associationTreeToBranches, XPolicyAssociation.class)
                .getMethodNameGetter();
        String getLeaf = standardBuilderSet.getModelNode(associationBranchToLeaf, XPolicyAssociation.class)
                .getMethodNameGetter();
        String expected = FormulaEvaluatorUtil.class.getSimpleName() + ".getModelObjectById(new "
                + FormulaEvaluatorUtil.AssociationTo1Helper.class.getSimpleName() + "<" + iBranch + ", " + iLeaf
                + ">(){@Override protected " + iLeaf + " getTargetInternal(" + iBranch
                + " sourceObject){return sourceObject." + getLeaf + "();}}.getTargets(tree." + getBranches
                + "()), \"ALeaf\")";
        // FormulaEvaluatorUtil.getModelObjectById(new AssociationTo1Helper<IBranch,
        // ILeaf>(){@Override protected ILeaf getTargetInternal(IBranch sourceObject){return
        // sourceObject.getleaf();}}.getTargets(tree.getBranches()), "ALeaf")
        assertEquals(expected, result.getCodeFragment().getSourcecode());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IExpression} has a parameter called {@code "tree"} of the {@link IPolicyCmptType}
     * {@code "Tree"} which has a 1toMany association to the {@link IPolicyCmptType}
     * {@code "Branch"} with the name {@code "branch"}. The {@code "Branch"} has a 1toMany
     * association to {@code "Leaf"} called {@code "moreLeafs"}. The {@code "Leaf"} has an attribute
     * {@code "color"} of type String. The resolver is called with
     * {@code tree.branch.moreLeaf["MyLeaf"].color} where {@code "MyLeaf"} is the runtime name of a
     * {@link IProductCmpt} configuring {@code "Leaf"}.
     * <p>
     * 
     * <strong>Expected Outcome:</strong><br>
     * <ul>
     * <li>successful compilation
     * <li>the result datatype is {@code String}
     * <li>the result's sourcecode uses the method the
     * {@link XPolicyAssociation#getMethodNameGetter()} method returns and the
     * {@link FormulaEvaluatorUtil#getModelObjectById(List, String)} method with the ID
     * {@code "ALeaf"}
     * </ul>
     */
    @Test
    public void testCompileAssociations_1toManyChainQualifiedAndAttribute() throws CoreException {
        method.newParameter(treePolicyCmptType.getQualifiedName(), "tree");
        newProductCmpt(leafPolicyCmptType.findProductCmptType(ipsProject), "pack.MyLeaf");
        CompilationResult result = resolver.compile("tree.branch.moreLeaf[\"MyLeaf\"].color", null, locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.STRING, result.getDatatype());
        String iLeaf = standardBuilderSet.getModelNode(leafPolicyCmptType, XPolicyCmptClass.class).getInterfaceName();
        String iBranch = standardBuilderSet.getModelNode(branchPolicyCmptType, XPolicyCmptClass.class)
                .getInterfaceName();
        String getBranches = standardBuilderSet.getModelNode(associationTreeToBranches, XPolicyAssociation.class)
                .getMethodNameGetter();
        String getMoreLeafs = standardBuilderSet.getModelNode(associationBranchToMoreLeafs, XPolicyAssociation.class)
                .getMethodNameGetter();
        String expected = FormulaEvaluatorUtil.class.getSimpleName() + ".getModelObjectById(new "
                + FormulaEvaluatorUtil.AssociationToManyHelper.class.getSimpleName() + "<" + iBranch + ", " + iLeaf
                + ">(){@Override protected " + List.class.getSimpleName() + '<' + iLeaf + "> getTargetsInternal("
                + iBranch + " sourceObject){return sourceObject." + getMoreLeafs + "();}}.getTargets(tree."
                + getBranches + "()), \"MyLeaf\")."
                + standardBuilderSet.getModelNode(attributeColor, XPolicyAttribute.class).getMethodNameGetter() + "()";
        // FormulaEvaluatorUtil.getModelObjectById(new AssociationToManyHelper<IBranch,
        // ILeaf>(){@Override protected List<ILeaf> getTargetsInternal(IBranch sourceObject){return
        // sourceObject.getMoreLeafs();}}.getTargets(tree.getBranches()), "MyLeaf").getColor()
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
     * {@link XPolicyAssociation#getMethodNameGetter()} method returns and the
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
        Object expected = FormulaEvaluatorUtil.class.getSimpleName()
                + ".getModelObjectById(twig."
                + standardBuilderSet.getModelNode(associationTwigToLeaf, XPolicyAssociation.class)
                        .getMethodNameGetter() + "(), \"ALeaf\")";
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
    public void testDontCompileAssociations_1to1QualifiedWithUnknownProduct() {
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
