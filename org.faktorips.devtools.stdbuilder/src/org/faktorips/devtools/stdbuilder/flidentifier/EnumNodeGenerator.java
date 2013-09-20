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
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.stdbuilder.BuilderKindIds;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;

public class EnumNodeGenerator extends StdBuilderIdentifierNodeGenerator {

    StandardBuilderSet builderSet;

    public EnumNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory, StandardBuilderSet builderSet) {
        super(factory, builderSet);
        this.builderSet = builderSet;
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        EnumClassNode classNode = (EnumClassNode)identifierNode;
        EnumDatatype datatype = classNode.getDatatype().getEnumDatatype();
        EnumValueNode valueNode = classNode.getSuccessor();

        JavaCodeFragment codeFragment = new JavaCodeFragment();
        codeFragment.getImportDeclaration().add(datatype.getJavaClassName());
        if (datatype instanceof EnumTypeDatatypeAdapter) {
            ExtendedExprCompiler exprCompiler = new ExtendedExprCompiler();
            try {
                addNewInstanceForEnumType(codeFragment, (EnumTypeDatatypeAdapter)datatype, exprCompiler,
                        valueNode.getEnumValueName());
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            String enumValueName = valueNode.getEnumValueName();
            JavaCodeFragment javaCodeFragment = new JavaCodeFragment(enumValueName);
            codeFragment.append(javaCodeFragment);
        }

        return new CompilationResultImpl(codeFragment, datatype);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getErrorCompilationResult(InvalidIdentifierNode invalidIdentifierNode) {
        return new CompilationResultImpl(invalidIdentifierNode.getMessage());
    }

    protected void addNewInstanceForEnumType(JavaCodeFragment fragment,
            EnumTypeDatatypeAdapter datatype,
            ExprCompiler<JavaCodeFragment> exprCompiler,
            String value) throws CoreException {
        getEnumTypeBuilder().setExtendedExprCompiler((ExtendedExprCompiler)exprCompiler);
        fragment.append(getEnumTypeBuilder().getNewInstanceCodeFragement(datatype, value));
    }

    public EnumTypeBuilder getEnumTypeBuilder() {
        return builderSet.getBuilderById(BuilderKindIds.ENUM_TYPE, EnumTypeBuilder.class);
    }
}
