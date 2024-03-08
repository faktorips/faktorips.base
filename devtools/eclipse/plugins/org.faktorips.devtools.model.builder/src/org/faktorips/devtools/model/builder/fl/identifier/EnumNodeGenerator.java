/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.fl.identifier;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.model.builder.ExtendedExprCompiler;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumType;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * JavaGenerator for an {@link EnumValueNode}.
 * 
 * @author hbaagil
 * @since 3.11.0
 */

public class EnumNodeGenerator extends JavaBuilderIdentifierNodeGenerator {

    private ExtendedExprCompiler exprCompiler;

    public EnumNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory, JavaBuilderSet builderSet,
            ExtendedExprCompiler exprCompiler) {
        super(factory, builderSet);
        this.exprCompiler = exprCompiler;
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        EnumValueNode valueNode = (EnumValueNode)identifierNode;
        EnumDatatype enumDatatype = valueNode.getDatatype();
        JavaCodeFragment codeFragment = new JavaCodeFragment();
        if (isEnumTypeDatatypeAdapter(enumDatatype)) {
            addNewInstanceForEnumType(codeFragment, (EnumTypeDatatypeAdapter)enumDatatype,
                    valueNode.getEnumValueName());
        } else {
            DatatypeHelper helper = getIpsProject().getDatatypeHelper(enumDatatype);
            codeFragment.append(helper.newInstance(valueNode.getEnumValueName()));
        }
        return new CompilationResultImpl(codeFragment, enumDatatype);
    }

    private boolean isEnumTypeDatatypeAdapter(EnumDatatype enumDatatype) {
        return enumDatatype instanceof EnumTypeDatatypeAdapter;
    }

    protected void addNewInstanceForEnumType(JavaCodeFragment fragment,
            EnumTypeDatatypeAdapter datatype,
            String value) {
        XEnumType enumType = getBuilderSet().getModelNode(datatype.getEnumType(), XEnumType.class);
        fragment.append(enumType.getNewInstanceCodeFragement(datatype, value, exprCompiler));
    }
}
