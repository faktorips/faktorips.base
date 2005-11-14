package org.faktorips.fl.operations;

import java.math.BigDecimal;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Operation for the multiplication of two decimals. 
 */
public class MultiplyMoneyDecimal extends AbstractBinaryOperation {

    public MultiplyMoneyDecimal() {
        super("*", Datatype.MONEY, Datatype.DECIMAL);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl, org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        JavaCodeFragment fragment = lhs.getCodeFragment();
        fragment.append(".multiply(");
        lhs.add(rhs);
        fragment.append(", ");
        fragment.appendClassName(BigDecimal.class);
        fragment.append(".ROUND_HALF_UP)");
        lhs.setDatatype(Datatype.MONEY);
        return lhs;
    }

}
