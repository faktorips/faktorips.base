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
import org.faktorips.devtools.core.builder.flidentifier.ast.IndexBasedAssociationNode;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * Generator for {@link IndexBasedAssociationNode IndexBasedAssociationNodes}. Example in formula
 * language: "policy.converage[0]" (get the first coverage from policy).
 * 
 * @author frank
 * @since 3.11.0
 */
public class IndexBasedAssociationNodeGenerator extends StdBuilderIdentifierNodeGenerator {

    public IndexBasedAssociationNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory,
            StandardBuilderSet builderSet) {
        super(factory, builderSet);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        IndexBasedAssociationNode node = (IndexBasedAssociationNode)identifierNode;
        JavaCodeFragment codeFragement = createAssociationGetterWithIndex(contextCompilationResult.getCodeFragment(),
                node);
        return new CompilationResultImpl(codeFragement, node.getDatatype());
    }

    private JavaCodeFragment createAssociationGetterWithIndex(JavaCodeFragment contextCodeFragment,
            IndexBasedAssociationNode node) {
        JavaCodeFragment newJavaCodeFragment = new JavaCodeFragment();
        newJavaCodeFragment.append(contextCodeFragment);
        newJavaCodeFragment.append('.');
        newJavaCodeFragment.append(getAssociationTargetAtIndexGetterName(node.getAssociation()));
        newJavaCodeFragment.append("(" + node.getIndex() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        return newJavaCodeFragment;
    }

    private String getAssociationTargetAtIndexGetterName(IAssociation association) {
        XPolicyAssociation xPolicyAssociation = getModelNode(association, XPolicyAssociation.class);
        return xPolicyAssociation.getMethodNameGetSingle();
    }
}
