/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.core.ui.views.modelexplorer.LayoutStyle;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelContentProvider;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerConfiguration;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class ProductContentProvider extends ModelContentProvider {

    public ProductContentProvider(ModelExplorerConfiguration config, LayoutStyle layoutStyle) {
        super(config, layoutStyle);
    }

    /**
     * For the productdefinitionExplorer do not display the default package, only display its files
     * as children of the given PackageFragmentRoot. {@inheritDoc}
     */
    @Override
    protected Object[] getPackageFragmentRootContent(IIpsPackageFragmentRoot root) {
        if (getLayoutStyle() == LayoutStyle.FLAT) {
            IIpsPackageFragment[] fragments = root.getIpsPackageFragments();
            // filter out empty packagefragments if their IFolders do not contain files and at the
            // same time contain subfolders (subpackages) (this prevents empty or newly created
            // packagefragments from being hidden in the view)
            List<Object> filteredElements = new ArrayList<>();
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

    @Override
    protected Object[] getUnfilteredChildren(Object parentElement) {
        // exclude all non ips project definition projects
        if (parentElement instanceof IIpsProject ipsProject) {
            if (!ipsProject.isProductDefinitionProject()) {
                return EMPTY_ARRAY;
            }
        }
        return super.getUnfilteredChildren(parentElement);
    }
}
