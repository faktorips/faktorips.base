/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * An event that signals the change of an ips source file's modification status from
 * modifier to unmodified or vice versa.
 * 
 * @author Jan Ortmann
 */
public class ModificationStatusChangedEvent {

    private IIpsSrcFile file;
    
    public ModificationStatusChangedEvent(IIpsSrcFile file) {
        this.file = file;
    }

    /**
     * Returns the file which modifcation status has changed.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return file;
    }
    
}
