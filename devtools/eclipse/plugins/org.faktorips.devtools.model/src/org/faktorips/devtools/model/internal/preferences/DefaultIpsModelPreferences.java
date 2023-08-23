/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.preferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.plugin.IDatatypeFormatter;
import org.faktorips.devtools.model.plugin.NamedDataTypeDisplay;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.runtime.internal.IpsStringUtils;

public class DefaultIpsModelPreferences implements IIpsModelPreferences {

    private static final String NULL_PRESENTATION = "<null>"; //$NON-NLS-1$
    private static final String CHANGES_OVER_TIME_NAMING_CONVENTION = IChangesOverTimeNamingConvention.VAA;

    private boolean autoValidateTables = true;

    @Override
    public IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention() {
        return IIpsModel.get().getChangesOverTimeNamingConvention(CHANGES_OVER_TIME_NAMING_CONVENTION);
    }

    @Override
    public IDatatypeFormatter getDatatypeFormatter() {
        return new IDatatypeFormatter() {
            @Override
            public String getNullPresentation() {
                return DefaultIpsModelPreferences.this.getNullPresentation();
            }
        };
    }

    @Override
    public String getNullPresentation() {
        return NULL_PRESENTATION;
    }

    @Override
    public DateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    @Override
    public boolean isAutoValidateTables() {
        return autoValidateTables;
    }

    public void setAutoValidateTables(boolean autoValidateTables) {
        this.autoValidateTables = autoValidateTables;
    }

    @Override
    public String getIpsTestRunnerMaxHeapSize() {
        return IpsStringUtils.EMPTY;
    }

    @Override
    public boolean isBuilderEnabled() {
        return true;
    }

    @Override
    public Locale getDatatypeFormattingLocale() {
        return Locale.US;
    }

    @Override
    public NamedDataTypeDisplay getNamedDataTypeDisplay() {
        return NamedDataTypeDisplay.NAME_AND_ID;
    }
}
