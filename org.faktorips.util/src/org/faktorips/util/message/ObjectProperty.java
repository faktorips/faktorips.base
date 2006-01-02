package org.faktorips.util.message;

/**
 * An instance of this class identifies a property in an object, e.g.
 * the name property of a specific person.
 */
public class ObjectProperty {
    
    private Object object;
    private String property;

    public ObjectProperty(Object object, String property) {
        this.object = object;
        this.property = property;
    }
    
    public Object getObject() {
        return object;
    }
    
    public String getProperty() {
        return property;
    }
    
    public String toString() {
        if (object==null) {
            return "null." + property;
        }
        return object.toString() + "." + property;
    }

}
