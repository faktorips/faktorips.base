/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import org.faktorips.values.NullObject;
import org.faktorips.values.NullObjectSupport;

public class PaymentMode implements NullObjectSupport {

    static final PaymentMode ANNUAL = new PaymentMode("annual", "Annual Payment"); //$NON-NLS-1$ //$NON-NLS-2$
    static final PaymentMode MONTHLY = new PaymentMode("monthly", "Monthly Payment"); //$NON-NLS-1$ //$NON-NLS-2$
    static final PaymentMode NULL = new PaymentModeNull();

    private final String id;
    private final String name;

    PaymentMode(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static final PaymentMode[] getAllPaymentModes() {
        return new PaymentMode[] { MONTHLY, ANNUAL };
    }

    public static final boolean isParsable(String id) {
        try {
            PaymentMode.getPaymentMode(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static final PaymentMode getPaymentMode(String id) {
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

    @Override
    public boolean isNull() {
        return this == PaymentMode.NULL;
    }

    @Override
    public boolean isNotNull() {
        return !isNull();
    }

    static class PaymentModeNull extends PaymentMode implements NullObject {

        PaymentModeNull() {
            super("null", "No Payment"); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

}
