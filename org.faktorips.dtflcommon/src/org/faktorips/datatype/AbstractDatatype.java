package org.faktorips.datatype;

import org.faktorips.util.message.MessageList;

/**
 * Abstract super class for Datatype implementations.
 *   
 * @author Jan Ortmann
 */
public abstract class AbstractDatatype implements Datatype {
    
    /**
     * Overridden.
     */
    public MessageList validate() {
        return new MessageList();
    }

    /**
     * Overridden.
     */
	public boolean isVoid() {
		return false;
	}

	/**
	 * Overridden.
	 */
	public int hashCode() {
	    return getName().hashCode();
	}
	
	/**
	 * Overridden.
	 */
	public boolean equals(Object o) {
		if (this==o) {
			return true;
		}
		if (!(o instanceof Datatype)) {
			return false;
		}
		return getQualifiedName().equals(((Datatype)o).getQualifiedName());
	}

	/**
	 * Returns the type's name. 
	 */
	public String toString() {
		return getQualifiedName();
	}
	
	/**
	 * Compares the two type's alphabetically by their name.
	 */
	public int compareTo(Object o) {
		Datatype type = (Datatype)o;
		return getQualifiedName().compareTo(type.getQualifiedName());
	}

}
