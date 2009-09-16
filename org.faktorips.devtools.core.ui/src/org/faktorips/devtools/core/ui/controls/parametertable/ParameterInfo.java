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

package org.faktorips.devtools.core.ui.controls.parametertable;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.type.IParameter;

public class ParameterInfo {

    public static final int INDEX_FOR_ADDED = -1;
    private final String fOldName;
    private final String fOldTypeName;
    private final int fOldIndex;

    private String fNewTypeName;
    private String fDefaultValue;
    private String fNewName;
    private boolean fIsDeleted;

    public final static List<ParameterInfo> createInfosAsList(IParameter[] params) {
        List<ParameterInfo> result = new ArrayList<ParameterInfo>(params.length);
        for (int i = 0; i < params.length; i++) {
            result.add(new ParameterInfo(params[i].getDatatype(), params[i].getName(), i));
        }
        return result;
    }

    public ParameterInfo(String type, String name, int index) {
        fOldTypeName = type;
        fNewTypeName = type;
        fOldName = name;
        fNewName = name;
        fOldIndex = index;
        fDefaultValue = "";
        fIsDeleted = false;
    }

    public static ParameterInfo createInfoForAddedParameter() {
        ParameterInfo info = new ParameterInfo("String", "newParam", INDEX_FOR_ADDED); //$NON-NLS-1$ //$NON-NLS-2$
        info.setDefaultValue("null");
        return info;
    }

    public boolean isDeleted() {
        return fIsDeleted;
    }

    public void markAsDeleted() {
        fIsDeleted = true;
    }

    public boolean isAdded() {
        return fOldIndex == INDEX_FOR_ADDED;
    }

    public String getDefaultValue() {
        return fDefaultValue;
    }

    public void setDefaultValue(String value) {
        fDefaultValue = value;
    }

    public String getOldTypeName() {
        return fOldTypeName;
    }

    public String getNewTypeName() {
        return fNewTypeName;
    }

    public void setNewTypeName(String type) {
        fNewTypeName = type;
    }

    public String getOldName() {
        return fOldName;
    }

    public int getOldIndex() {
        return fOldIndex;
    }

    public void setNewName(String newName) {
        fNewName = newName;
    }

    public String getNewName() {
        return fNewName;
    }

    public boolean isRenamed() {
        return !fOldName.equals(fNewName);
    }

    public boolean isTypeNameChanged() {
        return !fOldTypeName.equals(fNewTypeName);
    }

    @Override
    public String toString() {
        return fOldTypeName + " " + fOldName + " @" + fOldIndex + " -> " //$NON-NLS-1$//$NON-NLS-2$
                + fNewTypeName + " " + fNewName + ": " + fDefaultValue + (fIsDeleted ? " (deleted)" : " (stays)");
    }
}
