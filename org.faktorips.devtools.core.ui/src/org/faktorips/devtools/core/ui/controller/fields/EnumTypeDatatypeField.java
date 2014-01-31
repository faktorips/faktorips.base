/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.DatatypeFormatter;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * An implementation of <code>AbstractEnumDatatypeBasedField</code> that displays the values of an
 * {@link IEnumType} respectively the {@link IEnumContent}. The {@link DatatypeFormatter} from which
 * the display texts are requested by this field uses the enum display settings of the
 * {@link IpsPreferences}.
 * 
 * @author Peter Kuntz
 */
public class EnumTypeDatatypeField extends AbstractEnumDatatypeBasedField {

    private boolean enableEnumContentDisplay = true;

    public EnumTypeDatatypeField(Combo combo, EnumTypeDatatypeAdapter datatypeAdapter) {
        super(combo, datatypeAdapter);
        reInitInternal();
    }

    public void setEnableEnumContentDisplay(boolean enable) {
        enableEnumContentDisplay = enable;
        reInitInternal();
    }

    @Override
    protected List<String> getDatatypeValueIds() {
        if (getEnumDatatype().getEnumContent() != null && !enableEnumContentDisplay) {
            ArrayList<String> result = new ArrayList<String>();
            result.add(null);
            return result;
        }
        return new ArrayList<String>(Arrays.asList(getEnumDatatype().getAllValueIds(true)));
    }

    private EnumTypeDatatypeAdapter getEnumDatatype() {
        return (EnumTypeDatatypeAdapter)getDatatype();
    }

    @Override
    public String getDisplayTextForValue(String id) {
        return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(getEnumDatatype(), id);
    }

}
