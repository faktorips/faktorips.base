/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.IpsSrcFileViewItem;

public class ProductCmptViewItem extends IpsSrcFileViewItem {

    private ProductCmptViewItem parent;

    private final List<ProductCmptViewItem> children = new ArrayList<ProductCmptViewItem>();

    public ProductCmptViewItem(IIpsSrcFile ipsSrcFile) {
        super(ipsSrcFile);
    }

    public void addChild(ProductCmptViewItem child) {
        children.add(child);
        child.parent = this;
    }

    public IProductCmpt getProductCmpt() {
        if (getIpsSrcFile() != null && getIpsSrcFile().getIpsObject() instanceof IProductCmpt) {
            IProductCmpt productCmpt = (IProductCmpt)getIpsSrcFile().getIpsObject();
            return productCmpt;
        } else {
            return null;
        }
    }

    public String getName() {
        if (getProductCmpt() != null) {
            return getProductCmpt().getQualifiedName();
        } else {
            return null;
        }
    }

    public ProductCmptViewItem getParent() {
        return parent;
    }

    public List<ProductCmptViewItem> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public boolean contains(ProductCmptViewItem viewItem) {
        if (this.equals(viewItem)) {
            return true;
        } else {
            for (ProductCmptViewItem child : getChildren()) {
                if (child.contains(viewItem)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public String toString() {
        return getIpsSrcFile().getName();
    }

}
