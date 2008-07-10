/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community)
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation
 *
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.List;

import org.faktorips.values.Decimal;

public class TestTable extends Table {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initKeyMaps() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addRow(List columns) {
        String company = (String)columns.get(0);
        Integer gender = columns.get(1) == null ? null : new Integer((String)columns.get(1));
        Decimal rate = columns.get(2) == null ? Decimal.NULL : Decimal.valueOf((String)columns.get(2));
        rows.add(new TestTableRow(company, gender, rate));
    }
}