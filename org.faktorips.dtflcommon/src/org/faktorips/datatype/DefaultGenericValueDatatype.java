/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * A generic value datatype that makes an <strong>existing</strong> Java class (already loaded by
 * the classloader) available as datatype.
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
