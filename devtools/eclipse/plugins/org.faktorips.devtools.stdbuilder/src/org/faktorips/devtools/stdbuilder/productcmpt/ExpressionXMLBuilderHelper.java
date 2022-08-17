/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.BuilderHelper;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.AbstractXmlFileBuilder;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.runtime.formula.AbstractFormulaEvaluator;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A class that provides the
 * {@link ExpressionXMLBuilderHelper#addCompiledFormulaExpressions(Document, List, List, MultiStatus)}
 * method for {@link AbstractXmlFileBuilder}s handling {@link IExpression}s.
 * 
 * @author schwering
 */
public class ExpressionXMLBuilderHelper {

    private final IIpsProject ipsProject;
    private final StandardBuilderSet standardBuilderSet;

    /**
     * Creates a new {@link ExpressionXMLBuilderHelper} that uses the given
     * {@link StandardBuilderSet} to determine Java representations for given formula signature
     * datatypes.
     * 
     * @param builderSet the StandardBuilderSet used to resolve datatypes
     */
    public ExpressionXMLBuilderHelper(StandardBuilderSet builderSet) {
        super();
        standardBuilderSet = builderSet;
        ipsProject = builderSet.getIpsProject();
    }

    private IIpsProject getIpsProject() {
        return ipsProject;
    }

    private StandardBuilderSet getStandardBuilderSet() {
        return standardBuilderSet;
    }

    /**
     * Adds the compiled expressions to the XML formula elements corresponding to the given
     * formulas, logging errors to the given status object.
     * 
     * @param document the {@link Document} that contains the formula elements.
     * @param formulas the {@link IExpression}s to be compiled. Must be in the same order as the
     *            corresponding {@link Element}s in the given {@code formulaElements}
     *            {@link NodeList}
     * @param formulaElements the {@link Element}s to be extended
     * @param buildStatus buildStatus a {@link MultiStatus} that receives {@link IpsStatus} messages
     *            when compilation produces an error
     */
    public void addCompiledFormulaExpressions(Document document,
            List<? extends IExpression> formulas,
            List<Element> formulaElements,
            MultiStatus buildStatus) {
        Map<String, IExpression> formulaMap = getFormulas(formulas);
        for (Element formulaElement : formulaElements) {
            if (formulaElement != null) {
                String attribute = formulaElement.getAttribute(IExpression.PROPERTY_FORMULA_SIGNATURE_NAME);
                IFormula formula = (IFormula)formulaMap.get(attribute);
                if (formula != null) {
                    IProductCmptTypeMethod method = formula.findFormulaSignature(getIpsProject());
                    if (method != null) {
                        Element javaExpression = document
                                .createElement(AbstractFormulaEvaluator.COMPILED_EXPRESSION_XML_TAG);
                        String sourceCode = generateJavaCode(formula, method, buildStatus);
                        CDATASection javaCode = document.createCDATASection(sourceCode);
                        javaExpression.appendChild(javaCode);
                        formulaElement.appendChild(javaExpression);
                    }
                }
            }
        }
    }

    protected String generateJavaCode(IFormula formula, IProductCmptTypeMethod method, MultiStatus buildStatus) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder().appendln();
        JavaCodeFragment formulaFragment = ExpressionBuilderHelper.compileFormulaToJava(formula, method, buildStatus);

        generateFormulaMethodSignature(method, builder);

        builder.openBracket();
        builder.append("return ").append(formulaFragment).append(';');
        builder.closeBracket().appendln();
        return builder.getFragment().getImportDeclaration().toString() + '\n'
                + builder.getFragment().getSourcecode();
    }

    private Map<String, IExpression> getFormulas(List<? extends IExpression> formulas) {
        Map<String, IExpression> formulaMap = new HashMap<>();
        for (IExpression expression : formulas) {
            formulaMap.put(expression.getFormulaSignature(), expression);
        }
        return formulaMap;
    }

    private void generateFormulaMethodSignature(IMethod method, JavaCodeFragmentBuilder builder) {
        int modifier = method.getJavaModifier();
        String returnClass = StdBuilderHelper.transformDatatypeToJavaClassName(method.getDatatype(), false,
                getStandardBuilderSet(), getIpsProject());
        String methodName = method.getName();

        IParameter[] parameters = method.getParameters();
        String[] parameterNames = BuilderHelper.extractParameterNames(parameters);
        String[] parameterTypes = StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters, false,
                getStandardBuilderSet(), getIpsProject());
        // extend the method signature with the given parameter names
        builder.signature(modifier, returnClass, methodName, parameterNames, parameterTypes, false);
    }
}
