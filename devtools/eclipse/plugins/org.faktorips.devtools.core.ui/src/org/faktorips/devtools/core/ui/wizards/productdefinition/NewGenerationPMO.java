/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.beans.PropertyChangeEvent;
import java.util.GregorianCalendar;

import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;

/**
 * {@linkplain PresentationModelObject Presentation Model Object} that configures the
 * {@linkplain NewGenerationWizard} and {@linkplain NewGenerationRunnable}.
 * <p>
 * This {@linkplain PresentationModelObject Presentation Model Object} stores
 * <ul>
 * <li>the date from which new generations are valid
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
        if (IpsUIPlugin.getDefault() != null) {
            // may be null in test cases :(
            validFrom = IpsUIPlugin.getDefault().getDefaultValidityDate();
        }
    }

    /**
     * Returns the date from which the new {@link IIpsObjectGeneration IPS Object Generations} shall
     * be valid from.
     */
    public GregorianCalendar getValidFrom() {
        return validFrom;
    }

    /**
     * @see #getValidFrom()
     */
    public void setValidFrom(GregorianCalendar validFrom) {
        GregorianCalendar oldValue = this.validFrom;
        this.validFrom = validFrom;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_VALID_FROM, oldValue, validFrom));
    }

    /**
     * Returns whether no new {@link IIpsObjectGeneration IPS Object Generation} shall be created
     * for objects that already contain an {@link IIpsObjectGeneration IPS Object Generation} valid
     * from the configured date.
     */
    public boolean isSkipExistingGenerations() {
        return skipExistingGenerations;
    }

    /**
     * @see #isSkipExistingGenerations()
     */
    public void setSkipExistingGenerations(boolean skipExistingGenerations) {
        boolean oldValue = this.skipExistingGenerations;
        this.skipExistingGenerations = skipExistingGenerations;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SKIP_EXISTING_GENERATIONS, oldValue,
                skipExistingGenerations));
    }

}
