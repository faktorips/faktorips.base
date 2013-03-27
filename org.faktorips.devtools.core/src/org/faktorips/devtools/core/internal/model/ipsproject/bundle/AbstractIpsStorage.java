/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsproject.bundle;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsStorage;
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
    public boolean containsPackage(String packageName) throws CoreException {
        if (packageName == null) {
            return false;
        }
        if (StringUtils.EMPTY.equals(packageName)) {
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
        if (StringUtils.isEmpty(pack)) {
            return StringUtils.EMPTY;
        } else {
            return pack + '.';
        }
    }

    @Override
    public String[] getNonEmptySubpackages(String packageName) throws CoreException {
        if (packageName == null) {
            return new String[0];
        }
        Set<String> result = new LinkedHashSet<String>();
        String parentPrefix = getPackagePrefix(packageName);

        for (String nonEmptyPackageName : getNonEmptyPackages()) {

            String nameAfterParent = StringUtils.substringAfter(nonEmptyPackageName, parentPrefix);
            if (StringUtils.EMPTY.equals(nameAfterParent)) {
                continue;
            }
            result.add(parentPrefix + StringUtils.substringBefore(nameAfterParent, SEPARATOR));
        }

        return result.toArray(new String[result.size()]);
    }

}