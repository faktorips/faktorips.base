/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.codegen.BaseDatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;

/**
 * 
 * @author Jan Ortmann
 */
public class IsEmpty extends AbstractFlFunction {

    public static final String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "ISEMPTY"; //$NON-NLS-1$

    public IsEmpty(String name, String description) {
        super(name, description, FunctionSignatures.IsEmpty);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        Datatype argType = argResults[0].getDatatype();
        if (argType.isPrimitive()) {
            // values of primitive types can never be null
            return new CompilationResultImpl("false", Datatype.PRIMITIVE_BOOLEAN);
        }
        JavaCodeFragment code = new JavaCodeFragment();
        if (argType instanceof ListOfTypeDatatype) {
            code.append(argResults[0].getCodeFragment());
            code.append(".isEmpty()");
        } else if (argType.hasNullObject()) {
            BaseDatatypeHelper<JavaCodeFragment> helper = getCompiler().getDatatypeHelper(argType);
            code.append(helper.nullExpression());
            code.append(".equals(");
            code.append(argResults[0].getCodeFragment());
            code.append(")");
        } else if (argType instanceof StringDatatype) {
            code.append(compileIsEmptyForStrings(argResults[0].getCodeFragment()));
        } else {
            code.append(argResults[0].getCodeFragment());
            code.append("==null");
        }
        return new CompilationResultImpl(code, Datatype.PRIMITIVE_BOOLEAN);
    }

    private JavaCodeFragment compileIsEmptyForStrings(JavaCodeFragment paramName) {
        return new JavaCodeFragment()
                .append(paramName)
                .append("==null")
                .append("||")
                .append(paramName)
                .append(".isEmpty()");
    }

}
