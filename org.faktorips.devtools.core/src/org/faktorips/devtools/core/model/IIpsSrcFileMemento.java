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

package org.faktorips.devtools.core.model;

/**
 * A memento for a source file that stores the contents and the dirty state.
 * <p>
 * This is an application of the memento pattern.
 */
public class IIpsSrcFileMemento {
    
    private IIpsSrcFile file;
    private String contents;
    private boolean dirty;
    
    public IIpsSrcFileMemento(IIpsSrcFile file, String contents, boolean dirty) {
        this.file = file;
        this.contents = contents;
        this.dirty = dirty;
    }
    
    public IIpsSrcFile getIpsSrcFile() {
        return file;
    }

    public String getContents() {
        return contents;
    }
    
    public boolean isDirty() {
        return dirty;
    }
}
