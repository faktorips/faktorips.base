/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.flidentifier;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
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
 * JavaGenerator for an {@link EnumValueNode}.
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
        EnumValueNode valueNode = (EnumValueNode)identifierNode;
        EnumDatatype enumDatatype = valueNode.getDatatype();
        JavaCodeFragment codeFragment = new JavaCodeFragment();
        codeFragment.getImportDeclaration().add(enumDatatype.getJavaClassName());
        if (isEnumTypeDatatypeAdapter(enumDatatype)) {
            addNewInstanceForEnumType(codeFragment, (EnumTypeDatatypeAdapter)enumDatatype, valueNode.getEnumValueName());
        } else {
            DatatypeHelper helper = getIpsProject().getDatatypeHelper(enumDatatype);
            codeFragment.append(helper.newInstance(valueNode.getEnumValueName()));
        }
        return new CompilationResultImpl(codeFragment, enumDatatype);
    }

    private boolean isEnumTypeDatatypeAdapter(EnumDatatype enumDatatype) {
        return enumDatatype instanceof EnumTypeDatatypeAdapter;
    }

    protected void addNewInstanceForEnumType(JavaCodeFragment fragment, EnumTypeDatatypeAdapter datatype, String value) {
        EnumTypeBuilder enumTypeBuilder = getEnumTypeBuilder();
        enumTypeBuilder.setExtendedExprCompiler(exprCompiler);
        try {
            fragment.append(enumTypeBuilder.getNewInstanceCodeFragement(datatype, value));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public EnumTypeBuilder getEnumTypeBuilder() {
        return builderSet.getBuilderById(BuilderKindIds.ENUM_TYPE, EnumTypeBuilder.class);
    }
}
