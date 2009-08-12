/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.DatatypeFormatter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;

/**
 * An implementation of <code>AbstractEnumDatatypeBasedField</code> that displays the values of an
 * {@link IEnumType} respectively the {@link IEnumContent}. The {@link DatatypeFormatter} from which
 * the display texts are requested by this field uses the enum display settings of the
 * {@link IpsPreferences}.
 * 
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
        return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(getEnumDatatype(), id);
    }
    
}
