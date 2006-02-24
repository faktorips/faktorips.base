package org.faktorips.datatype;

import org.faktorips.values.NullObject;

public class PaymentMode implements NullObject {

    final static PaymentMode ANNUAL = new PaymentMode("annual", "Annual Payment");
    final static PaymentMode MONTHLY = new PaymentMode("monthly", "Monthly Payment");
    final static PaymentMode NULL = new PaymentMode("null", "No Payment");

    private String id;
    private String name;
    

    public final static PaymentMode[] getAllPaymentModes() {
        return new PaymentMode[]{MONTHLY, ANNUAL};
    }
    
    public final static boolean isParsable(String id) {
        try {
            PaymentMode.getPaymentMode(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    public final static PaymentMode getPaymentMode(String id) {
        if (id==null) {
            return null;
        }
        if (ANNUAL.id.equals(id)) {
            return ANNUAL;
        }
        if (MONTHLY.id.equals(id)) {
            return MONTHLY;
        }
        throw new IllegalArgumentException("The id " + id + " does not identify a PaymentMode");
    }
    
    PaymentMode(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public String getId() {
        return id;
    }
    
    public boolean isSupportingNames(){
    	return true;
    }
    
    public String getName(){
    	return name;
    }
    
    public String toString() {
        return id;
    }

    public boolean isNull() {
        return this==PaymentMode.NULL;
    }
}
