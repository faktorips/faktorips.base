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

package org.faktorips.runtime;

import org.faktorips.runtime.internal.AbstractRuntimeRepositoryManager;

public class ClassloaderRuntimeRepositoryManager extends AbstractRuntimeRepositoryManager {

    private final ClassLoader classLoader;
    private final String basePackage;
    private final String pathToToc;

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
