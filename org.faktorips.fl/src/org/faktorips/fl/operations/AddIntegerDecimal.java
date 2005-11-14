package org.faktorips.fl.operations;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Operation for the addition of two decimals. 
 */
public class AddIntegerDecimal extends AbstractBinaryOperation {

    public AddIntegerDecimal() {
        super("+", Datatype.INTEGER, Datatype.DECIMAL);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl, org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append('(');
        fragment.append(lhs.getCodeFragment());
        fragment.append("==null?null:");
        fragment.append(rhs.getCodeFragment());
        fragment.append(".add(");
        fragment.append(lhs.getCodeFragment());
        fragment.append("))");
        return new CompilationResultImpl(fragment, Datatype.DECIMAL);
    }

}
