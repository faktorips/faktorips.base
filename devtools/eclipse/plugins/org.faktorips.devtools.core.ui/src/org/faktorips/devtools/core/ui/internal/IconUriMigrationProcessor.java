/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal;

import java.util.function.Predicate;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.framework.Bundle;

/**
 * Migrates icon URIs that were persisted in the workbench model (workbench.xmi) and point to
 * former GIF or PNG icons that have been replaced by SVG icons.
 */
public class IconUriMigrationProcessor {

    private static final String PLUGIN_URI_PREFIX = "platform:/plugin/"; //$NON-NLS-1$
    private static final String SVG_EXTENSION = ".svg"; //$NON-NLS-1$

    private final Predicate<String> resourceExists;

    public IconUriMigrationProcessor() {
        this(IconUriMigrationProcessor::bundleEntryExists);
    }

    IconUriMigrationProcessor(Predicate<String> resourceExists) {
        this.resourceExists = resourceExists;
    }

    @Execute
    public void process(MApplication application, EModelService modelService) {
        modelService.findElements(application, MUILabel.class, EModelService.ANYWHERE, element -> true)
                .forEach(this::migrate);
        application.getDescriptors().forEach(this::migrate);
    }

    void migrate(MUILabel label) {
        String iconUri = label.getIconURI();
        if (iconUri == null || !iconUri.startsWith(PLUGIN_URI_PREFIX)) {
            return;
        }
        int extensionStart = iconUri.lastIndexOf('.');
        if (extensionStart < 0) {
            return;
        }
        String extension = iconUri.substring(extensionStart);
        if (!(".gif".equalsIgnoreCase(extension) || ".png".equalsIgnoreCase(extension))) { //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        String svgUri = iconUri.substring(0, extensionStart) + SVG_EXTENSION;
        if (!resourceExists.test(iconUri) && resourceExists.test(svgUri)) {
            label.setIconURI(svgUri);
        }
    }

    private static boolean bundleEntryExists(String platformPluginUri) {
        String bundleAndPath = platformPluginUri.substring(PLUGIN_URI_PREFIX.length());
        int separator = bundleAndPath.indexOf('/');
        if (separator <= 0) {
            return false;
        }
        Bundle bundle = Platform.getBundle(bundleAndPath.substring(0, separator));
        return bundle != null && bundle.getEntry(bundleAndPath.substring(separator + 1)) != null;
    }
}
