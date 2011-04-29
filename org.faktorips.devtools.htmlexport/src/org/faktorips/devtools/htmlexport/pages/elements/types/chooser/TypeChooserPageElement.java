/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.types.chooser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin.ImageHandling;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.ILinkStrategy;

/**
 * Element to chose a {@link IpsObjectType}
 * 
 * TODO: nearly complete implementation
 * 
 * @author dicker
 */
public class TypeChooserPageElement extends AbstractPageElement {
    private AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
    private final Collection<IpsObjectType> types;
    private final DocumentationContext context;

    public TypeChooserPageElement(DocumentationContext context) {
        this.context = context;
        this.types = Arrays.asList(context.getDocumentedIpsObjectTypes());
    }

    public TypeChooserPageElement(DocumentationContext context, Set<IpsObjectType> relatedObjectTypes) {
        this.context = context;
        this.types = relatedObjectTypes;
    }

    @Override
    public void build() {
        wrapper = new WrapperPageElement(WrapperType.BLOCK);

        for (IpsObjectType ipsObjectType : context.getDocumentedIpsObjectTypes()) {

            if (!types.contains(ipsObjectType) || context.getDocumentedSourceFiles(ipsObjectType).isEmpty()) {
                continue;
            }

            // TODO Bedingung aendern durch geeigneten Filter, sobald Auswahl auf Wizard angeboten
            // wird. Achtung: evtl. nicht anzeige disabled images nicht moeglich bei headless build
            if (context.getDocumentedSourceFiles(ipsObjectType).isEmpty()) {
                addDisabledImage(ipsObjectType, wrapper);
                continue;
            }
            addLink(ipsObjectType, wrapper);
        }
    }

    private void addDisabledImage(final IpsObjectType ipsObjectType, final ICompositePageElement wrapper) {
        Display display = (Display)IpsUIPlugin.getImageHandling().getResourceManager().getDevice();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                ImageHandling imageHandling = IpsUIPlugin.getImageHandling();
                ImageDescriptor defaultImageDescriptor = imageHandling.getDefaultImageDescriptor(ipsObjectType
                        .getImplementingClass());
                Image disabledSharedImage = imageHandling.getDisabledSharedImage(defaultImageDescriptor);
                ImageData imageData = disabledSharedImage.getImageData();
                wrapper.addPageElements(new ImagePageElement(imageData, ipsObjectType.getDisplayName(), ipsObjectType
                        .getFileExtension()));
            }
        };
        display.syncExec(runnable);
    }

    private void addLink(IpsObjectType ipsObjectType, ICompositePageElement wrapper) {
        ILinkStrategy linkStrategy = new LinkToObjectTypeClassesStrategy(ipsObjectType);

        ImageData imageData = IpsUIPlugin.getImageHandling().getDefaultImage(ipsObjectType.getImplementingClass())
                .getImageData();

        PageElement pageElement = new ImagePageElement(imageData, ipsObjectType.getDisplayName(),
                ipsObjectType.getFileExtension());

        wrapper.addPageElements(linkStrategy.createLink(pageElement));
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        if (types.size() > 1) {
            wrapper.acceptLayouter(layouter);
        }
    }

}
