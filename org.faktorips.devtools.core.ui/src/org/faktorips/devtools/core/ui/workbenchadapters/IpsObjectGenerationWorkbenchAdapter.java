/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.workbenchadapters;

import java.util.Locale;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class IpsObjectGenerationWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    private final static String GENERATION_IMAGE_BASE = "Generation"; //$NON-NLS-1$

    private ImageDescriptor cachedImageDescriptor;

    public IpsObjectGenerationWorkbenchAdapter() {
        IpsPlugin.getDefault().getIpsPreferences().addChangeListener(new IPropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                cachedImageDescriptor = null;
            }
        });
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        // Image is independent of object
        return getDefaultImageDescriptor();
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        if (cachedImageDescriptor == null) {
            cachedImageDescriptor = getGenerationImageDescriptor();
        }
        return cachedImageDescriptor;
    }

    public ImageDescriptor getGenerationImageDescriptor() {
        IChangesOverTimeNamingConvention namingConvention = IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention();
        String id = namingConvention.getId();
        Locale locale = IpsPlugin.getDefault().getUsedLanguagePackLocale();

        ImageDescriptor imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
        // first we try to load the image with the full locale, i.e. de_DE
        if (locale.toString().length() > 0) {
            imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                    id + "_" + GENERATION_IMAGE_BASE + "_" + locale.toString() + ".gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        // if the locale has a country code (e.g. DE), we now ignore this and try
        // to load the image only with the language code (e.g. de).
        if (!exists(imageDescriptor) && locale.getCountry().length() != 0) {
            imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                    id + "_" + GENERATION_IMAGE_BASE + "_" + locale.getLanguage() + ".gif"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // neither for full locale nor for only language code an image was found,
        // so try to load the base image and let the missing image descriptor be
        // returned if not found.
        if (!exists(imageDescriptor)) {
            imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                    id + "_" + GENERATION_IMAGE_BASE + ".gif"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // if image still does not exists try to load default image
        if (!exists(imageDescriptor)) {
            imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(GENERATION_IMAGE_BASE + ".gif");
        }

        return imageDescriptor;
    }

    private boolean exists(ImageDescriptor imageDescriptor) {
        return (imageDescriptor != null && imageDescriptor != ImageDescriptor.getMissingImageDescriptor());
    }
}
