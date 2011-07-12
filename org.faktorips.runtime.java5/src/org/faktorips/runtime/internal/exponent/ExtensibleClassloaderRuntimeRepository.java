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

public class ExtensibleClassloaderRuntimeRepository extends ClassloaderRuntimeRepository {

    private AbstractRuntimeRepositoryExtension firstExtension;

    public ExtensibleClassloaderRuntimeRepository(ClassLoader cl, String basePackage) {
        super(cl, basePackage);
        firstExtension = new LastVariation();
    }

    public void addExtension(AbstractRuntimeRepositoryExtension newExtension) {
        newExtension.setNextExtension(firstExtension);
        firstExtension = newExtension;
    }

    @Override
    protected IProductComponent getNotCachedProductComponent(String id) {
        return firstExtension.getNotCachedProductComponent(id);
    }

    @Override
    protected IProductComponentGeneration getNotCachedProductComponentGeneration(GenerationId generationId) {
        return firstExtension.getNotCachedProductComponentGeneration(generationId);
    }

    private class LastVariation extends AbstractRuntimeRepositoryExtension {

        @Override
        protected IProductComponent getNotCachedProductComponent(String id) {
            return ExtensibleClassloaderRuntimeRepository.super.getNotCachedProductComponent(id);
        }

        @Override
        protected IProductComponentGeneration getNotCachedProductComponentGeneration(GenerationId generationId) {
            return ExtensibleClassloaderRuntimeRepository.super.getNotCachedProductComponentGeneration(generationId);
        }

        @Override
        protected ClassloaderRuntimeRepository getClassloaderRuntimeRepository() {
            return ExtensibleClassloaderRuntimeRepository.super;
        }

    }
}
