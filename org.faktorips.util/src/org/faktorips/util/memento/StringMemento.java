package org.faktorips.util.memento;



/**
 * A memento that stores the originator's state as string.
 */
public class StringMemento implements Memento {
    
    private Object originator;
    private String state;

    /**
     * Creates a new memento.
     * 
     * @param member the object this is a memento for.
     * @param state the originator's state as string.
     */
    public StringMemento(Object originator, String state) {
        this.originator = originator;
        this.state = state;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.util.memento.Memento#getOriginator()
     */
    public Object getOriginator() {
        return originator;
    }
    
    /**
     * Returns the originator's state stored in the memento. 
     */
    public String getState() {
        return state;
    }
    
}
