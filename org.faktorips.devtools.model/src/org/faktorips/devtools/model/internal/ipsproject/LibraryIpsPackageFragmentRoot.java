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
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsobject.LibraryIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsLibraryEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsStorage;
import org.faktorips.devtools.model.ipsproject.ILibraryIpsPackageFragmentRoot;
import org.faktorips.devtools.model.plugin.IpsStatus;

/**
 * {@link IIpsPackageFragmentRoot} for Libraries.
 * 
 * @author Jan Ortmann
 */
public class LibraryIpsPackageFragmentRoot extends AbstractIpsPackageFragmentRoot
        implements ILibraryIpsPackageFragmentRoot {

    private IIpsStorage storage;

    public LibraryIpsPackageFragmentRoot(IIpsProject ipsProject, IIpsStorage storage) {
        super(ipsProject, storage.getName());
        this.storage = storage;
    }

    @Override
    public IIpsStorage getIpsStorage() {
        return storage;
    }

    @Override
    public boolean exists() {
        if (getIpsStorage() == null) {
            return false;
        }
        return getIpsStorage().exists();
    }

    @Override
    public APackageFragmentRoot getArtefactDestination(boolean derived) {
        IIpsLibraryEntry entry = (IIpsLibraryEntry)getIpsObjectPathEntry();
        String path;
        if (entry.getPath().isAbsolute()) {
            path = entry.getPath().toString();
        } else {
            path = getIpsProject().getProject().getLocation().resolve(entry.getPath()).toString();
        }
        return getIpsProject().getJavaProject().getPackageFragmentRoot(path);
    }

    @Override
    public IIpsPackageFragment[] getIpsPackageFragments() {
        List<IIpsPackageFragment> list = getIpsPackageFragmentsAsList();
        return list.toArray(new IIpsPackageFragment[list.size()]);
    }

    private List<IIpsPackageFragment> getIpsPackageFragmentsAsList() {
        if (getIpsStorage() == null) {
            return new ArrayList<>(0);
        }

        String[] packNames = storage.getNonEmptyPackages();
        List<IIpsPackageFragment> list = new ArrayList<>(packNames.length);
        for (String packName : packNames) {
            list.add(new LibraryIpsPackageFragment(this, packName));
        }

        return list;
    }

    @Override
    protected IIpsPackageFragment newIpsPackageFragment(String name) {
        return new LibraryIpsPackageFragment(this, name);
    }

    @Override
    public AResource[] getNonIpsResources() {
        return new AResource[0];
    }

    @Override
    public IIpsPackageFragment createPackageFragment(String name, boolean force, IProgressMonitor monitor)
            {

        throw newExceptionMethodNotAvailableForArchvies();
    }

    @Override
    public AResource getCorrespondingResource() {
        return storage.getCorrespondingResource();
    }

    private IpsException newExceptionMethodNotAvailableForArchvies() {
        return new IpsException(new IpsStatus("Not possible for archives because they are not modifiable.")); //$NON-NLS-1$
    }

    @Override
    void findIpsSourceFiles(IpsObjectType type, String packageFragment, List<IIpsSrcFile> result)
            {
        if (type == null) {
            return;
        }
        if (storage == null) {
            return;
        }
        Set<QualifiedNameType> qntSet = storage.getQNameTypes();
        for (QualifiedNameType qnt : qntSet) {
            if (!type.equals(qnt.getIpsObjectType())) {
                continue;
            }
            if (packageFragment != null && !qnt.getPackageName().equals(packageFragment)) {
                continue;
            }
            IIpsPackageFragment pack = getIpsPackageFragment(qnt.getPackageName());
            if (pack == null) {
                return;
            }
            IIpsSrcFile file = pack.getIpsSrcFile(qnt.getFileName());
            if (file.exists()) {
                result.add(file);
            }
        }
    }

    @Override
    public boolean isContainedInArchive() {
        if (getIpsStorage() == null) {
            return false;
        }
        return !getIpsStorage().isFolder();
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("IPS Package Fragment Roots that are stored" + //$NON-NLS-1$
                " in an archive cannot be deleted."); //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks if two objects are "equal" without considering the parent. If {@link LibraryIpsSrcFile
     * LibraryIpsSrcFiles} from different projects refer the same jar file and the
     * {@link LibraryIpsPackageFragmentRoot} is the same but the {@link IIpsProject} is different,
     * the default implementation in {@link IIpsElement} may yield misleadingly <code>false</code>.
     * Therefore we need to overwrite the default implementation in {@link IIpsElement}.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LibraryIpsPackageFragmentRoot other = (LibraryIpsPackageFragmentRoot)obj;
        return Objects.equals(storage.getLocation(), other.storage.getLocation());
    }

}
