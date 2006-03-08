/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
