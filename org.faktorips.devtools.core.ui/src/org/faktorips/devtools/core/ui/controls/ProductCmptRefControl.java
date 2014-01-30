/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;

public class ProductCmptRefControl extends IpsObjectRefControl {

    private boolean includeCmptsForSubtypes = true;
    private IProductCmpt[] toExclude = new IProductCmpt[0];
    private IProductCmptType productCmptType;

    public ProductCmptRefControl(IIpsProject project, Composite parent, UIToolkit toolkit) {
        this(Arrays.asList(project), parent, toolkit);
    }

    public ProductCmptRefControl(List<IIpsProject> projects, Composite parent, UIToolkit toolkit) {
        super(projects, parent, toolkit, Messages.ProductCmptRefControl_title,
                Messages.ProductCmptRefControl_description);
    }

    /**
     * @param productCmptType The product component type for which product components should be
     *            selectable.
     * @param includeCmptsForSubtypes <code>true</code> if also product components for subtypes
     *            should be selectable.
     */
    public void setProductCmptType(IProductCmptType productCmptType, boolean includeCmptsForSubtypes) {
        this.productCmptType = productCmptType;
        this.includeCmptsForSubtypes = includeCmptsForSubtypes;
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {

        Set<IIpsSrcFile> ipsSrcFiles = new LinkedHashSet<IIpsSrcFile>();
        for (IIpsProject ipsProject : getIpsProjects()) {
            ipsSrcFiles.addAll(Arrays.asList(ipsProject.findAllProductCmptSrcFiles(productCmptType,
                    includeCmptsForSubtypes)));
        }

        if (ipsSrcFiles.isEmpty()) {
            return new IIpsSrcFile[0];
        }

        for (IProductCmpt productCmpt : toExclude) {
            ipsSrcFiles.remove(productCmpt.getIpsSrcFile());
        }

        return ipsSrcFiles.toArray(new IIpsSrcFile[ipsSrcFiles.size()]);
    }

    /**
     * Set all product components to exclude from result.
     * 
     * @param cmpts All product components to exclude.
     */
    public void setProductCmptsToExclude(IProductCmpt[] cmpts) {
        if (cmpts == null) {
            toExclude = new IProductCmpt[0];
        } else {
            toExclude = cmpts;
        }
    }
}
