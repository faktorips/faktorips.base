/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
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
 * {@link ExpressionXMLBuilderHelper#addCompiledFormulaExpressions(Document, IExpression[], NodeList, MultiStatus)}
 * method for {@link AbstractXmlFileBuilder}s handling {@link IExpression}s.
 * 
 * @author schwering
 */
public class ExpressionXMLBuilderHelper {

    /**
     * Creates a new {@link ExpressionXMLBuilderHelper} that uses the given
     * {@link StandardBuilderSet} to determine Java representations for given formula signature
     * datatypes.
     * 
     * @param builderSet the StandardBuilderSet used to resolve datatypes
     */
    public ExpressionXMLBuilderHelper(StandardBuilderSet builderSet) {
        super();
        this.standardBuilderSet = builderSet;
        this.ipsProject = builderSet.getIpsProject();
    }

    private IIpsProject ipsProject;
    private StandardBuilderSet standardBuilderSet;

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
            IExpression[] formulas,
            NodeList formulaElements,
            MultiStatus buildStatus) {
        for (int formulaIndex = 0; formulaIndex < formulas.length; formulaIndex++) {
            IExpression formula = formulas[formulaIndex];
            IProductCmptTypeMethod method = formula.findFormulaSignature(getIpsProject());
            Element formulaElement = (Element)formulaElements.item(formulaIndex);
            if (method != null && formulaElement != null) {
                Element javaExpression = document.createElement(AbstractFormulaEvaluator.COMPILED_EXPRESSION_XML_TAG);
                JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder().appendln();
                IProductCmptTypeMethod formulaSignature = formula.findFormulaSignature(getIpsProject());
                JavaCodeFragment formulaFragment = ExpressionBuilderHelper.compileFormulaToJava(formula,
                        formulaSignature, false, buildStatus);

                generateFormulaMethodSignature(method, builder);

                builder.openBracket();
                builder.append("return ").append(formulaFragment).append(';');
                builder.closeBracket().appendln();
                String sourceCode = builder.getFragment().getImportDeclaration().toString() + '\n'
                        + builder.getFragment().getSourcecode();
                CDATASection javaCode = document.createCDATASection(sourceCode);
                javaExpression.appendChild(javaCode);
                formulaElement.appendChild(javaExpression);
            }
        }
    }

    private void generateFormulaMethodSignature(IMethod method, JavaCodeFragmentBuilder builder) {
        try {
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
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }
}