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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

public class ProductCmptRefControl extends IpsObjectRefControl {

    private boolean includeCmptsForSubtypes = true;
    private IProductCmpt[] toExclude = new IProductCmpt[0];
    private IProductCmptType productCmptType;
    private boolean searchTemplates;

    public ProductCmptRefControl(IIpsProject project, Composite parent, UIToolkit toolkit) {
        this(Arrays.asList(project), parent, toolkit);
    }

    public ProductCmptRefControl(List<IIpsProject> projects, Composite parent, UIToolkit toolkit) {
        super(projects, parent, toolkit, Messages.ProductCmptRefControl_title,
                Messages.ProductCmptRefControl_description);
    }

    public ProductCmptRefControl(IIpsProject project, Composite parent, UIToolkit toolkit, boolean allowEmptyRef) {
        this(Arrays.asList(project), parent, toolkit, allowEmptyRef);
    }

    public ProductCmptRefControl(List<IIpsProject> projects, Composite parent, UIToolkit toolkit,
            boolean allowEmptyRef) {
        super(projects, parent, toolkit, Messages.ProductCmptRefControl_title,
                Messages.ProductCmptRefControl_description, allowEmptyRef);
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
    protected IIpsSrcFile[] getIpsSrcFiles() {

        Set<IIpsSrcFile> ipsSrcFiles = new LinkedHashSet<>();
        for (IIpsProject ipsProject : getIpsProjects()) {
            ipsSrcFiles.addAll(findIpsSrcFiles(ipsProject));
        }

        if (ipsSrcFiles.isEmpty()) {
            return new IIpsSrcFile[0];
        }

        for (IProductCmpt productCmpt : toExclude) {
            ipsSrcFiles.remove(productCmpt.getIpsSrcFile());
        }

        return ipsSrcFiles.toArray(new IIpsSrcFile[ipsSrcFiles.size()]);
    }

    private Collection<? extends IIpsSrcFile> findIpsSrcFiles(IIpsProject ipsProject) {
        if (searchTemplates) {
            return ipsProject.findCompatibleProductTemplates(productCmptType);
        } else {
            return Arrays.asList(ipsProject.findAllProductCmptSrcFiles(productCmptType, includeCmptsForSubtypes));
        }
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

    /**
     * Configures this ref-control to search for either product components or templates.
     * 
     * @param searchTemplates <code>true</code> to search for product templates instead of product
     *            components. <code>false</code> to search for product components only (default).
     */
    public void setSearchTemplates(boolean searchTemplates) {
        this.searchTemplates = searchTemplates;

    }
}
