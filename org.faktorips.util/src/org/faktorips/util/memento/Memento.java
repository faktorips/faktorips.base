package org.faktorips.util.memento;

/**
 * A memento stores the state of another object called the originator
 * in memory. The memento can be used to reset the originators state
 * to the state saved in the memento.
 */
public interface Memento {

    /**
     * Returns the object this is a memento of.
     */
    public Object getOriginator();

}
