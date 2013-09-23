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
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.stdbuilder.BuilderKindIds;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * JavaBuilder for a {@link EnumClassNode} and {@link EnumValueNode}.
 * 
 * @author hbaagil
 * @since 3.11.0
 */

public class EnumNodeGenerator extends StdBuilderIdentifierNodeGenerator {

    private StandardBuilderSet builderSet;
    private ExtendedExprCompiler exprCompiler;

    public EnumNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory, StandardBuilderSet builderSet,
            ExtendedExprCompiler exprCompiler) {
        super(factory, builderSet);
        this.builderSet = builderSet;
        this.exprCompiler = exprCompiler;
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        EnumClassNode classNode = (EnumClassNode)identifierNode;
        EnumDatatype enumDatatype = classNode.getDatatype().getEnumDatatype();
        EnumValueNode valueNode = classNode.getSuccessor();

        JavaCodeFragment codeFragment = new JavaCodeFragment();
        codeFragment.getImportDeclaration().add(enumDatatype.getJavaClassName());
        if (enumDatatype instanceof EnumTypeDatatypeAdapter) {
            addNewInstanceForEnumType(codeFragment, (EnumTypeDatatypeAdapter)enumDatatype, exprCompiler,
                    valueNode.getEnumValueName());
        } else {
            DatatypeHelper helper = getIpsProject().getDatatypeHelper(enumDatatype);
            codeFragment.append(helper.newInstance(valueNode.getEnumValueName()));
        }
        return new CompilationResultImpl(codeFragment, enumDatatype);
    }

    protected void addNewInstanceForEnumType(JavaCodeFragment fragment,
            EnumTypeDatatypeAdapter datatype,
            ExtendedExprCompiler exprCompiler,
            String value) {
        getEnumTypeBuilder().setExtendedExprCompiler(exprCompiler);
        try {
            fragment.append(getEnumTypeBuilder().getNewInstanceCodeFragement(datatype, value));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public EnumTypeBuilder getEnumTypeBuilder() {
        return builderSet.getBuilderById(BuilderKindIds.ENUM_TYPE, EnumTypeBuilder.class);
    }
}
