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

package org.faktorips.datatype.classtypes;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueClassDatatype;

/**
 * Data type for {@link BigDecimal}.
 * 
 * @author Jan Ortmann
 */
public class BigDecimalDatatype extends ValueClassDatatype {

    /**
     * @param clazz
     */
    public BigDecimalDatatype() {
        super(BigDecimal.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return new BigDecimal(value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return true;
    }
}
