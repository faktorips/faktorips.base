/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject.bundle;

import java.util.List;

import org.eclipse.core.runtime.IPath;

/**
 * This {@link IpsFolderBundleContentIndex} registers the folder structure for an
 * {@link IpsFolderBundle} using the {@link FolderExplorer}.
 * 
 * @author dicker
 */
public class IpsFolderBundleContentIndex extends AbstractIpsBundleContentIndex {

    private final FolderExplorer explorer;

    private final IPath bundleRoot;

    public IpsFolderBundleContentIndex(IPath bundleRoot, List<IPath> modelFolders) {
        this(modelFolders, bundleRoot, new FolderExplorer());
    }

    protected IpsFolderBundleContentIndex(List<IPath> modelFolders, IPath bundleRoot, FolderExplorer folderExplorer) {
        this.bundleRoot = bundleRoot;
        this.explorer = folderExplorer;
        initFolderStructure(modelFolders, bundleRoot);
    }

    private void initFolderStructure(List<IPath> modelFolders, IPath bundleRoot) {
        for (IPath relativeModelFolder : modelFolders) {
            IPath absoluteModelFolder = bundleRoot.append(relativeModelFolder);
            registerFolder(absoluteModelFolder, relativeModelFolder);
        }
    }

    private void registerFolders(List<IPath> absoluteFolders, IPath relativeModelPath) {
        for (IPath absoluteFolder : absoluteFolders) {
            registerFolder(absoluteFolder, relativeModelPath);
        }
    }

    private void registerFolder(IPath absoluteFolder, IPath relativeModelPath) {
        registerPaths(absoluteFolder, relativeModelPath);
        registerFolders(explorer.getFolders(absoluteFolder), relativeModelPath);
    }

    private void registerPaths(IPath absolutePathToFolder, IPath relativeModelFolder) {
        List<IPath> files = explorer.getFiles(absolutePathToFolder);
        for (IPath file : files) {
            IPath relativeFilePath = file.makeRelativeTo(bundleRoot.append(relativeModelFolder));
            registerPath(relativeModelFolder, relativeFilePath);
        }
    }

}
