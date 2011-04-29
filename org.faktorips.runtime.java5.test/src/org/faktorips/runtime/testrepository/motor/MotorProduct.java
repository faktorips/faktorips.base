/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.testrepository.motor;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.testrepository.PnCProduct;

/**
 * @author Jan Ortmann
 */
public class MotorProduct extends PnCProduct {

    public MotorProduct(IRuntimeRepository registry, String id, String productKindId, String versionId) {
        super(registry, id, productKindId, versionId);
    }

    protected ProductComponentGeneration createGeneration() {
        return new MotorProductGen(this);
    }

    public IConfigurableModelObject createPolicyComponent() {
        return new MotorPolicy(this);
    }

}
