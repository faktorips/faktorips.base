/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.AbstractTextField;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectContext;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;

/**
 * The ProductComponentTypeField is a field to choose a {@link IProductCmptType}.
 * <p>
 * This class is necessary, because {@link ProductCmptType2RefControl} takes
 * {@link IProductCmptType IProductCmptTypes} out of one given project. The search must provide all
 * IProductCmptTypes within the workspace.
 * 
 * @author dicker
 */
public class ProductComponentTypeField extends AbstractTextField<IProductCmptType> {

    private IProductCmptType productCmptType;
    private final Button button;

    public ProductComponentTypeField(UIToolkit toolkit, Composite parent) {
        super(toolkit.createText(parent));
        this.button = toolkit.createButton(parent, Messages.ProductSearchPage_labelChooseProductComponentType);
        button.addSelectionListener(new ProductCmptTypeSelectionListener());
    }

    @Override
    public void setValue(IProductCmptType newValue) {
        productCmptType = newValue;
        setText(IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(newValue));
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
