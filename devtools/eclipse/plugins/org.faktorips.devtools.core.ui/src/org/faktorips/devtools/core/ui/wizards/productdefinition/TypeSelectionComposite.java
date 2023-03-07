/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.util.Collection;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.ui.LocalizedLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.wizards.ElementSelectionComposite;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.internal.ProductCmptDecorator;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * This type selection composite contains two columns. On the left hand you see a list of types you
 * could select. On the right hand you see the description of the selected element.
 * 
 * @author dirmeier
 */
public class TypeSelectionComposite extends ElementSelectionComposite<IIpsObject> {

    private Label title;

    /**
     * Constructs a new type selection composite.
     * 
     * @param parent the parent composite
     * @param toolkit the {@link UIToolkit} to create the internal controls
     * @param pmo a presentation model object to bind the selected type
     * @param property the property of the presentation model object
     * @param inputList The input list for the type selection. This list instance should never
     *            change, the content may change of course
     */
    public TypeSelectionComposite(Composite parent, UIToolkit toolkit, BindingContext bindingContext,
            PresentationModelObject pmo, String property, Collection<? extends IIpsObject> inputList) {
        super(parent, toolkit, bindingContext, pmo, property, inputList, new ProductCmptWizardTypeLabelProvider(),
                IIpsObject.class);
    }

    @Override
    protected void createControls() {
        title = getToolkit().createLabel(this, IpsStringUtils.EMPTY);
        title.setLayoutData(new GridData(SWT.BEGINNING, SWT.END, false, false));

        getToolkit().createVerticalSpacer(this, 0);
        super.createControls();
    }

    public void setTitle(String titleString) {
        title.setText(titleString);
    }

    private static class ProductCmptWizardTypeLabelProvider extends LocalizedLabelProvider {

        private final ProductCmptDecorator productCmptDecorator = (ProductCmptDecorator)IIpsDecorators
                .get(IpsObjectType.PRODUCT_CMPT);

        @Override
        public Image getImage(Object element) {
            if (element instanceof IProductCmptType productCmptType) {
                ImageDescriptor descriptorForInstancesOf = productCmptDecorator
                        .getImageDescriptorForInstancesOf(productCmptType);
                return JFaceResources.getResources().createImage(descriptorForInstancesOf);
            }

            return super.getImage(element);
        }

    }
}
