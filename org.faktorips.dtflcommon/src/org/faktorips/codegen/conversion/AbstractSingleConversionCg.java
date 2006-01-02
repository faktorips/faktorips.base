package org.faktorips.codegen.conversion;

import org.faktorips.codegen.SingleConversionCg;
import org.faktorips.datatype.Datatype;


/**
 * A ConversionGenerator that ...
 */
public abstract class AbstractSingleConversionCg implements
        SingleConversionCg {

    private Datatype from;
    private Datatype to;
    
    /**
     * Creates a new ConversionGenerator that converts from Datatype from to 
     * Datatype to.
     */
    public AbstractSingleConversionCg(Datatype from, Datatype to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Overridden method.
     * @see org.faktorips.codegen.SingleConversionCg#getFrom()
     */
    public Datatype getFrom() {
        return from;
    }

    /**
     * Overridden method.
     * @see org.faktorips.codegen.SingleConversionCg#getTo()
     */
    public Datatype getTo() {
        return to;
    }
    
}
