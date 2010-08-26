/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class IpsContainerEntry extends IpsObjectPathEntry implements IIpsContainerEntry {

    public IpsContainerEntry(IpsObjectPath path) {
        super(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return TYPE_CONTAINER;
    }

    @Override
    public IIpsObjectPathEntry[] resolveEntries() throws CoreException {
        List<IpsObjectPathEntry> entries = resolveEntriesInternal();
        return entries.toArray(new IIpsObjectPathEntry[entries.size()]);
    }

    protected abstract List<IpsObjectPathEntry> resolveEntriesInternal() throws CoreException;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(QualifiedNameType qnt) throws CoreException {
        List<IpsObjectPathEntry> entries = resolveEntriesInternal();
        for (IpsObjectPathEntry entry : entries) {
            if (entry.exists(qnt)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IIpsSrcFile findIpsSrcFileInternal(QualifiedNameType nameType, Set<IIpsObjectPathEntry> visitedEntries)
            throws CoreException {

        List<IpsObjectPathEntry> entries = resolveEntriesInternal();
        for (IpsObjectPathEntry entry : entries) {
            IIpsSrcFile file = entry.findIpsSrcFileInternal(nameType, visitedEntries);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void findIpsSrcFilesInternal(IpsObjectType type,
            String packageFragment,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {

        List<IpsObjectPathEntry> entries = resolveEntriesInternal();
        for (IpsObjectPathEntry entry : entries) {
            entry.findIpsSrcFilesInternal(type, packageFragment, result, visitedEntries);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findIpsSrcFilesStartingWithInternal(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {

        List<IpsObjectPathEntry> entries = resolveEntriesInternal();
        for (IpsObjectPathEntry entry : entries) {
            entry.findIpsSrcFilesStartingWithInternal(type, prefix, ignoreCase, result, visitedEntries);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() throws CoreException {
        // TODO should be ok!
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIpsPackageFragmentRootName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getRessourceAsStream(String path) throws CoreException {
        List<IpsObjectPathEntry> entries = resolveEntriesInternal();
        for (IpsObjectPathEntry entry : entries) {
            InputStream stream = entry.getRessourceAsStream(path);
            if (stream != null) {
                return stream;
            }
        }
        return null;
    }

}
