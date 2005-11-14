package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.UnaryOperation;


/**
 *
 */
public abstract class AbstractUnaryOperation implements UnaryOperation {
    
    private Datatype datatype;
    private String operator;

    public AbstractUnaryOperation(Datatype datatype, String operator) {
        this.datatype = datatype;
        this.operator = operator;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.UnaryOperation#getDatatype()
     */
    public Datatype getDatatype() {
       return datatype;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.fl.UnaryOperation#getOperator()
     */
    public String getOperator() {
        return operator;
    }

}
