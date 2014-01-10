/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
