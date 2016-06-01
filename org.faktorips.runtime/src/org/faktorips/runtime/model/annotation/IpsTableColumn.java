/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.faktorips.runtime.model.table.TableColumnModel;

/**
 * Preserves design time information about a table structure's column for runtime reference via
 * {@link TableColumnModel}.
 */
@Retention(value = RetentionPolicy.RUNTIME)
public @interface IpsTableColumn {

    /**
     * The column's name.
     */
    String name();

}
