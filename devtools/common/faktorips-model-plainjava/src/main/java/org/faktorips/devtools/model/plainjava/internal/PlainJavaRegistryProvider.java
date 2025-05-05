/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
/*******************************************************************************
 * Based on Code from Eclipse Tycho, Copyright (c) 2022 Christoph Läubrich and others. This program
 * and the accompanying materials are made available under the terms of the Eclipse Public License
 * 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.faktorips.devtools.model.plainjava.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.internal.registry.RegistryProviderFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.spi.IRegistryProvider;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.core.runtime.spi.RegistryStrategy;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.osgi.framework.Constants;

public class PlainJavaRegistryProvider implements IRegistryProvider {

    private static final Collection<String> EXTENSION_DESCRIPTORS = List.of("plugin.xml", "fragment.xml");
    private IExtensionRegistry registry;

    @Override
    public IExtensionRegistry getRegistry() {
        if (registry == null) {
            registry = new ExtensionRegistry(new ClasspathRegistryStrategy(), null, null);
        }
        return registry;

    }

    public void initialize() {
        try {
            if (RegistryProviderFactory.getDefault() == null) {
                RegistryProviderFactory.setDefault(this);
            }
        } catch (CoreException e) {
            throw new IpsException("can't set default provider", e);
        }

    }

    public void dispose() {
        if (RegistryProviderFactory.getDefault() == this) {
            RegistryProviderFactory.releaseDefault();
        }
    }

    private static class ClasspathRegistryStrategy extends RegistryStrategy {

        public ClasspathRegistryStrategy() {
            // no caching
            super(null, null);
        }

        @Override
        public void onStart(IExtensionRegistry registry, boolean loadedFromCache) {
            super.onStart(registry, loadedFromCache);
            boolean trace = Boolean.parseBoolean(
                    Abstractions.getDebugOption("org.faktorips.devtools.model/trace/PlainJavaRegistryProvider"));
            try {
                AtomicInteger id = new AtomicInteger(0);
                for (String descriptorFile : EXTENSION_DESCRIPTORS) {
                    if (trace) {
                        IpsLog.log(new IpsStatus(IStatus.INFO, "Scanning for " + descriptorFile + " contributions..."));
                    }
                    Enumeration<URL> resources = PlainJavaRegistryProvider.class.getClassLoader()
                            .getResources(descriptorFile);

                    scanForExtensions(resources, id, registry, trace);
                }
            } catch (IOException e) {
                IpsLog.log(new IpsStatus(IStatus.ERROR, "Scanning for contributions failed!" + e));
            }
        }

        private void scanForExtensions(Enumeration<URL> resources,
                AtomicInteger id,
                IExtensionRegistry registry,
                boolean trace) throws IOException {
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (trace) {
                    IpsLog.log(new IpsStatus(IStatus.INFO, "Processing " + url + " ..."));
                }
                Manifest manifest = readManifest(url);
                if (manifest == null) {
                    if (trace) {
                        IpsLog.log(new IpsStatus(IStatus.WARNING, url + " has no " + JarFile.MANIFEST_NAME));
                    }
                    continue;
                }
                String bundleId = manifest.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME)
                        .split(";")[0];
                if (bundleId == null) {
                    if (trace) {
                        IpsLog.log(new IpsStatus(IStatus.WARNING,
                                url + " has no " + Constants.BUNDLE_SYMBOLICNAME));
                    }
                    continue;
                }
                createContributor(id, registry, url, bundleId);
            }
        }

        private void createContributor(AtomicInteger id,
                IExtensionRegistry registry,
                URL url,
                String bundleId) throws IOException {
            RegistryContributor contributor = new RegistryContributor(String.valueOf(id.getAndIncrement()), bundleId,
                    null, null);
            try (InputStream stream = url.openStream()) {
                if (registry.addContribution(stream, contributor, false, null, null, null)) {
                    registry.getExtensionPoints(contributor);
                } else {
                    IpsLog.log(new IpsStatus(IStatus.ERROR, "Contributions can't be processed for " + url));
                }
            }
        }

        private Manifest readManifest(URL base) {
            try {
                String externalForm = base.toExternalForm();
                URL url = externalForm.startsWith("jar:file:")
                        ? URI.create(
                                externalForm.substring(0, externalForm.lastIndexOf("!/") + 2)
                                        + JarFile.MANIFEST_NAME)
                                .toURL()
                        : base.toURI().resolve(JarFile.MANIFEST_NAME).toURL();

                try (InputStream stream = url.openStream()) {
                    return new Manifest(stream);
                }
            } catch (IOException | URISyntaxException e) {
                return null;
            }
        }

    }
}