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

package org.faktorips.runtime;

/**
 * An exception that indicates that a method do modifiy the repository contents was
 * called, but the repository does not allow to modify it's contents.
 * 
 * @author Jan Ortmann
 */
public class IllegalRepositoryModificationException extends RuntimeException {

    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    public IllegalRepositoryModificationException() {
        super();
    }
}
