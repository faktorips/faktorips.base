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

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.views.modelexplorer.LayoutStyle;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelContentProvider;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerConfiguration;

public class ProductContentProvider extends ModelContentProvider {

    public ProductContentProvider(ModelExplorerConfiguration config, LayoutStyle layoutStyle) {
        super(config, layoutStyle);
    }

    /**
     * For the productdefinitionExplorer do not display the default package, only display its files
     * as children of the given PackageFragmentRoot. {@inheritDoc}
     */
    @Override
    protected Object[] getPackageFragmentRootContent(IIpsPackageFragmentRoot root) throws CoreException {
        if (getLayoutStyle() == LayoutStyle.FLAT) {
            IIpsPackageFragment[] fragments = root.getIpsPackageFragments();
            // filter out empty packagefragments if their IFolders do not contain files and at the
            // same time contain subfolders (subpackages) (this prevents empty or newly created
            // packagefragments from being hidden in the view)
            List<Object> filteredElements = new ArrayList<Object>();
            for (IIpsPackageFragment fragment : fragments) {
                if (fragment.isDefaultPackage()) {
                    filteredElements.addAll(Arrays.asList(getFileContent(fragment)));
                    continue;
                }
                if (hasChildren(fragment) || fragment.getChildIpsPackageFragments().length == 0) {
                    filteredElements.add(fragment);
                }
            }
            return filteredElements.toArray();
        } else {
            IIpsPackageFragment defaultPackage = root.getDefaultIpsPackageFragment();
            Object[] childPackages = defaultPackage.getChildIpsPackageFragments();
            if (hasChildren(root.getDefaultIpsPackageFragment())) {
                return concatenate(childPackages, getFileContent(defaultPackage));
            } else {
                return childPackages;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object[] getUnfilteredChildren(Object parentElement) {
        // exclude all non ips project definition projects
        if (parentElement instanceof IIpsProject) {
            IIpsProject ipsProject = (IIpsProject)parentElement;
            if (!ipsProject.isProductDefinitionProject()) {
                return EMPTY_ARRAY;
            }
        }
        return super.getUnfilteredChildren(parentElement);
    }
}
