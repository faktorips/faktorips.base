/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

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
     * @throws IllegalArgumentException if the memento does not contain this object's state.
     */
    public void setState(Memento memento);

}
