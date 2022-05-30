/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.faktorips.runtime.ITable;
import org.faktorips.runtime.model.table.TableStructure;
import org.faktorips.runtime.model.table.TableStructureKind;

/**
 * Preserves design time information about a table structure for runtime reference via
 * {@link TableStructure}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IpsTableStructure {

    /**
     * The qualified IPS object name.
     */
    String name();

    /**
     * Whether this table structure allows multiple contents.
     */
    TableStructureKind type();

    /**
     * The names of the columns, in order. Additional information is available in
     * {@link IpsTableColumn} annotations on the {@link ITable table's} row class.
     */
    String[] columns();
}
