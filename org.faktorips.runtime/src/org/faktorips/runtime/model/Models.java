/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.runtime.ITable;
import org.faktorips.runtime.model.table.TableModel;

/**
 * Repository of Faktor-IPS model information. This class should be used to obtain model instances
 * from runtime classes or their instances instead of using the constructors. By caching model
 * information, this class operates more efficiently if model information is retrieved repeatedly.
 */
public class Models {

    private static final Map<Class<?>, TableModel> TABLE_MODEL_CACHE = new HashMap<Class<?>, TableModel>();

    private Models() {
        // prevent default constructor
    }

    /**
     * Returns a {@link TableModel} describing the type and columns of the given {@link ITable}
     * class.
     */
    public static TableModel getTableModel(Class<? extends ITable> tableObjectClass) {
        TableModel tm = TABLE_MODEL_CACHE.get(tableObjectClass);
        if (tm == null) {
            synchronized (TABLE_MODEL_CACHE) {
                tm = TABLE_MODEL_CACHE.get(tableObjectClass);
                if (tm == null) {
                    tm = new TableModel(tableObjectClass);
                    TABLE_MODEL_CACHE.put(tableObjectClass, tm);
                }
            }
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
