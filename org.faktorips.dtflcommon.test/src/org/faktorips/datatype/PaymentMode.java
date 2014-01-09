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

package org.faktorips.datatype;

import org.faktorips.values.NullObject;
import org.faktorips.values.NullObjectSupport;

public class PaymentMode implements NullObjectSupport {

    final static PaymentMode ANNUAL = new PaymentMode("annual", "Annual Payment"); //$NON-NLS-1$ //$NON-NLS-2$
    final static PaymentMode MONTHLY = new PaymentMode("monthly", "Monthly Payment"); //$NON-NLS-1$ //$NON-NLS-2$
    final static PaymentMode NULL = new PaymentModeNull();

    private final String id;
    private final String name;

    public final static PaymentMode[] getAllPaymentModes() {
        return new PaymentMode[] { MONTHLY, ANNUAL };
    }

    public final static boolean isParsable(String id) {
        try {
            PaymentMode.getPaymentMode(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public final static PaymentMode getPaymentMode(String id) {
        if (id == null) {
            return null;
        }
        if (ANNUAL.id.equals(id)) {
            return ANNUAL;
        }
        if (MONTHLY.id.equals(id)) {
            return MONTHLY;
        }
        throw new IllegalArgumentException("The id " + id + " does not identify a PaymentMode"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    PaymentMode(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public boolean isSupportingNames() {
        return true;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return id;
    }

    public boolean isNull() {
        return this == PaymentMode.NULL;
    }

    public boolean isNotNull() {
        return !isNotNull();
    }

    static class PaymentModeNull extends PaymentMode implements NullObject {

        PaymentModeNull() {
            super("null", "No Payment"); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

}
