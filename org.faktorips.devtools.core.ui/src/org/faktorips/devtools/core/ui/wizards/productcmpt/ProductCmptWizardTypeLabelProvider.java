/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.LocalizedLabelProvider;
import org.faktorips.devtools.core.ui.workbenchadapters.ProductCmptWorkbenchAdapter;

public class ProductCmptWizardTypeLabelProvider extends LocalizedLabelProvider {

    private final ProductCmptWorkbenchAdapter productCmptWorkbenchAdapter = new ProductCmptWorkbenchAdapter();

    @Override
    public Image getImage(Object element) {
        if (element instanceof IProductCmptType) {
            IProductCmptType productCmptType = (IProductCmptType)element;
            ImageDescriptor descriptorForInstancesOf = productCmptWorkbenchAdapter
                    .getImageDescriptorForInstancesOf(productCmptType);
            return JFaceResources.getResources().createImage(descriptorForInstancesOf);
        }

        return super.getImage(element);
    }

}
