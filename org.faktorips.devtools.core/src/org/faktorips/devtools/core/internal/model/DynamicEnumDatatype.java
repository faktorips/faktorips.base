/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.faktorips.datatype.DefaultGenericEnumDatatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.w3c.dom.Element;

/**
 * A dynamic enum datatype. See the super class for more detais.
 * 
 * @author Jan Ortmann
 */
public class DynamicEnumDatatype extends DynamicValueDatatype implements EnumDatatype {

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
        datatype.setGetAllValuesMethodName(getAllValuesMethodName);

        return datatype.getAllValueIds(includeNull);
    }

    /**
     * Sets the name of the method that provides all values of the datatype.
     */
    public void setAllValuesMethodName(String getAllValuesMethodName) {
        this.getAllValuesMethodName = getAllValuesMethodName;
    }

    /**
     * Returns the name of the method that provides all values of the datatype.
     */
    public String getAllValuesMethodName() {
        return getAllValuesMethodName;
    }

    /**
     * Sets the name of the method that returns the isSupportingNames flag of the enumeration class
     * wrapped by this dynamic enum datatype.
     */
    public void setIsSupportingNames(boolean supporting) {
        isSupportingNames = supporting;
    }

    /**
     * Sets the name of the method that returns the name of a value of the enumeration class wrapped
     * by this dynamic enum datatype.
     */
    public void setGetNameMethodName(String getNameMethodName) {
        this.getNameMethodName = getNameMethodName;
    }

    /**
     * Returns the name of the method returning the value's name.
     */
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
            IpsPlugin.log(new IpsStatus(
                    "The getName(String) method is not supported by this enumeration class: " + getAdaptedClass())); //$NON-NLS-1$)
            return id;
        }
        DefaultGenericEnumDatatype datatype = new DefaultGenericEnumDatatype(getAdaptedClass());
        datatype.setIsSupportingNames(isSupportingNames);
        datatype.setGetNameMethodName(getNameMethodName);
        datatype.setValueOfMethodName(getValueOfMethodName());
        datatype.setToStringMethodName(getToStringMethodName());
        try {
            return datatype.getValueName(id);
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error getting name for enum value id " + id, e)); //$NON-NLS-1$
            return id;
        }
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
