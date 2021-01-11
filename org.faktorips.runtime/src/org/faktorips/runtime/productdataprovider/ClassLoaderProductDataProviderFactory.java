/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.productdataprovider;

public class ClassLoaderProductDataProviderFactory implements IProductDataProviderFactory {

    private final String tocResourcePath;

    private boolean checkForModifications = false;

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public ClassLoaderProductDataProviderFactory(String tocResourcePath) {
        this.tocResourcePath = tocResourcePath;
    }

    public void setCheckForModifications(boolean checkForModifications) {
        this.checkForModifications = checkForModifications;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public IProductDataProvider newInstance() {
        ClassLoaderDataSource dataSource = new ClassLoaderDataSource(classLoader);
        return new ClassLoaderProductDataProvider(dataSource, tocResourcePath, checkForModifications);
    }

}
