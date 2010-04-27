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

package org.faktorips.datatype;

import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * A generic value datatype that makes an <strong>existing</code> Java class (is is already loaded
 * by the classloader) available as datatype.
 * 
 * @author Jan Ortmann
 */
public class DefaultGenericValueDatatype extends GenericValueDatatype {

    private Class<?> adaptedClass;

    public DefaultGenericValueDatatype() {
        super();
    }

    public DefaultGenericValueDatatype(Class<?> adaptedClass) {
        ArgumentCheck.notNull(adaptedClass);
        this.adaptedClass = adaptedClass;
        setQualifiedName(StringUtil.unqualifiedName(adaptedClass.getName()));
    }

    /**
     * Overridden.
     */
    @Override
    public Class<?> getAdaptedClass() {
        return adaptedClass;
    }

    /**
     * Overridden.
     */
    @Override
    public String getAdaptedClassName() {
        return adaptedClass.getName();
    }

}
