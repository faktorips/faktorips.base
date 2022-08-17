/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin.extensions;

import org.faktorips.devtools.model.IClassLoaderProvider;
import org.faktorips.devtools.model.IClassLoaderProviderFactory;
import org.faktorips.devtools.model.ipsproject.IClasspathContentsChangeListener;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IClassLoaderProviderFactory}-supplier for the single implementation of the extension point
 * {@value #EXTENSION_POINT_ID_CLASSLOADER_PROVIDER_FACTORY}.
 */
public class ClassLoaderProviderFactoryExtension extends SimpleSingleLazyExtension<IClassLoaderProviderFactory> {
    /**
     * The extension point id of the extension point
     * {@value #EXTENSION_POINT_ID_CLASSLOADER_PROVIDER_FACTORY}.
     */
    public static final String EXTENSION_POINT_ID_CLASSLOADER_PROVIDER_FACTORY = "classLoaderProviderFactory"; //$NON-NLS-1$

    public ClassLoaderProviderFactoryExtension(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_CLASSLOADER_PROVIDER_FACTORY,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IClassLoaderProviderFactory.class,
                SimpleClassLoaderProviderFactory::new);
    }

    /**
     * An {@link IClassLoaderProvider} that does not register changes to the classpath's contents
     */
    private interface ISimpleClassLoaderProvider extends IClassLoaderProvider {

        @Override
        default void addClasspathChangeListener(IClasspathContentsChangeListener listener) {
            // ignored
        }

        @Override
        default void removeClasspathChangeListener(IClasspathContentsChangeListener listener) {
            // ignored
        }

    }

    /**
     * Provides a classloader that was used to load a project - this means that all classes
     * referenced from the project must also be on the classpath used to start the
     * {@link IIpsProject}.
     */
    private static final class SimpleClassLoaderProviderFactory implements IClassLoaderProviderFactory {

        @Override
        public IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject) {
            return (ISimpleClassLoaderProvider)() -> ipsProject.getClass().getClassLoader();
        }

        @Override
        public IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject, ClassLoader parent) {
            return (ISimpleClassLoaderProvider)() -> parent;
        }

    }

}
