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

package org.faktorips.runtime.productprovider;

public class ClassLoaderPdpFactory implements IProductDataProviderFactory {

    private final String tocResourcePath;

    private boolean checkForModifications = false;

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public ClassLoaderPdpFactory(String tocResourcePath) {
        this.tocResourcePath = tocResourcePath;
    }

    public void setCheckForModifications(boolean checkForModifications) {
        this.checkForModifications = checkForModifications;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public IProductDataProvider newInstance() {
        return new ClassLoaderProductDataProvider(classLoader, tocResourcePath, checkForModifications);
    }

}
