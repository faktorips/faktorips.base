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

package org.faktorips.devtools.core.ui.inputformat;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * IInputFormatFactory registered with the <i>inputFormat</i> extension point.
 * 
 * @since 3.11
 */

public interface IDatatypeInputFormatFactory {

    /**
     * Instantiate a specific data type with respect to the provided data type. It is in the
     * responsibility of the factory provider if the data type is considered.
     * 
     * @param datatype the data type for which you want retrieve an input format
     * @param ipsProject the project provided to the input format for example to get the default currency in money values   
     */
    public IInputFormat<String> newInputFormat(ValueDatatype datatype, IIpsProject ipsProject);

}
