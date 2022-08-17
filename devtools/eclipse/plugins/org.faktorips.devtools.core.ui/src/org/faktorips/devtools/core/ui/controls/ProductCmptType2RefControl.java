/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

/**
 * Control to edit references to product component types.
 */
public class ProductCmptType2RefControl extends IpsObjectRefControl {

    private boolean excludeAbstractTypes;

    public ProductCmptType2RefControl(IIpsProject project, Composite parent, UIToolkit toolkit,
            boolean excludeAbstractTypes) {
        this(Arrays.asList(project), parent, toolkit, excludeAbstractTypes);
    }

    public ProductCmptType2RefControl(List<IIpsProject> projects, Composite parent, UIToolkit toolkit,
            boolean excludeAbstractTypes) {
        super(projects, parent, toolkit, Messages.ProductCmptTypeRefControl_title,
                Messages.ProductCmptTypeRefControl_description);
        this.excludeAbstractTypes = excludeAbstractTypes;
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() {

        IIpsSrcFile[] allProductCmptTypes = findIpsSrcFilesByType(IpsObjectType.PRODUCT_CMPT_TYPE);

        if (!excludeAbstractTypes) {
            return allProductCmptTypes;
        }

        Set<IIpsSrcFile> filteredProductCmptTypes = new LinkedHashSet<>();

        for (IIpsSrcFile ipsSrcFile : allProductCmptTypes) {
            if (!Boolean.valueOf(ipsSrcFile.getPropertyValue(IProductCmptType.PROPERTY_ABSTRACT))) {
                filteredProductCmptTypes.add(ipsSrcFile);
            }
        }

        return filteredProductCmptTypes.toArray(new IIpsSrcFile[filteredProductCmptTypes.size()]);
    }

    /**
     * Returns the product component type entered in this control. Returns <code>null</code> if the
     * text in the control does not identify a product component type. If abstract types are set to
     * be exclueded but the type found is abstract it is returned nevertheless!
     * 
     * @throws IpsException if an exception occurs while searching for the type.
     */
    public IProductCmptType findProductCmptType() {
        return (IProductCmptType)findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE);
    }
}
