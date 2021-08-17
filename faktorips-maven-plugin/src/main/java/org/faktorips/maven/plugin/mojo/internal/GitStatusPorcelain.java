/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.maven.plugin.mojo.internal;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GitStatusPorcelain {

    public enum Verbosity {
        QUIET("quiet"),
        VERBOSE("verbose"),
        DIFF("diff");

        private final String name;

        private Verbosity(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        boolean matches(String value) {
            return name.equals(value);
        }
    }

    private boolean failBuild = false;
    private Verbosity verbosity = Verbosity.VERBOSE;

    public String getFailBuild() {
        return String.valueOf(failBuild);
    }

    public void setFailBuild(boolean failBuild) {
        this.failBuild = failBuild;
    }

    public Verbosity getVerbosity() {
        return verbosity;
    }

    public void setVerbosity(String value) {
        for (Verbosity enumValue : Verbosity.values()) {
            if (enumValue.matches(value)) {
                this.verbosity = enumValue;
                return;
            }
        }
        throw new IllegalArgumentException("Unrecognized value for attribute 'verbosity': ''" + value
                + "''. Valid values are: "
                + Arrays.stream(Verbosity.values()).map(Verbosity::toString).collect(Collectors.joining(",")));
    }
}
