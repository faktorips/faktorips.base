/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.AbstractTextField;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectContext;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * The ProductComponentTypeField is a field to choose a {@link IProductCmptType}.
 * <p>
 * This class is necessary, because {@link ProductCmptType2RefControl} takes {@link IProductCmptType
 * IProductCmptTypes} out of one given project. The search must provide all IProductCmptTypes within
 * the workspace.
 * 
 * @author dicker
 */
public class ProductComponentTypeField extends AbstractTextField<IProductCmptType> {

    private IProductCmptType productCmptType;
    private final Button button;

    public ProductComponentTypeField(UIToolkit toolkit, Composite parent) {
        super(toolkit.createText(parent));
        button = toolkit.createButton(parent, Messages.ProductSearchPage_labelChooseProductComponentType);
        button.addSelectionListener(new ProductCmptTypeSelectionListener());
    }

    @Override
    public void setValue(IProductCmptType newValue) {
        productCmptType = newValue;
        String localizedLabel = (newValue == null) ? IpsStringUtils.EMPTY
                : IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(newValue);
        setText(localizedLabel);
    }

    @Override
    protected IProductCmptType parseContent() throws Exception {
        return productCmptType;
    }

    private final class ProductCmptTypeSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            OpenIpsObjectContext context = new OpenIpsObjectContext(true) {

                @Override
                protected boolean isAllowedSrcFile(IIpsSrcFile srcFile) {
                    return IpsObjectType.PRODUCT_CMPT_TYPE.equals(srcFile.getIpsObjectType());
                }

            };

            OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(button.getShell(),
                    Messages.ProductSearchPage_labelChooseProductComponentType, context);
            if (dialog.open() == Window.OK) {
                IProductCmptType object = (IProductCmptType)dialog.getSelectedObject().getAdapter(IIpsObject.class);
                if (object != null) {
                    setText(object.getQualifiedName());
                    setValue(object);
                }
            }
        }
    }

}
