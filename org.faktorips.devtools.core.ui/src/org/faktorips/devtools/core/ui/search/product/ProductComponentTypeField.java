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

package org.faktorips.devtools.core.ui.search.product;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.controller.fields.AbstractTextField;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectContext;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;

public class ProductComponentTypeField extends AbstractTextField<IProductCmptType> {

    private IProductCmptType productCmptType;
    private final Button button;

    public ProductComponentTypeField(Text text, Button button) {
        super(text);
        this.button = button;
        button.addSelectionListener(new ProductCmptTypeSelectionListener());
    }

    @Override
    public void setValue(IProductCmptType newValue) {
        productCmptType = newValue;
        setText(productCmptType.getQualifiedName());
    }

    @Override
    protected IProductCmptType parseContent() throws Exception {
        if (productCmptType != null && productCmptType.getQualifiedName().equals(getText())) {
            return productCmptType;
        }

        IIpsProject[] ipsProductDefinitionProjects = IpsPlugin.getDefault().getIpsModel()
                .getIpsProductDefinitionProjects();
        for (IIpsProject ipsProject : ipsProductDefinitionProjects) {
            IProductCmptType productCmptTypeInstance = ipsProject.findProductCmptType(getText());

            if (productCmptTypeInstance != null) {
                return productCmptTypeInstance;
            }
        }
        return null;
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
