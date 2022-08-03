/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types.chooser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.ILinkStrategy;
import org.faktorips.devtools.model.decorators.IImageHandling;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

/**
 * Element to chose a {@link IpsObjectType}
 * 
 * TODO: nearly complete implementation
 * 
 * @author dicker
 */
public class TypeChooserPageElement extends AbstractPageElement {
    private ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
    private final Collection<IpsObjectType> types;

    public TypeChooserPageElement(DocumentationContext context) {
        super(context);
        types = Arrays.asList(context.getDocumentedIpsObjectTypes());
    }

    public TypeChooserPageElement(DocumentationContext context, Set<IpsObjectType> relatedObjectTypes) {
        super(context);
        types = relatedObjectTypes;
    }

    @Override
    protected void buildInternal() {
        wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());

        for (IpsObjectType ipsObjectType : getContext().getDocumentedIpsObjectTypes()) {

            if (!types.contains(ipsObjectType) || getContext().getDocumentedSourceFiles(ipsObjectType).isEmpty()) {
                continue;
            }

            // TODO Bedingung aendern durch geeigneten Filter, sobald Auswahl auf Wizard angeboten
            // wird. Achtung: evtl. nicht anzeige disabled images nicht moeglich bei headless build
            if (getContext().getDocumentedSourceFiles(ipsObjectType).isEmpty()) {
                addDisabledImage(ipsObjectType, wrapper);
                continue;
            }
            addLink(ipsObjectType, wrapper);
        }
    }

    private void addDisabledImage(final IpsObjectType ipsObjectType, final ICompositePageElement wrapper) {
        Display display = (Display)IIpsDecorators.getImageHandling().getResourceManager().getDevice();
        Runnable runnable = () -> {
            IImageHandling imageHandling = IIpsDecorators.getImageHandling();
            ImageDescriptor defaultImageDescriptor = IIpsDecorators.getDefaultImageDescriptor(ipsObjectType);
            ImageDescriptor disabledSharedImage = imageHandling.getDisabledImageDescriptor(defaultImageDescriptor);
            ImageData imageData = disabledSharedImage.getImageData(100);
            wrapper.addPageElements(new ImagePageElement(imageData, ipsObjectType.getDisplayName(), ipsObjectType
                    .getFileExtension(), getContext()));
        };
        display.syncExec(runnable);
    }

    private void addLink(IpsObjectType ipsObjectType, ICompositePageElement wrapper) {
        ILinkStrategy linkStrategy = new LinkToObjectTypeClassesStrategy(ipsObjectType);

        ImageData imageData = IIpsDecorators.getDefaultImageDescriptor(ipsObjectType).getImageData(100);

        IPageElement pageElement = new ImagePageElement(imageData, ipsObjectType.getDisplayName(),
                ipsObjectType.getFileExtension(), getContext());

        wrapper.addPageElements(linkStrategy.createLink(getContext(), pageElement));
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        if (types.size() > 1) {
            wrapper.acceptLayouter(layouter);
        }
    }

}
