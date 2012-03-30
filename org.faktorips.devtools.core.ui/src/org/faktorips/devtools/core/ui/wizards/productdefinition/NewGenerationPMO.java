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

/**
 * {@linkplain PresentationModelObject Presentation Model Object} that configures the
 * {@linkplain NewGenerationWizard} and {@linkplain NewGenerationRunnable}.
 * <p>
 * This {@linkplain PresentationModelObject Presentation Model Object} stores
 * <ul>
 * <li>
 * the date from which new generations are valid
 * <li>a boolean flag controlling whether a new generation shall be created if a matching generation
 * already exists in the target object
 * </ul>
 * 
 * @see NewGenerationWizard
 * @see NewGenerationRunnable
 */
// Must be public as otherwise binding contexts cannot do their job
public class NewGenerationPMO extends PresentationModelObject {

    public static final String PROPERTY_VALID_FROM = "validFrom"; //$NON-NLS-1$

    public static final String PROPERTY_SKIP_EXISTING_GENERATIONS = "skipExistingGenerations"; //$NON-NLS-1$

    private GregorianCalendar validFrom;

    private boolean skipExistingGenerations;

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

    public boolean isSkipExistingGenerations() {
        return skipExistingGenerations;
    }

    public void setSkipExistingGenerations(boolean skipExistingGenerations) {
        boolean oldValue = this.skipExistingGenerations;
        this.skipExistingGenerations = skipExistingGenerations;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SKIP_EXISTING_GENERATIONS, oldValue,
                skipExistingGenerations));
    }

}