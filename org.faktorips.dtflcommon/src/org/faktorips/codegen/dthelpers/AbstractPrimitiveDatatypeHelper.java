package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.PrimitiveDatatypeHelper;
import org.faktorips.datatype.Datatype;

/**
 * Abstract base class for all primtive datatypes.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractPrimitiveDatatypeHelper extends AbstractDatatypeHelper implements
        PrimitiveDatatypeHelper {

    public AbstractPrimitiveDatatypeHelper() {
        super();
    }

    public AbstractPrimitiveDatatypeHelper(Datatype datatype) {
        super(datatype);
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstanceFromExpression(String expression) {
        return valueOfExpression(expression);
    }

}
