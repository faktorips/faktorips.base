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

import java.util.Map;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controller.fields.IInputFormat;

public class InputFormat {

    private Map<ValueDatatype, IInputFormatFactory<String>> inputFormatFactories;

    public InputFormat(Map<ValueDatatype, IInputFormatFactory<String>> inputFormatFactories) {
        this.inputFormatFactories = inputFormatFactories;
    }

    public IInputFormat<String> getInputFormat(ValueDatatype datatype) {
        for (Datatype mapDatatype : inputFormatFactories.keySet()) {
            if (datatype.getQualifiedName().equals(mapDatatype.getQualifiedName())) {
                IInputFormatFactory<String> inputFormatFactory = inputFormatFactories.get(mapDatatype);
                return inputFormatFactory.newInputFormat(datatype);
            }
        }

        return null;
    }
}
