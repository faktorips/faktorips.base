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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.beans.PropertyChangeEvent;
import java.util.GregorianCalendar;

import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

public class NewGenerationPMO extends PresentationModelObject {

    public static final String PROPERTY_VALID_FROM = "validFrom"; //$NON-NLS-1$

    private GregorianCalendar validFrom;

    public NewGenerationPMO() {
        if (IpsUIPlugin.getDefault() != null) { // may be null in test cases :(
            validFrom = IpsUIPlugin.getDefault().getDefaultValidityDate();
        }
    }

    public GregorianCalendar getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(GregorianCalendar validFrom) {
        GregorianCalendar oldValue = this.validFrom;
        this.validFrom = validFrom;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_VALID_FROM, oldValue, validFrom));
    }

}