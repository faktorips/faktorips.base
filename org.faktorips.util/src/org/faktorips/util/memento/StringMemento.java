/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

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
     * @param originator the object this is a memento for.
     * @param state the originator's state as string.
     */
    public StringMemento(Object originator, String state) {
        this.originator = originator;
        this.state = state;
    }

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
