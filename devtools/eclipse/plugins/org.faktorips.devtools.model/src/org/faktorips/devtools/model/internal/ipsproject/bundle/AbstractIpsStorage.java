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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsStorage;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;

/**
 * This is an abstract implementation of an {@link IIpsStorage}.
 * <p>
 * The storage is contained by an {@link IIpsProject}. The project can use the ips-files and
 * resources of the storage.
 * 
 * @author dicker
 */
public abstract class AbstractIpsStorage implements IIpsStorage {

    private static final String SEPARATOR = "."; //$NON-NLS-1$
    private final IIpsProject ipsProject;

    /**
     * 
     * @param ipsProject the {@link IIpsProject}, which uses this storage. Must not be
     *            <code>null</code>.
     */
    public AbstractIpsStorage(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject, "The parameter ipsproject cannot be null."); //$NON-NLS-1$
        this.ipsProject = ipsProject;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    @Override
    public boolean containsPackage(String packageName) {
        if (packageName == null) {
            return false;
        }
        if (IpsStringUtils.EMPTY.equals(packageName)) {
            return true;
        }
        String prefix = getPackagePrefix(packageName);
        String[] nonEmptyPackages = getNonEmptyPackages();
        for (String nonEmptyPackageName : nonEmptyPackages) {
            if (nonEmptyPackageName.equals(packageName) || nonEmptyPackageName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String getPackagePrefix(String pack) {
        if (IpsStringUtils.isEmpty(pack)) {
            return IpsStringUtils.EMPTY;
        } else {
            return pack + '.';
        }
    }

    @Override
    public String[] getNonEmptySubpackages(String packageName) {
        if (packageName == null) {
            return new String[0];
        }
        Set<String> result = new LinkedHashSet<>();
        String parentPrefix = getPackagePrefix(packageName);

        for (String nonEmptyPackageName : getNonEmptyPackages()) {

            String nameAfterParent = StringUtils.substringAfter(nonEmptyPackageName, parentPrefix);
            if (IpsStringUtils.EMPTY.equals(nameAfterParent)) {
                continue;
            }
            result.add(parentPrefix + StringUtils.substringBefore(nameAfterParent, SEPARATOR));
        }

        return result.toArray(new String[result.size()]);
    }

}
