/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;

/**
 * Content provider for the IPS object path
 * 
 * @author Roman Grutza
 */
public class IpsObjectPathContentProvider implements ITreeContentProvider {

    private Class<? extends IIpsObjectPathEntry> includedClasses;

    /**
     * Only classes contained in the given list are returned or null.
     */
    public void setIncludedClasses(Class<? extends IIpsObjectPathEntry> includedClasses) {
        this.includedClasses = includedClasses;
    }

    public void removeClassFilter() {
        includedClasses = null;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        Object[] result = null;
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)parentElement;
        IIpsObjectPath objectPath = entry.getIpsObjectPath();

        // handling attributes of IIpsSrcFolderEntries
        if (parentElement instanceof IIpsSrcFolderEntry) {
            ArrayList<IIpsObjectPathEntryAttribute> attributes = new ArrayList<>();

            // tocPath is always configurable
            IIpsObjectPathEntryAttribute attribute = newTocPath(entry);
            attributes.add(attribute);

            if (objectPath.isOutputDefinedPerSrcFolder()) {
                attribute = newOutputFolderForMergableJavaFiles(entry);
                attributes.add(attribute);

                attribute = newOutputFolderForDerivedJavaFiles(entry);
                attributes.add(attribute);

                attribute = newBasePackageNameForMergableJavaFiles(entry);
                attributes.add(attribute);

                attribute = newBasePackageNameForDerivedJavaFiles(entry);
                attributes.add(attribute);
            }

            result = attributes.toArray();
        }

        return result;
    }

    private boolean passesFilter(IIpsObjectPathEntry o) {
        if (includedClasses == null) {
            return true;
        } else {
            return includedClasses.isAssignableFrom(o.getClass());
        }
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IIpsObjectPathEntry) {
            return ((IIpsObjectPathEntry)element).getIpsObjectPath();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IIpsObjectPathEntry) {
            return true;
        }
        return false;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        Object[] returnedElements = null;
        if (inputElement instanceof IIpsObjectPath) {
            // must preserve the order of elements even if some types of entries are filtered
            IIpsObjectPath ipsObjectPath = (IIpsObjectPath)inputElement;
            IIpsObjectPathEntry[] entries = ipsObjectPath.getEntries();

            if (includedClasses == null) {
                returnedElements = entries;
            } else {
                // do filtering
                List<IIpsObjectPathEntry> passedEntries = new ArrayList<>();
                for (IIpsObjectPathEntry entrie : entries) {
                    if (passesFilter(entrie)) {
                        passedEntries.add(entrie);
                    }
                }
                returnedElements = passedEntries.toArray();
            }
        }
        return returnedElements;
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to do
    }

    private IIpsObjectPathEntryAttribute newTocPath(IIpsSrcFolderEntry entry) {
        IIpsObjectPathEntryAttribute attribute = null;
        if (entry.getBasePackageRelativeTocPath() != null) {
            attribute = new IpsObjectPathEntryAttribute(IIpsObjectPathEntryAttribute.SPECIFIC_TOC_PATH,
                    entry.getBasePackageRelativeTocPath());
        }
        return attribute;
    }

    private IIpsObjectPathEntryAttribute newOutputFolderForMergableJavaFiles(IIpsSrcFolderEntry entry) {
        return new IpsObjectPathEntryAttribute(
                IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES,
                entry.getSpecificOutputFolderForMergableJavaFiles());
    }

    private IIpsObjectPathEntryAttribute newOutputFolderForDerivedJavaFiles(IIpsSrcFolderEntry entry) {
        return new IpsObjectPathEntryAttribute(
                IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES,
                entry.getSpecificOutputFolderForDerivedJavaFiles());
    }

    private IIpsObjectPathEntryAttribute newBasePackageNameForDerivedJavaFiles(IIpsSrcFolderEntry entry) {
        IIpsObjectPathEntryAttribute attribute;
        if (entry.getSpecificBasePackageNameForDerivedJavaClasses() == null
                || entry.getSpecificBasePackageNameForDerivedJavaClasses().length() == 0) {
            attribute = new IpsObjectPathEntryAttribute(IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED,
                    entry.getIpsObjectPath().getBasePackageNameForDerivedJavaClasses());
        } else {
            attribute = new IpsObjectPathEntryAttribute(IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED,
                    entry.getSpecificBasePackageNameForDerivedJavaClasses());
        }
        return attribute;
    }

    private IIpsObjectPathEntryAttribute newBasePackageNameForMergableJavaFiles(IIpsSrcFolderEntry entry) {
        IIpsObjectPathEntryAttribute attribute;
        if (entry.getSpecificBasePackageNameForMergableJavaClasses() == null
                || entry.getSpecificBasePackageNameForMergableJavaClasses().length() == 0) {
            attribute = new IpsObjectPathEntryAttribute(IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_MERGABLE,
                    entry.getIpsObjectPath().getBasePackageNameForMergableJavaClasses());
        } else {
            attribute = new IpsObjectPathEntryAttribute(IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_MERGABLE,
                    entry.getSpecificBasePackageNameForMergableJavaClasses());
        }
        return attribute;
    }
}
