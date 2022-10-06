/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import java.util.ArrayList;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.eclipse.osgi.util.ManifestElement;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.model.internal.ipsproject.jdtcontainer.IpsContainer4JdtClasspathContainerType;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * This reader is used to create an {@link IIpsObjectPath} in when the option 'useManifest' in the
 * project properties is set to true.
 * <p>
 * The reader reads the MANIFEST.MF file of the project and creates an {@link IIpsObjectPath}
 * containing all the configured {@link IpsSrcFolderEntry source folder entries}. A specification of
 * the used elements could be found in {@link IpsBundleManifest}. Additionally this reader always
 * adds an {@link IpsContainerEntry} with the ID "org.eclipse.pde.core.requiredPlugins". This
 * classpath container is only available when org.eclipse.pde is installed to the running platform!
 * If it is available, the container consists of all dependencies configured as OSGi dependencies in
 * the MANIFEST.MF. Hence all necessary information about the {@link IpsObjectPath} is located in
 * the MANIFEST file.
 * 
 * @see IpsProjectProperties
 * 
 * @author dirmeier
 */
public class IpsObjectPathManifestReader {

    static final String REQUIRED_PLUGIN_CONTAINER = "org.eclipse.pde.core.requiredPlugins"; //$NON-NLS-1$

    private IpsObjectPath ipsObjectPath;

    private final IIpsProject ipsProject;

    private final IpsBundleManifest bundleManifest;

    public IpsObjectPathManifestReader(IpsBundleManifest bundleManifest, IIpsProject ipsProject) {
        this.bundleManifest = bundleManifest;
        this.ipsProject = ipsProject;
    }

    /**
     * This method returns reads the {@link IIpsObjectPath} from configured
     * {@link IpsBundleManifest} and returns it. The object path consists of all
     * {@link IpsSrcFolderEntry source folder entries} and one {@link IpsContainerEntry} for the
     * required plugins (OSGi dependencies).
     * 
     * @return A new {@link IIpsObjectPath} read from the manifest.
     */
    public IIpsObjectPath readIpsObjectPath() {
        ipsObjectPath = new IpsObjectPath(ipsProject);
        ipsObjectPath.setUsingManifest(true);
        ipsObjectPath.setOutputDefinedPerSrcFolder(true);
        ArrayList<IIpsObjectPathEntry> entries = new ArrayList<>();
        ManifestElement[] objectDirElements = bundleManifest.getObjectDirElements();
        for (ManifestElement manifestElement : objectDirElements) {
            entries.add(readEntry(manifestElement, bundleManifest));
        }
        entries.add(getRequiredPluginsContainerEntry());
        ipsObjectPath.setEntries(entries.toArray(new IIpsObjectPathEntry[entries.size()]));
        return ipsObjectPath;
    }

    private IpsSrcFolderEntry readEntry(ManifestElement manifestElement, IpsBundleManifest bundleManifest) {
        String objectDir = manifestElement.getValue();
        AFolder objectFolder = ipsProject.getProject().getFolder(objectDir);
        IpsSrcFolderEntry ipsSrcFolderEntry = new IpsSrcFolderEntry(ipsObjectPath, objectFolder);
        String srcOutDir = bundleManifest.getSourcecodeOutput(objectDir);
        AFolder srcOutputFolder = ipsProject.getProject().getFolder(srcOutDir);
        ipsSrcFolderEntry.setSpecificOutputFolderForMergableJavaFiles(srcOutputFolder);
        String resourceOutDir = bundleManifest.getResourceOutput(objectDir);
        AFolder resourceOutputFolder = ipsProject.getProject().getFolder(resourceOutDir);
        ipsSrcFolderEntry.setSpecificOutputFolderForDerivedJavaFiles(resourceOutputFolder);
        ipsSrcFolderEntry.setSpecificBasePackageNameForMergableJavaClasses(bundleManifest.getBasePackage(objectDir));
        ipsSrcFolderEntry.setSpecificBasePackageNameForDerivedJavaClasses(bundleManifest.getBasePackage(objectDir));
        ipsSrcFolderEntry.setUniqueQualifier(bundleManifest.getUniqueQualifier(objectDir));
        String tocPath = bundleManifest.getTocPath(manifestElement);
        if (IpsStringUtils.isNotEmpty(tocPath)) {
            ipsSrcFolderEntry.setBasePackageRelativeTocPath(tocPath);
        }
        String validationMessagesBundle = bundleManifest.getValidationMessagesBundle(manifestElement);
        if (IpsStringUtils.isNotEmpty(validationMessagesBundle)) {
            ipsSrcFolderEntry.setValidationMessagesBundle(validationMessagesBundle);
        }
        return ipsSrcFolderEntry;
    }

    private IIpsObjectPathEntry getRequiredPluginsContainerEntry() {
        IpsContainerEntry ipsContainerEntry = new IpsContainerEntry(ipsObjectPath);
        ipsContainerEntry.setContainerTypeId(IpsContainer4JdtClasspathContainerType.ID);
        ipsContainerEntry.setOptionalPath(REQUIRED_PLUGIN_CONTAINER);
        return ipsContainerEntry;
    }
}
