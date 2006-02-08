package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Operation for the equality check for two decimals. 
 */
public class EqualsStringString extends AbstractBinaryOperation {

    public EqualsStringString () {
        super("=", Datatype.STRING, Datatype.STRING);
    }

    /** 
     * {@inheritDoc}
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        lhs.getCodeFragment().append(".equals(");
        lhs.add(rhs);
        lhs.getCodeFragment().append(')');
        lhs.setDatatype(Datatype.PRIMITIVE_BOOLEAN);
        return lhs;
    }

}
