/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.datatype.classtypes;

/**
 * A datatype using the GregorianCalendar class to represent dates without any information about the
 * time.
 * 
 * @author Jan Ortmann
 */
public class GregorianCalendarAsDateDatatype extends GregorianCalendarDatatype {

    /**
     * Constructs a new instance with the name "GregorianCalendar".
     */
    public GregorianCalendarAsDateDatatype() {
        this("GregorianCalendar"); //$NON-NLS-1$
    }

    /**
     * Constructs a new instance with the given name.
     */
    public GregorianCalendarAsDateDatatype(String name) {
        super(name, false);
    }

}
