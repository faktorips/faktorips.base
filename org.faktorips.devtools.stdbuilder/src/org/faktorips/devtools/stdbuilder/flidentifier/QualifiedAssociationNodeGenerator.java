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
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.QualifiedAssociationNode;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * Generator for {@link QualifiedAssociationNode}.
 * 
 * @author frank
 * @since 3.11.0
 */
public class QualifiedAssociationNodeGenerator extends AbstractIdentifierGenerator {

    public QualifiedAssociationNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory,
            StandardBuilderSet builderSet) {
        super(factory, builderSet);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResult(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        QualifiedAssociationNode node = (QualifiedAssociationNode)identifierNode;

        return compileAssociationQualifier(node, contextCompilationResult.getCodeFragment());
    }

    private CompilationResult<JavaCodeFragment> compileAssociationQualifier(QualifiedAssociationNode node,
            JavaCodeFragment contextodeFragment) {

        JavaCodeFragment associationCodeFragment = createTypeAssociationCodeFragment(contextodeFragment, node);
        IType datatype = getElementDatatype(node);

        JavaCodeFragment getQualifiedTargetCode = new JavaCodeFragment();
        boolean isTargetType = isTargetType(node.getPolicyCmptType(), datatype);
        if (!isTargetType) {
            getQualifiedTargetCode.append("(("); //$NON-NLS-1$
            getQualifiedTargetCode.appendClassName(getBuilderSet().getJavaClassName(node.getPolicyCmptType()));
            getQualifiedTargetCode.append(")"); //$NON-NLS-1$
        }
        getQualifiedTargetCode.appendClassName(org.faktorips.runtime.formula.FormulaEvaluatorUtil.class);
        getQualifiedTargetCode.append(".getModelObjectById("); //$NON-NLS-1$
        getQualifiedTargetCode.append(associationCodeFragment);
        getQualifiedTargetCode.append(", \""); //$NON-NLS-1$
        getQualifiedTargetCode.append(node.getRuntimeID());
        getQualifiedTargetCode.append("\")"); //$NON-NLS-1$
        if (!isTargetType) {
            getQualifiedTargetCode.append(")"); //$NON-NLS-1$
            return new CompilationResultImpl(getQualifiedTargetCode, node.getPolicyCmptType());
        } else {
            return new CompilationResultImpl(getQualifiedTargetCode, datatype);
        }
    }

    private JavaCodeFragment createTypeAssociationCodeFragment(JavaCodeFragment contextodeFragment,
            QualifiedAssociationNode node) {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment(contextodeFragment);
        String associationTargetGetterName = getAssociationTargetGetterName(node.getAssociation());
        javaCodeFragment.append('.');
        javaCodeFragment.append(associationTargetGetterName);
        javaCodeFragment.append("()"); //$NON-NLS-1$
        return javaCodeFragment;
    }

    private IType getElementDatatype(QualifiedAssociationNode node) {
        if (node.isListOfTypeDatatype()) {
            ListOfTypeDatatype listOfTypeDatatype = ((ListOfTypeDatatype)node.getDatatype());
            return (IType)listOfTypeDatatype.getBasicDatatype();
        } else {
            return (IType)node.getDatatype();
        }
    }

    private boolean isTargetType(IType policyCmptType, IType target) {
        return policyCmptType != null && policyCmptType.equals(target);
    }

    private String getAssociationTargetGetterName(IAssociation association) {
        XPolicyAssociation xPolicyAssociation = getModelNode(association, XPolicyAssociation.class);
        return xPolicyAssociation.getMethodNameGetter();
    }

}
