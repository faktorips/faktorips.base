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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.eclipse.swt.graphics.ImageData;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;

public class TypeChosePageElement extends AbstractPageElement {
    private AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
    private final Collection<IpsObjectType> types;
    private final DocumentorConfiguration config;

    public TypeChosePageElement(DocumentorConfiguration config) {
        this.config = config;
        this.types = Arrays.asList(config.getDocumentedIpsObjectTypes());
    }

    public TypeChosePageElement(DocumentorConfiguration config, Set<IpsObjectType> relatedObjectTypes) {
        this.config = config;
        this.types = relatedObjectTypes;
    }

    @Override
    public void build() {
        wrapper = new WrapperPageElement(WrapperType.BLOCK);

        for (IpsObjectType ipsObjectType : config.getDocumentedIpsObjectTypes()) {
            /*
             * Image image =
             * IpsUIPlugin.getImageHandling().getDefaultImage(ipsObjectType.getImplementingClass());
             * 
             * // image.getImageData().
             * 
             * ImageDescriptor defaultImageDescriptor =
             * IpsUIPlugin.getImageHandling().getDefaultImageDescriptor(
             * ipsObjectType.getImplementingClass());
             * 
             * if (!types.contains(ipsObjectType)) { // defaultImageDescriptor =
             * IpsUIPlugin.getImageHandling().createDisabledImageDescriptor( //
             * defaultImageDescriptor); image =
             * IpsUIPlugin.getImageHandling().getDisabledImage(ipsObjectType); }
             * 
             * System.out.println(ipsObjectType + " " + defaultImageDescriptor);
             * 
             * ImageData imageData = image.getImageData(); // imageData.
             */

            // TODO hier die nicht vorhandenen disabled anzeigen!!!
            if (!types.contains(ipsObjectType)) {
                continue;
            }

            /*
             * StringBuilder imgName = new StringBuilder(); imgName.append(ipsObjectType.getId());
             * if (!types.contains(ipsObjectType)) { // imgName.append("Disabled"); }
             * imgName.append(".gif");
             * 
             * System.out.println(imgName);
             * 
             * ImageDescriptor imageData =
             * IpsUIPlugin.getImageHandling().getSharedImageDescriptor(imgName.toString(), true) ;
             * if (!types.contains(ipsObjectType)) {
             * 
             * }
             */
            ImageData imageData = IpsUIPlugin.getImageHandling().getDefaultImage(ipsObjectType.getImplementingClass())
                    .getImageData();

            ImagePageElement pageElement = new ImagePageElement(imageData, ipsObjectType.getDisplayName(),
                    ipsObjectType.getFileExtension());
            wrapper.addPageElements(pageElement);
        }

    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        if (types.size() > 1) {
            wrapper.acceptLayouter(layouter);
        }
    }

}
