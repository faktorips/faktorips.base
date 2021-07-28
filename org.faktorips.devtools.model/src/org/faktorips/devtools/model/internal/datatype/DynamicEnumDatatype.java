/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.datatype;

import org.faktorips.datatype.DefaultGenericEnumDatatype;
import org.faktorips.devtools.model.datatype.IDynamicEnumDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.w3c.dom.Element;

/**
 * A dynamic enum datatype. See the super class for more details.
 * 
 * @author Jan Ortmann
 */
public class DynamicEnumDatatype extends DynamicValueDatatype implements IDynamicEnumDatatype {

    private String getAllValuesMethodName = ""; //$NON-NLS-1$

    private String getNameMethodName = ""; //$NON-NLS-1$

    private boolean isSupportingNames = false;

    public DynamicEnumDatatype(IIpsProject ipsProject) {
        super(ipsProject);
    }

    @Override
    public String[] getAllValueIds(boolean includeNull) {
        if (getAdaptedClass() == null) {
            throw new RuntimeException("Datatype " + getQualifiedName() //$NON-NLS-1$
                    + ", Class " + getAdaptedClassName() + " not found."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        DefaultGenericEnumDatatype datatype = new DefaultGenericEnumDatatype(getAdaptedClass());
        datatype.setToStringMethodName(getToStringMethodName());
        datatype.setGetAllValuesMethodName(getAllValuesMethodName);

        return datatype.getAllValueIds(includeNull);
    }

    @Override
    public void setAllValuesMethodName(String getAllValuesMethodName) {
        this.getAllValuesMethodName = getAllValuesMethodName;
    }

    @Override
    public String getAllValuesMethodName() {
        return getAllValuesMethodName;
    }

    @Override
    public void setIsSupportingNames(boolean supporting) {
        isSupportingNames = supporting;
    }

    @Override
    public void setGetNameMethodName(String getNameMethodName) {
        this.getNameMethodName = getNameMethodName;
    }

    @Override
    public String getGetNameMethodName() {
        return getNameMethodName;
    }

    @Override
    public boolean isSupportingNames() {
        return isSupportingNames;
    }

    @Override
    public String getValueName(String id) {
        if (!isSupportingNames) {
            IpsLog.log(new IpsStatus(
                    "The getName(String) method is not supported by this enumeration class: " + getAdaptedClass())); //$NON-NLS-1$ )
            return id;
        }
        DefaultGenericEnumDatatype datatype = new DefaultGenericEnumDatatype(getAdaptedClass());
        datatype.setIsSupportingNames(isSupportingNames);
        datatype.setGetNameMethodName(getNameMethodName);
        datatype.setValueOfMethodName(getValueOfMethodName());
        datatype.setToStringMethodName(getToStringMethodName());
        try {
            return datatype.getValueName(id);
            // CSOFF: IllegalCatchCheck
        } catch (Exception e) {
            IpsLog.log(new IpsStatus("Error getting name for enum value id " + id, e)); //$NON-NLS-1$
            return id;
        }
        // CSON: IllegalCatchCheck
    }

    @Override
    public void writeToXml(Element element) {
        super.writeToXml(element);
        element.setAttribute("isEnumType", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        if (getAllValuesMethodName() != null) {
            element.setAttribute("getAllValuesMethod", getAllValuesMethodName()); //$NON-NLS-1$
        }
        if (getGetNameMethodName() != null) {
            element.setAttribute("getNameMethod", getGetNameMethodName()); //$NON-NLS-1$

        }
        element.setAttribute("isSupportingNames", Boolean.toString(isSupportingNames())); //$NON-NLS-1$

    }

}
