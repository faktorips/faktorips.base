/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.util.Objects;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
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
     *             could not be parsed.
     */
    public static IProductCmptKind createProductCmptKind(String name, IIpsProject ipsProject) {
        IProductCmptNamingStrategy strategy = ipsProject.getProductCmptNamingStrategy();
        String runtimeIdPrefix = ipsProject.getRuntimeIdPrefix();
        return createProductCmptKind(name, strategy, runtimeIdPrefix);
    }

    /**
     * Create a new ProductCmptKind by parsing the name using the given
     * {@link IProductCmptNamingStrategy} and runtime ID prefix.
     *
     * @param name the name of the {@link IProductCmpt}
     * @param strategy the {@link IProductCmptNamingStrategy} to be used to create the name for the
     *            {@link IProductCmpt}
     * @param runtimeIdPrefix the prefix for runtime IDs
     * @return the {@link IProductCmptKind} derived from the name or <code>null</code> if the name
     *             could not be parsed.
     */
    public static IProductCmptKind createProductCmptKind(String name,
            IProductCmptNamingStrategy strategy,
            String runtimeIdPrefix) {
        try {
            String kindName = strategy.getKindId(name);
            return new ProductCmptKind(kindName, runtimeIdPrefix + kindName);
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
        return Objects.hash(name, runtimeId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        ProductCmptKind other = (ProductCmptKind)obj;
        return Objects.equals(name, other.name)
                && Objects.equals(runtimeId, other.runtimeId);
    }

    @Override
    public String getRuntimeId() {
        return runtimeId;
    }

}
