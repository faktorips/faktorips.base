package org.faktorips.fl.operations;

import java.math.BigDecimal;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Operation for the multiplication of two decimals. 
 */
public class MultiplyDecimalMoney extends AbstractBinaryOperation {

    public MultiplyDecimalMoney() {
        super("*", Datatype.DECIMAL, Datatype.MONEY);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl, org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        JavaCodeFragment fragment = rhs.getCodeFragment();
        fragment.append(".multiply(");
        rhs.add(lhs);
        fragment.append(", ");
        fragment.appendClassName(BigDecimal.class);
        fragment.append(".ROUND_HALF_UP)");
        rhs.setDatatype(Datatype.MONEY);
        return rhs;
    }

}
