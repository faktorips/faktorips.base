/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.faktorips.runtime.ITable;

/**
 * Repository of Faktor-IPS model information. It's methods should be used to obtain model instances
 * from runtime classes or their instances instead of directly creating those models via their
 * constructors, as this class may cache the model information which might be expensive to obtain
 * again and again.
 */
public class Models {

    private static final Map<Class<?>, TableModel> CACHE = new ConcurrentHashMap<Class<?>, TableModel>();

    private Models() {
        // prevent default constructor
    }

    /**
     * Returns a {@link TableModel} describing the type and columns of the given {@link ITable}
     * class.
     */
    public static TableModel getTableModel(Class<? extends ITable> tableObjectClass) {
        TableModel tm = null;
        if (CACHE.containsKey(tableObjectClass)) {
            tm = CACHE.get(tableObjectClass);
        } else {
            tm = new TableModel(tableObjectClass);
            CACHE.put(tableObjectClass, tm);
        }
        return tm;
    }

    /**
     * Returns a {@link TableModel} describing the type and columns of the given {@link ITable}.
     */
    public static TableModel getTableModel(ITable table) {
        return getTableModel(table.getClass());
    }
}
