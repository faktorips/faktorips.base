/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import org.faktorips.annotation.UtilityClass;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Provides null-safe access to the {@linkplain AProject#getDefaultLineSeparator() default line
 * separator defined in a project}, falling back to the {@linkplain System#lineSeparator() system's
 * line separator}.
 */
@UtilityClass
public final class DefaultLineSeparator {

    private DefaultLineSeparator() {
        // util
    }

    public static String ofSystem() {
        return System.lineSeparator();
    }

    public static String of(AProject project) {
        return project == null ? ofSystem() : project.getDefaultLineSeparator();
    }

    public static String of(AResource resource) {
        return resource == null ? ofSystem() : of(resource.getProject());
    }

    public static String of(IIpsProject ipsProject) {
        return ipsProject == null ? ofSystem() : of(ipsProject.getProject());
    }

    public static String of(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile == null ? ofSystem() : of(ipsSrcFile.getIpsProject());
    }

    public static String of(IIpsObject ipsObject) {
        return ipsObject == null ? ofSystem() : of(ipsObject.getIpsProject());
    }

}
