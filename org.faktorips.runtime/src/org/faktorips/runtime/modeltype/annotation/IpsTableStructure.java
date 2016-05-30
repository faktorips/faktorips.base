/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.faktorips.runtime.ITable;
import org.faktorips.runtime.modeltype.TableModel;

/**
 * Preserves design time information about a table structure for runtime reference via
 * {@link TableModel}.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IpsTableStructure {

    /**
     * The qualified IPS object name.
     */
    String name();

    /**
     * Whether this table structure allows multiple contents.
     */
    TableStructureType type();

    /**
     * The names of the columns, in order. Additional information is available in
     * {@link IpsTableColumn} annotations on the {@link ITable table's} row class.
     */
    String[] columns();
}
