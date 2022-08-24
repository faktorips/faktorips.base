/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.bundle;

import org.faktorips.devtools.abstraction.util.IpsNLS;

public class Messages extends IpsNLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.ipsproject.bundle.messages"; //$NON-NLS-1$
    public static String IpsBundleEntry_msg_invalid;
    static {
        // initialize resource bundle
        IpsNLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
