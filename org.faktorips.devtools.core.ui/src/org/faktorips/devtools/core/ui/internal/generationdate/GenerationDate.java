/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal.generationdate;

import java.text.DateFormat;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.Assert;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * This class represents a generation date, containing valid from and valid to. It also have the
 * ability to parse the two dates to a readable string to view on UI
 * 
 * @author dirmeier
 */
public class GenerationDate {

    private final GregorianCalendar validFrom;

    private GregorianCalendar validTo;

    public GenerationDate(GregorianCalendar validFrom, GregorianCalendar validTo) {
        Assert.isNotNull(validFrom);
        this.validFrom = validFrom;
        setValidTo(validTo);
    }

    public GregorianCalendar getValidFrom() {
        return validFrom;
    }

    public GregorianCalendar getValidTo() {
        return validTo;
    }

    public void setValidTo(GregorianCalendar validTo) {
        this.validTo = validTo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GenerationDate) {
            GenerationDate other = (GenerationDate)obj;
            return validFrom.getTimeInMillis() == other.validFrom.getTimeInMillis()
                    && (validTo != null && other.validTo != null ? validTo.getTimeInMillis() == other.validTo
                            .getTimeInMillis() : validTo == other.validTo);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + validFrom.hashCode();
        if (getValidTo() != null) {
            result = 31 * result + getValidTo().hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return "GenerationDate: " + getText(); //$NON-NLS-1$
    }

    public String getText() {
        StringBuilder result = new StringBuilder(getDateFormat().format(getValidFrom().getTime()));
        result.append(" - "); //$NON-NLS-1$
        if (getValidTo() != null) {
            result.append(getDateFormat().format(getValidTo().getTime()));
        } else {
            result.append(Messages.GenerationDate_infinite);
        }
        return result.toString();
    }

    public DateFormat getDateFormat() {
        return IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
    }

}
