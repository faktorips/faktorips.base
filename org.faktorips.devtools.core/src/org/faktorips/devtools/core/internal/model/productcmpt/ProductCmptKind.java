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

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.faktorips.devtools.core.model.productcmpt.IProductCmptKind;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of the published interface.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptKind implements IProductCmptKind {

    private String name;

    private String runtimeId;

    public ProductCmptKind(String name, String runtimeId) {
        ArgumentCheck.notNull(name);
        ArgumentCheck.notNull(runtimeId);
        this.name = name;
        this.runtimeId = runtimeId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRuntimeId() {
        return runtimeId;
    }

}
