/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of the published interface.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptKind implements IProductCmptKind {

    private final String name;

    private final String runtimeId;

    public ProductCmptKind(String name, String runtimeId) {
        ArgumentCheck.notNull(name);
        ArgumentCheck.notNull(runtimeId);
        this.name = name;
        this.runtimeId = runtimeId;
    }

    /**
     * Create a new ProductCmptKind by parsing the name using the {@link IProductCmptNamingStrategy}
     * from the given {@link IIpsProject}.
     * 
     * @param name the name of the {@link IProductCmpt}
     * @param ipsProject the {@link IIpsProject} of the {@link IProductCmpt}
     * @return the {@link IProductCmptKind} derived from the name or <code>null</code> if the name
     *         could not be parsed.
     */
    public static IProductCmptKind createProductCmptKind(String name, IIpsProject ipsProject) {
        IProductCmptNamingStrategy stratgey = ipsProject.getProductCmptNamingStrategy();
        try {
            String kindName = stratgey.getKindId(name);
            return new ProductCmptKind(kindName, ipsProject.getRuntimeIdPrefix() + kindName);
        } catch (IllegalArgumentException e) {
            // error in parsing the name results in a "not found" for the client
            return null;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ProductCmptKind [name=" + name + ", runtimeId=" + runtimeId + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((runtimeId == null) ? 0 : runtimeId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProductCmptKind other = (ProductCmptKind)obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (runtimeId == null) {
            if (other.runtimeId != null) {
                return false;
            }
        } else if (!runtimeId.equals(other.runtimeId)) {
            return false;
        }
        return true;
    }

    @Override
    public String getRuntimeId() {
        return runtimeId;
    }

}
