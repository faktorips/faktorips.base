/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelContentProvider;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerConfiguration;

public class ProductContentProvider extends ModelContentProvider {
    

    public ProductContentProvider(ModelExplorerConfiguration config, boolean flatLayout) {
        super(config, flatLayout);
    }

    /**
     * For the productdefinitionExplorer do not display the default package,
     * only display its files as children of the given PackageFragmentRoot.
     * {@inheritDoc}
     */
    protected Object[] getPackageFragmentRootContent(IIpsPackageFragmentRoot root) throws CoreException {
        if (isFlatLayout) {
            IIpsPackageFragment[] fragments = root.getIpsPackageFragments();
            // filter out empty packagefragments if their IFolders do not contain files and at the
            // same time contain subfolders (subpackages) (this prevents empty or newly created
            // packagefragments from being hidden in the view)
            List filteredElements = new ArrayList();
            for (int i = 0; i < fragments.length; i++) {
                if(fragments[i].isDefaultPackage()){
                    filteredElements.addAll(Arrays.asList(getFileContent(fragments[i])));
                    continue;
                }
                if (hasChildren(fragments[i]) || fragments[i].getIpsChildPackageFragments().length == 0) {
                    filteredElements.add(fragments[i]);
                }
            }
            return filteredElements.toArray();
        } else {
            IIpsPackageFragment defaultPackage= root.getIpsDefaultPackageFragment();
            Object[] childPackages = defaultPackage.getIpsChildPackageFragments();
            if (hasChildren(root.getIpsDefaultPackageFragment())) {
                return concatenate(childPackages, getFileContent(defaultPackage));
            } else {
                return childPackages;
            }
        }
    }
}
