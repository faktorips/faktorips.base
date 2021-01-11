/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.EnumTypeDisplay;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;

public class EnumDatatypeInputFormat extends AbstractInputFormat<String> {

    private final EnumDatatype enumDatatype;

    private final IpsPreferences ipsPreferences;

    public EnumDatatypeInputFormat(EnumDatatype enumDatatype, IpsPreferences ipsPreferences) {
        super(StringUtils.EMPTY, ipsPreferences.getDatatypeFormattingLocale());
        this.enumDatatype = enumDatatype;
        this.ipsPreferences = ipsPreferences;
    }

    public static EnumDatatypeInputFormat newInstance(EnumDatatype enumDatatype) {
        return new EnumDatatypeInputFormat(enumDatatype, IpsPlugin.getDefault().getIpsPreferences());
    }

    @Override
    protected String parseInternal(String stringToBeparsed) {
        String parsedAsId = parseValueId(stringToBeparsed);
        if (parsedAsId != null) {
            return parsedAsId;
        }
        String parsedAsName = parseValueName(stringToBeparsed);
        if (parsedAsName != null) {
            return parsedAsName;
        }
        String parsedAsNameAndId = parseValueNameAndID(stringToBeparsed);
        EnumTypeDisplay enumTypeDisplay = ipsPreferences.getEnumTypeDisplay();
        if (EnumTypeDisplay.NAME_AND_ID.equals(enumTypeDisplay)) {
            if (parsedAsNameAndId != null) {
                return parsedAsNameAndId;
            }
        }
        return stringToBeparsed;
    }

    protected String parseValueId(String stringToBeparsed) {
        if (enumDatatype.isParsable(stringToBeparsed)) {
            return stringToBeparsed;
        } else {
            return null;
        }
    }

    protected String parseValueName(String stringToBeparsed) {
        String[] allValueIds = enumDatatype.getAllValueIds(false);
        for (String valueId : allValueIds) {
            String valueName = enumDatatype.getValueName(valueId);
            if (stringToBeparsed.equals(valueName)) {
                return valueId;
            }
        }
        return null;
    }

    protected String parseValueNameAndID(String stringToBeparsed) {
        /*
         * The pattern provides the name of the enum value (group(0)) and the content of the last
         * bracket (group(1)). The last bracket always contains the enum value's id, which in turn
         * cannot contain nested brackets/parentheses. The last bracket is found by matching ")$",
         * the closing parenthesis at the end of the string.
         */
        Pattern pattern = Pattern.compile(".*\\((.*?)\\)$"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(stringToBeparsed);
        if (matcher.find()) {
            String id = matcher.group(1);
            return parseValueId(id);
        } else {
            return null;
        }
    }

    @Override
    protected String formatInternal(String value) {
        return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(enumDatatype, value);
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // no verify yet
    }

    @Override
    protected void initFormat(Locale locale) {
        // do nothing
    }

}
