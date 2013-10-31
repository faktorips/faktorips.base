/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controller.fields.IInputFormat;

/**
 * IInputFormatFactory registered with the <i>inputFormat</i> extension point.
 * 
 */

public interface IInputFormatFactory<T extends Object> {

    /**
     * Instantiate a specific Datatype with respect to the provided datatype. It is in the
     * responsibility of the factory provider if the datatype is considered.
     */
    public IInputFormat<T> newInputFormat(ValueDatatype datatype);
}
