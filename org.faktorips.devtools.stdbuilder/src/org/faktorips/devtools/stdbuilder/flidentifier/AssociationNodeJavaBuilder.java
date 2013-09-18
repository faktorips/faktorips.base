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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.GeneratorRuntimeException;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * JavaBuilder for an {@link AssociationNode}
 * 
 * @author widmaier
 * @since 3.11.0
 */
public class AssociationNodeJavaBuilder extends AbstractIdentifierGenerator {

    public AssociationNodeJavaBuilder(IdentifierNodeGeneratorFactory<JavaCodeFragment> nodeBuilderFactory,
            StandardBuilderSet builderSet) {
        super(nodeBuilderFactory, builderSet);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResult(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        final AssociationNode node = (AssociationNode)identifierNode;
        IAssociation association = node.getAssociation();
        try {
            IType target = association.findTarget(getIpsProject());
            return getCompilationResultForAssociation(contextCompilationResult, association, target);
        } catch (CoreException e) {
            throw new GeneratorRuntimeException(e.getMessage());
        }
    }

    protected CompilationResult<JavaCodeFragment> getCompilationResultForAssociation(CompilationResult<JavaCodeFragment> contextCompilationResult,
            IAssociation association,
            IType target) {
        if (association.is1To1()) {
            return compileTypeAssociationTo1(contextCompilationResult, association, target);
        } else {
            return compileTypeAssociationToMany(contextCompilationResult, association, target);
        }
    }

    private CompilationResult<JavaCodeFragment> compileTypeAssociationTo1(CompilationResult<JavaCodeFragment> contextCompilationResult,
            IAssociation association,
            IType target) {
        JavaCodeFragment javaCodeFragment = copyContextCodeFragment(contextCompilationResult);
        javaCodeFragment.append('.');
        javaCodeFragment.append(getAssociationTargetGetterName(association));
        javaCodeFragment.append("()"); //$NON-NLS-1$
        return createCompilationResultWithDatatype(javaCodeFragment, target);
    }

    private CompilationResult<JavaCodeFragment> compileTypeAssociationToMany(CompilationResult<JavaCodeFragment> contextCompilationResult,
            IAssociation association,
            IType target) {
        JavaCodeFragment javaCodeFragment = copyContextCodeFragment(contextCompilationResult);
        String associationTargetGetterName = getAssociationTargetsGetterName(association);
        javaCodeFragment.append('.' + associationTargetGetterName + "()"); //$NON-NLS-1$
        return createCompilationResultWithDatatype(javaCodeFragment, new ListOfTypeDatatype(target));
    }

    private JavaCodeFragment copyContextCodeFragment(CompilationResult<JavaCodeFragment> contextCompilationResult) {
        return new JavaCodeFragment(contextCompilationResult.getCodeFragment());
    }

    private CompilationResultImpl createCompilationResultWithDatatype(JavaCodeFragment javaCodeFragment,
            Datatype datatype) {
        return new CompilationResultImpl(javaCodeFragment, datatype);
    }

    protected String getAssociationTargetGetterName(IAssociation association) {
        XPolicyAssociation xPolicyAssociation = getModelNode(association, XPolicyAssociation.class);
        return xPolicyAssociation.getMethodNameGetter();
    }

    protected String getAssociationTargetsGetterName(IAssociation association) {
        XPolicyAssociation xPolicyAssociation = getModelNode(association, XPolicyAssociation.class);
        return xPolicyAssociation.getMethodNameGetter();
    }

}
