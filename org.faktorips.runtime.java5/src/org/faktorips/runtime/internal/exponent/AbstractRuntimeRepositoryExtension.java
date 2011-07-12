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

import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.GenerationId;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;

public abstract class AbstractRuntimeRepositoryExtension {

    private AbstractRuntimeRepositoryExtension nextExtension;

    protected IProductComponent getNotCachedProductComponent(String id) {
        return nextExtension.getNotCachedProductComponent(id);
    }

    protected IProductComponentGeneration getNotCachedProductComponentGeneration(GenerationId generationId) {
        return nextExtension.getNotCachedProductComponentGeneration(generationId);
    }

    protected ClassloaderRuntimeRepository getClassloaderRuntimeRepository() {
        return nextExtension.getClassloaderRuntimeRepository();
    }

    /**
     * @param nextExtension The nextExtension to set.
     */
    void setNextExtension(AbstractRuntimeRepositoryExtension nextVariation) {
        this.nextExtension = nextVariation;
    }

    /**
     * @return Returns the nextExtension.
     */
    AbstractRuntimeRepositoryExtension getNextExtension() {
        return nextExtension;
    }

}
