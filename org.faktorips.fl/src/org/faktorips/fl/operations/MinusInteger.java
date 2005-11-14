package org.faktorips.fl.operations;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Minus (-) operator for datatype Integer.
 */
public class MinusInteger extends AbstractUnaryOperation {

    public MinusInteger() {
        super(Datatype.INTEGER, "-");
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
        fragment.append('(');
        fragment.append(arg.getCodeFragment());
        fragment.append("==null?null:new Integer(-1 * ");
        fragment.append(arg.getCodeFragment());
        fragment.append(".intValue()))");
        CompilationResultImpl result = new CompilationResultImpl(fragment, Datatype.INTEGER);
        result.addMessages(arg.getMessages());
        return result;
    }

}
