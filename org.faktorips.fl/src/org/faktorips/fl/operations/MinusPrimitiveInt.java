package org.faktorips.fl.operations;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Minus (-) operator for datatype primitive int.
 */
public class MinusPrimitiveInt extends AbstractUnaryOperation {

    public MinusPrimitiveInt() {
        super(Datatype.PRIMITIVE_INT, "-");
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.UnaryOperation#generate(org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl arg) {
        if (arg.failed()) {
            return arg;
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append('-');
        fragment.append(arg.getCodeFragment());
        CompilationResultImpl result = new CompilationResultImpl(fragment, Datatype.PRIMITIVE_INT);
        result.addMessages(arg.getMessages());
        return result;
    }

}
