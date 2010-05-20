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

package org.faktorips.devtools.tableconversion;

/**
 * Base converter class used in the Faktor-IPS import / export framework.
 * 
 * @author Roman Grutza
 */
public abstract class AbstractValueConverter implements IValueConverter {

    protected ITableFormat tableFormat;

    @Override
    public ITableFormat getTableFormat() {
        return tableFormat;
    }

    @Override
    public void setTableFormat(ITableFormat newFormat) {
        if (newFormat == null) {
            throw new NullPointerException();
        }
        if (tableFormat != null && !newFormat.equals(tableFormat)) {
            throw new RuntimeException("Can't reassign converter to a different table format!"); //$NON-NLS-1$
        }
        tableFormat = newFormat;
    }

}
