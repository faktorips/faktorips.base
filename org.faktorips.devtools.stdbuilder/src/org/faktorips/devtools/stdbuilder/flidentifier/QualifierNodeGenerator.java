/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.flidentifier;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.QualifierNode;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.runtime.formula.FormulaEvaluatorUtil;

/**
 * Generator for {@link QualifierNode QualifiedAssociationNodes}. <br>
 * Example in formula language: "policy.converage["hausrat.HRD-Fahrraddiebstahl 2012-03"]"
 * 
 * @see FormulaEvaluatorUtil#getListModelObjectById(java.util.List, String)
 * @see FormulaEvaluatorUtil#getModelObjectById(java.util.List, String)
 * 
 * @author frank
 * @since 3.11.0
 */
public class QualifierNodeGenerator extends StdBuilderIdentifierNodeGenerator {

    private static final String GET_MODEL_OBJECT_BY_ID = "getModelObjectById"; //$NON-NLS-1$
    private static final String GET_LIST_MODEL_OBJECT_BY_ID = "getListModelObjectById"; //$NON-NLS-1$
    private static final Class<FormulaEvaluatorUtil> CLAZZ_FORMULAEVALUATIONUTIL = org.faktorips.runtime.formula.FormulaEvaluatorUtil.class;

    public QualifierNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory,
            StandardBuilderSet builderSet) {
        super(factory, builderSet);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        QualifierNode node = (QualifierNode)identifierNode;
        JavaCodeFragment javaCodeFragment;
        if (identifierNode.isListOfTypeDatatype()) {
            javaCodeFragment = appendCallOfFormulaEvaluationUtilMethod(node, GET_LIST_MODEL_OBJECT_BY_ID,
                    contextCompilationResult);
        } else {
            javaCodeFragment = appendCallOfFormulaEvaluationUtilMethod(node, GET_MODEL_OBJECT_BY_ID,
                    contextCompilationResult);
        }
        return new CompilationResultImpl(javaCodeFragment, node.getDatatype());
    }

    private JavaCodeFragment appendCallOfFormulaEvaluationUtilMethod(QualifierNode node,
            String methodName,
            CompilationResult<JavaCodeFragment> contextCode) {
        JavaCodeFragment qualifiedTargetCode = new JavaCodeFragment();
        qualifiedTargetCode.appendClassName(CLAZZ_FORMULAEVALUATIONUTIL);
        qualifiedTargetCode.append('.');

        qualifiedTargetCode.append("<");
        qualifiedTargetCode.appendClassName(getJavaClassName(getContextBasicDatatype(contextCode)));
        qualifiedTargetCode.append(", ");
        qualifiedTargetCode.appendClassName(getJavaClassName(getBasicDatatype(node)));
        qualifiedTargetCode.append(">");

        qualifiedTargetCode.append(methodName);
        qualifiedTargetCode.append("("); //$NON-NLS-1$
        qualifiedTargetCode.append(contextCode.getCodeFragment());
        qualifiedTargetCode.append(", \""); //$NON-NLS-1$
        qualifiedTargetCode.append(node.getRuntimeId());
        qualifiedTargetCode.append("\")"); //$NON-NLS-1$
        return qualifiedTargetCode;
    }

}
