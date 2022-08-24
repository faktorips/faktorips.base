/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.bundle;

import java.nio.file.Path;
import java.util.List;

import org.faktorips.devtools.abstraction.util.PathUtil;

/**
 * This {@link IpsFolderBundleContentIndex} registers the folder structure for an
 * {@link IpsFolderBundle} using the {@link FolderExplorer}.
 * 
 * @author dicker
 */
public class IpsFolderBundleContentIndex extends AbstractIpsBundleContentIndex {

    private final FolderExplorer explorer;

    private final Path bundleRoot;

    public IpsFolderBundleContentIndex(Path bundleRoot, List<Path> modelFolders) {
        this(modelFolders, bundleRoot, new FolderExplorer());
    }

    protected IpsFolderBundleContentIndex(List<Path> modelFolders, Path bundleRoot, FolderExplorer folderExplorer) {
        this.bundleRoot = bundleRoot;
        explorer = folderExplorer;
        initFolderStructure(modelFolders, bundleRoot);
    }

    private void initFolderStructure(List<Path> modelFolders, Path bundleRoot) {
        for (Path relativeModelFolder : modelFolders) {
            Path absoluteModelFolder = bundleRoot.resolve(relativeModelFolder);
            registerFolder(absoluteModelFolder, relativeModelFolder);
        }
    }

    private void registerFolders(List<Path> absoluteFolders, Path relativeModelPath) {
        for (Path absoluteFolder : absoluteFolders) {
            registerFolder(absoluteFolder, relativeModelPath);
        }
    }

    private void registerFolder(Path absoluteFolder, Path relativeModelPath) {
        registerPaths(absoluteFolder, relativeModelPath);
        registerFolders(explorer.getFolders(absoluteFolder), relativeModelPath);
    }

    private void registerPaths(Path absolutePathToFolder, Path relativeModelFolder) {
        List<Path> files = explorer.getFiles(absolutePathToFolder);
        for (Path file : files) {
            Path relativeFilePath = PathUtil.makeRelativeTo(file, bundleRoot.resolve(relativeModelFolder));
            registerPath(relativeModelFolder, relativeFilePath);
        }
    }

}
