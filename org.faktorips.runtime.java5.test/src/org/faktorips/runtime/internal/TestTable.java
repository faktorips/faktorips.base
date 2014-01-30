/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.List;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.values.Decimal;

public class TestTable extends Table<TestTableRow> {

    @Override
    protected void initKeyMaps() {
        // do nothing
    }

    @Override
    protected void addRow(List<String> columns, IRuntimeRepository repository) {
        String company = columns.get(0);
        Integer gender = columns.get(1) == null ? null : new Integer(columns.get(1));
        Decimal rate = columns.get(2) == null ? Decimal.NULL : Decimal.valueOf(columns.get(2));
        rows.add(new TestTableRow(company, gender, rate));
    }

}
