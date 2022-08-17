/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.functions.Messages;
import org.faktorips.runtime.Message;
import org.faktorips.values.ObjectUtil;

/**
 * Equals operation for none primitive datatypes that are tested for equality with the equals()
 * Method.
 */
public class EqualsObjectDatatype extends AbstractBinaryJavaOperation {

    protected EqualsObjectDatatype(String operator, Datatype lhsDatatype, Datatype rhsDatatype) {
        super(operator, lhsDatatype, rhsDatatype);
    }

    public EqualsObjectDatatype(Datatype lhsDatatype, Datatype rhsDatatype) {
        super("=", lhsDatatype, rhsDatatype); //$NON-NLS-1$
    }

    public EqualsObjectDatatype(Datatype type) {
        super("=", type, type); //$NON-NLS-1$
    }

    protected static String getErrorMessageCode() {
        return ExprCompiler.PREFIX + "EQUALS-OPERATION"; //$NON-NLS-1$
    }

    @Override
    public CompilationResultImpl generate(final CompilationResultImpl lhs, final CompilationResultImpl rhs) {
        ConversionCodeGenerator<JavaCodeFragment> ccg = getCompiler().getConversionCodeGenerator();
        Datatype datatype1 = lhs.getDatatype();
        Datatype datatype2 = rhs.getDatatype();

        CompilationResultImpl left = lhs;
        CompilationResultImpl right = rhs;
        if (!datatype1.equals(datatype2)) {
            if (ccg.canConvert(datatype1, datatype2)) {
                JavaCodeFragment converted = ccg.getConversionCode(datatype1, datatype2, lhs.getCodeFragment());
                CompilationResultImpl newResult = new CompilationResultImpl(converted, datatype2, lhs.getMessages());
                left = newResult;
            } else if (ccg.canConvert(datatype2, datatype1)) {
                JavaCodeFragment converted = ccg.getConversionCode(datatype2, datatype1, rhs.getCodeFragment());
                CompilationResultImpl newResult = new CompilationResultImpl(converted, datatype1, rhs.getMessages());
                right = newResult;
            } else {
                String text = Messages.INSTANCE.getString(getErrorMessageCode(), datatype1, datatype2);
                Message msg = Message.newError(getErrorMessageCode(), text);
                return new CompilationResultImpl(msg);
            }
        }
        CompilationResultImpl result = new CompilationResultImpl();
        result.setDatatype(Datatype.PRIMITIVE_BOOLEAN);
        JavaCodeFragment frag = result.getCodeFragment();
        frag.appendClassName(ObjectUtil.class);
        frag.append(".equals("); //$NON-NLS-1$
        frag.append(right.getCodeFragment());
        frag.append(", ");
        frag.append(left.getCodeFragment());
        frag.append(')');
        return result;
    }

}
