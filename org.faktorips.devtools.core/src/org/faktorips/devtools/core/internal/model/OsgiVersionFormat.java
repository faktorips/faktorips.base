/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.regex.Pattern;

import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.model.IVersionProvider;
import org.faktorips.devtools.core.model.ipsproject.IVersionFormat;

/**
 * The {@link OsgiVersionFormat} is the used when no other {@link IVersionProvider} is configured.
 * It simply reads the version that is specified in the {@link IpsProjectProperties}. The version
 * format could be configured by a extended productReleaseExtension. If no productReleaseExtension
 * is configured, the default version format is "[0-9]+\\.[0-9]+\\.[0-9]+\\.[a-z]+"
 */
public class OsgiVersionFormat implements IVersionFormat {

    private static final Pattern VERSION_PATTERN = Pattern.compile("[0-9]+(\\.[0-9]+\\.[0-9]+\\.[A-Za-z]+)?"); //$NON-NLS-1$

    @Override
    public boolean isCorrectVersionFormat(String version) {
        return VERSION_PATTERN.matcher(version).matches();
    }

    @Override
    public String getVersionFormat() {
        return "for example: 2.1.4.qualifier"; //$NON-NLS-1$
    }
}
