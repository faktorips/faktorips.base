package org.faktorips.datatype;

/**
 * Abstract super class for Datatype implementations.
 *   
 * @author Jan Ortmann
 */
public abstract class AbstractDatatype implements Datatype {
	
    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#isVoid()
     */
	public boolean isVoid() {
		return false;
	}

	/**
	 * Overridden Method.
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
	    return getName().hashCode();
	}
	
	/**
	 * Overridden method.
	 * @see java.lang.Object#equals(java.lang.Object)
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
	 * Overridden method.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getQualifiedName();
	}
	
	/**
	 * Compares the two type's alphabetically by their name.
	 * Overridden method.
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		Datatype type = (Datatype)o;
		return getQualifiedName().compareTo(type.getQualifiedName());
	}

}
