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

/**
 * Logging modes for the Faktor-IPS build output.
 */
public enum LoggingMode {

    /**
     * Redirect everything to STDOUT
     */
    original,
    /**
     * Redirect the STDOUT of every thread to it's own file, and output one file after the other
     */
    perThread,
    /**
     * Additionally to the {@code perThread} mode this mode will filter the thread's output for well
     * known exceptions e.g. FileNotFoundException of the junit eclipse plugin.
     */
    perThreadFiltered;

}
