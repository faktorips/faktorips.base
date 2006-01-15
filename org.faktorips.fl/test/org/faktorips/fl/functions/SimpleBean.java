package org.faktorips.fl.functions;

import org.faktorips.values.Decimal;

/**
 * SimpleBean for testing purposes.
 *  
 * @author Jan Ortmann
 */
public class SimpleBean {

    public SimpleBean() {
        super();
    }

    public SimpleBean(Decimal value) {
        this.value = value;
    }
    
    private Decimal value;
    
    public Decimal getValue() {
        return value;
    }
    
    public void setValue(Decimal value) {
        this.value = value;
    }

}
