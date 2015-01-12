/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResourceDelta;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.util.MultiMap;

/**
 * A Cache providing a list of all {@link IIpsSrcFile}s of a specific {@link IpsObjectType} in an
 * {@link IpsProject}. The {@link IpsSrcFile}s are identified by their unqualified name. Currently
 * this Cache provides a list only for {@link IProductCmpt}s.
 * 
 */
public class UnqualifiedNameCache {

    private enum State {
        NEW,
        INITIALIZED
    }

    private IpsProject ipsProject;

    private MultiMap<String, IIpsSrcFile> prodCmptIpsSrcFilesMap = new MultiMap<String, IIpsSrcFile>();

    private final CacheUpdater listener;

    /**
     * This field is marked as volatile because it is used to check the initialized state of the map
     * using the double null check, @see {@link #checkedInit(String)} .
     */
    private volatile State state = State.NEW;

    public UnqualifiedNameCache(IpsProject ipsProject) {
        this.ipsProject = ipsProject;
        listener = new CacheUpdater(this);
        ipsProject.getIpsModel().addIpsSrcFilesChangedListener(listener);
    }

    public Collection<IIpsSrcFile> findProductCmptByUnqualifiedName(String unqualifiedName) {
        checkedInit(unqualifiedName);
        return prodCmptIpsSrcFilesMap.get(unqualifiedName);
    }

    private void checkedInit(String unqualifiedName) {
        if (requiresInit(unqualifiedName)) {
            synchronized (this) {
                if (requiresInit(unqualifiedName)) {
                    init();
                    state = State.INITIALIZED;
                }
            }
        }
    }

    private boolean requiresInit(String unqualifiedName) {
        if (state != State.INITIALIZED) {
            return true;
        }
        Collection<IIpsSrcFile> result = prodCmptIpsSrcFilesMap.get(unqualifiedName);
        return containsNonexistentSrcFiles(result);
    }

    private boolean containsNonexistentSrcFiles(Collection<IIpsSrcFile> result) {
        for (IIpsSrcFile ipsSrcFile : result) {
            if (!ipsSrcFile.exists()) {
                return true;
            }
        }
        return false;
    }

    public void init() {
        prodCmptIpsSrcFilesMap.clear();
        List<IIpsSrcFile> allProdCmptIpsSrcFiles = ipsProject.findAllIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        for (IIpsSrcFile ipsSrcFile : allProdCmptIpsSrcFiles) {
            addProductCmpt(ipsSrcFile);
        }
    }

    public void clear() {
        checkedClear();
    }

    private void checkedClear() {
        if (state != State.NEW) {
            synchronized (this) {
                if (state != State.NEW) {
                    prodCmptIpsSrcFilesMap.clear();
                    state = State.NEW;
                }
            }
        }
    }

    void addProductCmpt(IIpsSrcFile productCmpt) {
        prodCmptIpsSrcFilesMap.put(productCmpt.getIpsObjectName(), productCmpt);
    }

    public void dispose() {
        ipsProject.getIpsModel().removeIpsSrcFilesChangedListener(listener);
    }

    private static final class CacheUpdater implements IIpsSrcFilesChangeListener {

        private UnqualifiedNameCache cache;

        public CacheUpdater(UnqualifiedNameCache cache) {
            this.cache = cache;
        }

        @Override
        public void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event) {
            Set<IIpsSrcFile> changedIpsSrcFiles = event.getChangedIpsSrcFiles();
            for (IIpsSrcFile ipsSrcFile : changedIpsSrcFiles) {
                if (isRelevantIpsSrcFileEvent(event, ipsSrcFile)) {
                    cache.addProductCmpt(ipsSrcFile);
                }
            }
        }

        boolean isRelevantIpsSrcFileEvent(IpsSrcFilesChangedEvent event, IIpsSrcFile ipsSrcFile) {
            return isProductCmptSrcFile(ipsSrcFile) && isAddEvent(event, ipsSrcFile)
                    && ipsSrcFile.getIpsProject().equals(cache.ipsProject);
        }

        private boolean isProductCmptSrcFile(IIpsSrcFile ipsSrcFile) {
            return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
        }

        private boolean isAddEvent(IpsSrcFilesChangedEvent event, IIpsSrcFile ipsSrcFile) {
            return (event.getResourceDelta(ipsSrcFile).getKind() & IResourceDelta.ADDED) != 0;
        }

    }

}
