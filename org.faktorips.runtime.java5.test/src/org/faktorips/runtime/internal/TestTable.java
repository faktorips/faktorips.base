/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
