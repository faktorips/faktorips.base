package org.faktorips.util.memento;

/**
 * An interface that marks an object as one supporting the memento pattern.  
 * 
 * @author Jan Ortmann
 */
public interface MementoSupport {

    /**
     * Creates a new memento that holds the object's current state.
     */
    public Memento newMemento();
    
    /**
     * Sets the object's state to the one stored in the memento.
     * 
     * @throws IllegalArgumentException if the memento does not contain this
     * object's state.
     */
    public void setState(Memento memento);

}
