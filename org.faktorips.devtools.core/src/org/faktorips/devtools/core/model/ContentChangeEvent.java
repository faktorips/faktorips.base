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
 * An event for content changes. 
 */
public class ContentChangeEvent {

    public final static int PART_CHANGED = 0;
    
    public final static int PART_ADDED = 1;
    
    public final static int PART_REMOVED = 2;
    
    private IIpsSrcFile ipsSrcFile;
    
    private IIpsObjectPart part;
    
    public ContentChangeEvent(IIpsSrcFile ipsSrcFile) {
        this.ipsSrcFile = ipsSrcFile;
    }

    /**
     * Returns the source file which contents has changed.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }
    
    /**
     * 
     * @return
     */
    public IIpsObjectPart getPart() {
        return part;
    }
    
    public Integer getEventTypeForPart() {
       return null; 
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "ContentChangeEvent for " + ipsSrcFile; //$NON-NLS-1$
    }
    
    
    
}
