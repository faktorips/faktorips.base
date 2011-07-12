/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal.exponent;

import org.faktorips.runtime.GenerationId;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;

public class VarantRuntimeRepositoryExtension extends AbstractRuntimeRepositoryExtension {

    @Override
    protected IProductComponent getNotCachedProductComponent(String id) {
        return super.getNotCachedProductComponent(id);
    }

    @Override
    protected IProductComponentGeneration getNotCachedProductComponentGeneration(GenerationId generationId) {
        // TODO Auto-generated method stub
        return super.getNotCachedProductComponentGeneration(generationId);
    }

}
