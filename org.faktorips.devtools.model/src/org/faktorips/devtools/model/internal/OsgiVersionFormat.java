/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IVersionFormat;
import org.osgi.framework.Version;

/**
 * The {@link OsgiVersionFormat} is the used when no other {@link IVersionProvider} is configured.
 * It simply reads the version that is specified in the {@link IIpsProjectProperties}. The version
 * format could be configured by a extended productReleaseExtension. If no productReleaseExtension
 * is configured, the default version format equates to the grammar of OSGI version strings.
 */
public class OsgiVersionFormat implements IVersionFormat {

    @Override
    public boolean isCorrectVersionFormat(String version) {
        try {
            Version.parseVersion(version);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    @Override
    public String getVersionFormat() {
        return "2.1.4.qualifier"; //$NON-NLS-1$
    }
}
