package org.faktorips.datatype;

public class PaymentMode implements NullObject {

    final static PaymentMode ANNUAL = new PaymentMode("annual");
    final static PaymentMode MONTHLY = new PaymentMode("monthly");
    final static PaymentMode NULL = new PaymentMode("null");

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
    
    private String id;
    
    PaymentMode(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public String toString() {
        return id;
    }

    public boolean isNull() {
        return this==PaymentMode.NULL;
    }
}
