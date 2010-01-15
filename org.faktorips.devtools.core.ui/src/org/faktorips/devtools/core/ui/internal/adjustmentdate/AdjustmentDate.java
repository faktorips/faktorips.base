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

package org.faktorips.devtools.core.ui.internal.adjustmentdate;

import java.text.DateFormat;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.Assert;
import org.faktorips.devtools.core.IpsPlugin;

public class AdjustmentDate {

    private final GregorianCalendar validFrom;

    private GregorianCalendar validTo;

    public AdjustmentDate(GregorianCalendar validFrom, GregorianCalendar validTo) {
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
        if (obj instanceof AdjustmentDate) {
            AdjustmentDate other = (AdjustmentDate)obj;
            return validFrom.equals(other.validFrom)
                    && (getValidTo() != null ? getValidTo().equals(other.getValidTo()) : getValidTo() == other
                            .getValidTo());
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
        return "AdjustmentDate: " + getText(); //$NON-NLS-1$
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