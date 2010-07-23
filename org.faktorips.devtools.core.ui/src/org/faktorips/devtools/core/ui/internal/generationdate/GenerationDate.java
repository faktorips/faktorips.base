/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
        StringBuffer result = new StringBuffer(getDateFormat().format(getValidFrom().getTime()));
        result.append(" - "); //$NON-NLS-1$
        if (getValidTo() != null) {
            result.append(getDateFormat().format(getValidTo().getTime()));
        } else {
            result.append(Messages.AdjustmentDate_infinite);
        }
        return result.toString();
    }

    public DateFormat getDateFormat() {
        return IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
    }

}
