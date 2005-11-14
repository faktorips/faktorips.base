package org.faktorips.fl.operations;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Operation for the not equality check of two money objects. 
 */
public class NotEqualsMoneyMoney extends AbstractBinaryOperation {

    public NotEqualsMoneyMoney() {
        super("!=", Datatype.MONEY, Datatype.MONEY);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl, org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append('!');
        fragment.append(lhs.getCodeFragment());
        fragment.append(".equals(");
        fragment.append(rhs.getCodeFragment());
        fragment.append(')');
        CompilationResultImpl result = new CompilationResultImpl(fragment, Datatype.PRIMITIVE_BOOLEAN);
        result.addMessages(lhs.getMessages());
        result.addMessages(rhs.getMessages());
        return result;
    }

}
