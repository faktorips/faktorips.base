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

import java.util.List;

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
public class AssociationNodeGenerator extends AbstractIdentifierGenerator {

    public AssociationNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> nodeBuilderFactory,
            StandardBuilderSet builderSet) {
        super(nodeBuilderFactory, builderSet);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResult(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        final AssociationNode node = (AssociationNode)identifierNode;
        IAssociation association = node.getAssociation();
        IType target = getTargetType(association);
        return getCompilationResultForAssociation(contextCompilationResult, association, target);
    }

    private CompilationResult<JavaCodeFragment> getCompilationResultForAssociation(CompilationResult<JavaCodeFragment> contextCompilationResult,
            IAssociation association,
            IType associationTarget) {
        if (isListDatatypeContext(contextCompilationResult)) {
            return compileListContext(contextCompilationResult, association, associationTarget);
        } else {
            return compileSingleObjectContext(contextCompilationResult, association, associationTarget);
        }
    }

    protected CompilationResult<JavaCodeFragment> compileListContext(CompilationResult<JavaCodeFragment> contextCompilationResult,
            IAssociation association,
            IType associationTarget) {
        JavaCodeFragment codeFragment = compileAssociationChain(contextCompilationResult, association);
        return createCompilationResultWithDatatype(codeFragment, new ListOfTypeDatatype(associationTarget));
    }

    private JavaCodeFragment compileAssociationChain(CompilationResult<JavaCodeFragment> contextCompilationResult,
            IAssociation association) {

        IType sourceType = getSourceElementDatatype(contextCompilationResult);
        IType targetType = getTargetType(association);
        /**
         * Example Code:
         * 
         * <pre>
         * new AssociationToManyHelper&lt;IPolicy, ICoverage&gt;() {
         *     &#064;Override
         *     protected List&lt;ICoverage&gt; getTargetsInternal(IPolicy sourceObject) {
         *         return sourceObject.getCoverages();
         *     }
         * }.getTargets(javaCodeFragment)
         * </pre>
         */
        JavaCodeFragment getTargetCode = new JavaCodeFragment("new "); //$NON-NLS-1$
        if (association.is1ToManyIgnoringQualifier()) {
            getTargetCode
                    .appendClassName(org.faktorips.runtime.formula.FormulaEvaluatorUtil.AssociationToManyHelper.class);
        } else {
            getTargetCode
                    .appendClassName(org.faktorips.runtime.formula.FormulaEvaluatorUtil.AssociationTo1Helper.class);
        }
        getTargetCode.append("<"); //$NON-NLS-1$
        String sourceClassName = getJavaClassName(sourceType);
        getTargetCode.appendClassName(sourceClassName);
        getTargetCode.append(", "); //$NON-NLS-1$
        String targetClassName = getJavaClassName(targetType);
        getTargetCode.appendClassName(targetClassName);
        getTargetCode.append(">(){@Override protected "); //$NON-NLS-1$
        if (association.is1ToManyIgnoringQualifier()) {
            getTargetCode.appendClassName(List.class);
            getTargetCode.append("<"); //$NON-NLS-1$
        }
        getTargetCode.appendClassName(targetClassName);
        if (association.is1ToManyIgnoringQualifier()) {
            getTargetCode.append("> getTargetsInternal("); //$NON-NLS-1$
        } else {
            getTargetCode.append(" getTargetInternal("); //$NON-NLS-1$
        }
        getTargetCode.appendClassName(sourceClassName);
        getTargetCode.append(" sourceObject){return sourceObject."); //$NON-NLS-1$
        getTargetCode.append(getAssociationTargetGetterName(association));
        getTargetCode.append("();}}.getTargets("); //$NON-NLS-1$
        getTargetCode.append(contextCompilationResult.getCodeFragment());
        getTargetCode.append(")"); //$NON-NLS-1$

        return getTargetCode;
    }

    private IType getSourceElementDatatype(CompilationResult<JavaCodeFragment> contextCompilationResult) {
        if (contextCompilationResult.getDatatype() instanceof ListOfTypeDatatype) {
            ListOfTypeDatatype listDatatype = (ListOfTypeDatatype)contextCompilationResult.getDatatype();
            return (IType)listDatatype.getBasicDatatype();
        } else {
            return (IType)contextCompilationResult.getDatatype();
        }
    }

    private IType getTargetType(IAssociation association) {
        try {
            return association.findTarget(association.getIpsProject());
        } catch (CoreException e) {
            throw new GeneratorRuntimeException(e.getMessage());
        }
    }

    protected CompilationResult<JavaCodeFragment> compileSingleObjectContext(CompilationResult<JavaCodeFragment> contextCompilationResult,
            IAssociation association,
            IType target) {
        if (association.is1To1()) {
            return compileAssociationTo1(contextCompilationResult, association, target);
        } else {
            return compileAssociationToMany(contextCompilationResult, association, target);
        }
    }

    protected CompilationResult<JavaCodeFragment> compileAssociationTo1(CompilationResult<JavaCodeFragment> contextCompilationResult,
            IAssociation association,
            IType target) {
        JavaCodeFragment javaCodeFragment = copyContextCodeFragment(contextCompilationResult);
        javaCodeFragment.append('.');
        javaCodeFragment.append(getAssociationTargetGetterName(association));
        javaCodeFragment.append("()"); //$NON-NLS-1$
        return createCompilationResultWithDatatype(javaCodeFragment, target);
    }

    protected CompilationResult<JavaCodeFragment> compileAssociationToMany(CompilationResult<JavaCodeFragment> contextCompilationResult,
            IAssociation association,
            IType target) {
        JavaCodeFragment javaCodeFragment = copyContextCodeFragment(contextCompilationResult);
        String associationTargetGetterName = getAssociationTargetGetterName(association);
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

}
