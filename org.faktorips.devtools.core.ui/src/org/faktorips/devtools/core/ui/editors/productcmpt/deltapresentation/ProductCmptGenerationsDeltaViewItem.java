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

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGenerationToTypeDelta;
import org.faktorips.devtools.core.ui.controller.fields.GregorianCalendarFormat;

/**
 * Helper class for the {@link DeltaContentProvider} and {@link DeltaLabelProvider}, to provide
 * combined generation headers. This object is first created with an
 * {@link ProductCmptGenerationToTypeDelta} object and can afterwards be enriched by further
 * generations, which share the same differences.
 */
class ProductCmptGenerationToTypeDeltaWrapper {
    /**
     * List of all valid from dates of generations in this wrapper.
     */
    private List<GregorianCalendar> validFromDates;

    /**
     * The differences, which all generations represented here, share.
     */
    private final ProductCmptGenerationToTypeDelta delta;

    /**
     * A formatter instance to display the correct format of the dates to the user.
     */
    private static final GregorianCalendarFormat formatter = GregorianCalendarFormat.newInstance();

    ProductCmptGenerationToTypeDeltaWrapper(ProductCmptGenerationToTypeDelta delta) {
        validFromDates = new ArrayList<GregorianCalendar>();
        validFromDates.add(delta.getPropertyValueContainer().getValidFrom());
        this.delta = delta;
    }

    ProductCmptGenerationToTypeDelta getDelta() {
        return delta;
    }

    /**
     * Returns a comma separated list of all dates in this object.
     */
    String getDates() {
        StringBuilder builder = new StringBuilder();
        for (GregorianCalendar cal : validFromDates) {
            builder.append(formatter.format(cal, true));
            builder.append(", "); //$NON-NLS-1$
        }
        if (builder.length() > 0) {
            return builder.substring(0, builder.length() - 2);
        }
        return ""; //$NON-NLS-1$
    }

    void addDate(ProductCmptGenerationToTypeDelta dateProvider) {
        validFromDates.add(dateProvider.getPropertyValueContainer().getValidFrom());
    }
}
