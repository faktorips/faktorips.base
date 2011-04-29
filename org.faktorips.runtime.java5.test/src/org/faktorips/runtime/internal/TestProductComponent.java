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

package org.faktorips.runtime.internal;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;

/**
 * ProductComponent for testing purposes.
 * 
 * @author Jan Ortmann
 */
public class TestProductComponent extends ProductComponent {

    public TestProductComponent(IRuntimeRepository repository, String id, String productKindId, String versionId) {
        super(repository, id, productKindId, versionId);
    }

    protected ProductComponentGeneration createGeneration() {
        return new TestProductCmptGeneration(this);
    }

    public IConfigurableModelObject createPolicyComponent() {
        throw new UnsupportedOperationException();
    }

}
