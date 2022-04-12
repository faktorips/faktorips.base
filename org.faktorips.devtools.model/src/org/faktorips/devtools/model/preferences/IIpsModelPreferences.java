/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.preferences;

import java.text.DateFormat;
import java.util.Locale;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.builder.IpsBuilder;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.plugin.IDatatypeFormatter;
import org.faktorips.runtime.IpsTestCase;

/**
 * Preferences for the Faktor-IPS model. Mostly for String representation in log messages and UI.
 */
public interface IIpsModelPreferences {

    /**
     * Returns the naming convention used for product changes over time.
     */
    IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention();

    /**
     * Returns the formatter to convert values of different {@link Datatype Datatypes} to Strings
     */
    IDatatypeFormatter getDatatypeFormatter();

    /**
     * Returns the configured {@link Locale}
     */
    Locale getDatatypeFormattingLocale();

    /**
     * Returns the {@code null}-representation String.
     */
    String getNullPresentation();

    /**
     * Returns the formatter to convert date values to Strings
     */
    DateFormat getDateFormat();

    /**
     * Return whether tables should be automatically validated on every change or only after
     * explicit calls.
     */
    boolean isAutoValidateTables();

    /**
     * Returns the maximum heap size to use for the JVM created for running {@link IpsTestCase
     * IpsTestCases}.
     */
    String getIpsTestRunnerMaxHeapSize();

    /**
     * Returns whether the {@link IpsBuilder} should generate code.
     */
    boolean isBuilderEnabled();

}