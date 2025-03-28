/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.fl.FlFunction;

/**
 * Label provider that provides default images and labels (texts) for ips elements.
 * <p>
 * For {@link IIpsSrcFile}s you could specify whether to use icon and label from the source file
 * itself or to look for the {@link IIpsObject} enclosed in the source file.
 *
 * @author Cornelius Dirmeier
 */
public class DefaultLabelProvider extends LabelProvider {

    public DefaultLabelProvider() {
        super();
    }

    /**
     * Creates an DefaultLabelProvider with additional IpsSourceFile mapping support: In case of an
     * IpsSourceFile the text and the image of the corresponding IpsObject will be returned.
     */
    public static ILabelProvider createWithIpsSourceFileMapping() {
        return new DefaultLabelProvider();
    }

    @Override
    public Image getImage(Object element) {
        return switch (element) {
            case IAdaptable adaptable -> {
                Image result = IpsUIPlugin.getImageHandling().getImage(adaptable);
                if (result != null) {
                    yield result;
                }
                // check adaptable to IIpsSrcFile
                IIpsSrcFile adaptedIpsSrcFile = adaptable.getAdapter(IIpsSrcFile.class);
                if (adaptedIpsSrcFile != null) {
                    yield IIpsDecorators.getImageHandling().getImage(adaptedIpsSrcFile);
                }
                yield super.getImage(element);
            }
            case EnumTypeDatatypeAdapter enumTypeDatatypeAdapter -> getImage(enumTypeDatatypeAdapter.getEnumType());
            case Datatype $ -> IIpsDecorators.getImageHandling().getSharedImage("Datatype.gif", true); //$NON-NLS-1$
            case FlFunction<?> $ -> IpsUIPlugin.getImageHandling().getSharedImage("Function.gif", true); //$NON-NLS-1$
            default -> super.getImage(element);
        };
    }

    @Override
    public String getText(Object element) {
        return switch (element) {
            case null -> IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            case IIpsElement ipsElement -> IpsUIPlugin.getLabel(ipsElement);
            case IpsSrcFileViewItem viewItem -> getText(viewItem.getIpsSrcFile());
            case EnumTypeDatatypeAdapter enumTypeDatatypeAdapter -> getText(enumTypeDatatypeAdapter.getEnumType());
            default -> element.toString();
        };
    }

    @Override
    public void dispose() {
        super.dispose();
    }

}
