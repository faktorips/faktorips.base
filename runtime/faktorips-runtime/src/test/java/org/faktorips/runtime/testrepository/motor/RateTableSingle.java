/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.testrepository.motor;

import java.util.List;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.Table;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.model.table.TableStructureKind;

/**
 *
 * @author Peter Erzberger
 */
@IpsTableStructure(name = "motor.RateTableSingle", columns = {}, type = TableStructureKind.SINGLE_CONTENT)
public class RateTableSingle extends Table<Object> {

    @Override
    protected void initKeyMaps() {
        // do nothing
    }

    @Override
    protected void addRow(List<String> columns, IRuntimeRepository repository) {
        // do nothing
    }

}
