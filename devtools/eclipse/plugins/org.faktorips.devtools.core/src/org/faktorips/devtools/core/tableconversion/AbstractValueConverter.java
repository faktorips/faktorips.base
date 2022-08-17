/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.tableconversion;

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
