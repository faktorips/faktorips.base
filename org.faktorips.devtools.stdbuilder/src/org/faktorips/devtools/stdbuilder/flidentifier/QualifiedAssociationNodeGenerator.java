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

package org.faktorips.devtools.stdbuilder.flidentifier;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.QualifiedAssociationNode;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.runtime.formula.FormulaEvaluatorUtil;

/**
 * Generator for {@link QualifiedAssociationNode QualifiedAssociationNodes}. <br>
 * Example in formula language: "policy.converage["hausrat.HRD-Fahrraddiebstahl 2012-03"]"
 * 
 * @see FormulaEvaluatorUtil#getListModelObjectById(java.util.List, String)
 * @see FormulaEvaluatorUtil#getModelObjectById(java.util.List, String)
 * 
 * @author frank
 * @since 3.11.0
 */
public class QualifiedAssociationNodeGenerator extends StdBuilderIdentifierNodeGenerator {

    private static final String GET_MODEL_OBJECT_BY_ID = "getModelObjectById"; //$NON-NLS-1$
    private static final String GET_LIST_MODEL_OBJECT_BY_ID = "getListModelObjectById"; //$NON-NLS-1$
    private static final Class<FormulaEvaluatorUtil> CLAZZ_FORMULAEVALUATIONUTIL = org.faktorips.runtime.formula.FormulaEvaluatorUtil.class;

    public QualifiedAssociationNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory,
            StandardBuilderSet builderSet) {
        super(factory, builderSet);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        QualifiedAssociationNode node = (QualifiedAssociationNode)identifierNode;

        return compileAssociationQualifier(node.isListOfTypeDatatype(), node,
                contextCompilationResult.getCodeFragment());
    }

    private CompilationResult<JavaCodeFragment> compileAssociationQualifier(boolean isListOfDataype,
            QualifiedAssociationNode node,
            JavaCodeFragment contextCodeFragment) {
        JavaCodeFragment qualifiedTargetCode = new JavaCodeFragment();
        if (isListOfDataype) {
            appendCallOfFormulaEvaluationUtilMethod(qualifiedTargetCode, node, GET_LIST_MODEL_OBJECT_BY_ID,
                    contextCodeFragment);
        } else {
            boolean isSameTargetDatatype = isSameTargetDatatype(node);
            if (!isSameTargetDatatype) {
                qualifiedTargetCode.append("(("); //$NON-NLS-1$
                qualifiedTargetCode.appendClassName(getJavaClassName(node.getDatatype()));
                qualifiedTargetCode.append(")"); //$NON-NLS-1$
            }
            appendCallOfFormulaEvaluationUtilMethod(qualifiedTargetCode, node, GET_MODEL_OBJECT_BY_ID,
                    contextCodeFragment);
            if (!isSameTargetDatatype) {
                qualifiedTargetCode.append(")"); //$NON-NLS-1$
            }
        }
        return new CompilationResultImpl(qualifiedTargetCode, node.getDatatype());
    }

    private void appendCallOfFormulaEvaluationUtilMethod(JavaCodeFragment qualifiedTargetCode,
            QualifiedAssociationNode node,
            String methodName,
            JavaCodeFragment contextCodeFragment) {

        qualifiedTargetCode.appendClassName(CLAZZ_FORMULAEVALUATIONUTIL);
        qualifiedTargetCode.append('.');
        qualifiedTargetCode.append(methodName);
        qualifiedTargetCode.append("("); //$NON-NLS-1$
        qualifiedTargetCode.append(createTypeAssociationCodeFragment(node, contextCodeFragment));
        qualifiedTargetCode.append(", \""); //$NON-NLS-1$
        qualifiedTargetCode.append(node.getRuntimeID());
        qualifiedTargetCode.append("\")"); //$NON-NLS-1$
    }

    private JavaCodeFragment createTypeAssociationCodeFragment(QualifiedAssociationNode node,
            JavaCodeFragment contextodeFragment) {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment(contextodeFragment);
        String associationTargetGetterName = getAssociationTargetGetterName(node.getAssociation());
        javaCodeFragment.append('.');
        javaCodeFragment.append(associationTargetGetterName);
        javaCodeFragment.append("()"); //$NON-NLS-1$
        return javaCodeFragment;
    }

    private boolean isSameTargetDatatype(QualifiedAssociationNode node) {
        String dataTypeQualifiedName = node.getDatatype().getQualifiedName();
        String datatypeAssociationTarget = node.getAssociation().getTarget();
        return dataTypeQualifiedName != null && dataTypeQualifiedName.equals(datatypeAssociationTarget);
    }

    private String getAssociationTargetGetterName(IAssociation association) {
        XPolicyAssociation xPolicyAssociation = getModelNode(association, XPolicyAssociation.class);
        return xPolicyAssociation.getMethodNameGetter();
    }

}
