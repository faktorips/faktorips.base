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

package org.faktorips.runtime;

import org.faktorips.runtime.internal.AbstractRuntimeRepositoryManager;
import org.faktorips.runtime.productdataprovider.DetachedContentRuntimeRepository;

/**
 * This is a {@link IRuntimeRepositoryManager} for the {@link ClassloaderRuntimeRepository}. You do
 * not really need a {@link IRuntimeRepositoryManager} for {@link ClassloaderRuntimeRepository} and
 * in fact this manager always return the same {@link IRuntimeRepository}.
 * <p>
 * Use this {@link IRuntimeRepositoryManager} if you think about changing the runtime repository to
 * another e.g. {@link DetachedContentRuntimeRepository} that need to use a
 * {@link IRuntimeRepositoryManager} later.
 * 
 * @author dirmeier
 */
public class ClassloaderRuntimeRepositoryManager extends AbstractRuntimeRepositoryManager {

    private final ClassLoader classLoader;
    private final String basePackage;
    private final String pathToToc;

    /**
     * The constructor needs all the information to create a {@link ClassloaderRuntimeRepository}
     */
    public ClassloaderRuntimeRepositoryManager(ClassLoader classLoader, String basePackage, String pathToToc) {
        this.classLoader = classLoader;
        this.basePackage = basePackage;
        this.pathToToc = pathToToc;
    }

    @Override
    protected IRuntimeRepository createNewRuntimeRepository() {
        return new ClassloaderRuntimeRepository(classLoader, basePackage, pathToToc);
    }

    @Override
    protected boolean isRepositoryUpToDate(IRuntimeRepository actualRuntimeRepository) {
        return true;
    }

}
