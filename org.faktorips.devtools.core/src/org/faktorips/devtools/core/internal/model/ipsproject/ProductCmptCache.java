/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.faktorips.devtools.core.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.MultiMap;

/**
 * A Cache providing a list of all product components in an {@link IpsProject} that are identified
 * by a String key.
 * <p>
 * By default, the cache is updated whenever a product component is added or removed.
 */
public abstract class ProductCmptCache {

    private enum State {
        NEW,
        INITIALIZED
    }

    private final IpsProject ipsProject;
    private final MultiMap<String, IIpsSrcFile> prodCmptIpsSrcFilesMap = new MultiMap<String, IIpsSrcFile>();

    private final AddRemoveUpdater addRemovelistener;

    private State state = State.NEW;

    public ProductCmptCache(IpsProject ipsProject) {
        this.ipsProject = ipsProject;
        addRemovelistener = new AddRemoveUpdater(this);
        ipsProject.getIpsModel().addIpsSrcFilesChangedListener(addRemovelistener);
    }

    protected abstract String getKey(IIpsSrcFile productCmpt);

    protected synchronized void addProductCmpt(IIpsSrcFile productCmpt) {
        String key = getKey(productCmpt);
        if (!prodCmptIpsSrcFilesMap.get(key).contains(productCmpt)) {
            prodCmptIpsSrcFilesMap.put(key, productCmpt);
        }
    }

    /**
     * Removes the product component from the cache for all keys.
     */
    protected synchronized void removeProductCmpt(IIpsSrcFile productCmpt) {
        for (String key : prodCmptIpsSrcFilesMap.keySet()) {
            prodCmptIpsSrcFilesMap.remove(key, productCmpt);
        }
    }

    /**
     * Removes the product component from the cache for only the given key.
     */
    protected synchronized void removeProductCmpt(String key, IIpsSrcFile productCmpt) {
        prodCmptIpsSrcFilesMap.remove(key, productCmpt);
    }

    /**
     * Returns the IPS source files identified by the given key. The cache is (re-)initialized if it
     * has not been initialized yet or a matching file does not exist anymore.
     */
    protected synchronized Collection<IIpsSrcFile> findProductCmptsByKey(String key) {
        checkedInit(key);
        return prodCmptIpsSrcFilesMap.get(key);
    }

    private void checkedInit(String key) {
        if (requiresInit(key)) {
            init();
        }
    }

    private boolean requiresInit(String key) {
        return state != State.INITIALIZED || containsNonexistantFiles(key);
    }

    private boolean containsNonexistantFiles(String key) {
        for (IIpsSrcFile ipsSrcFile : prodCmptIpsSrcFilesMap.get(key)) {
            if (!ipsSrcFile.exists()) {
                return true;
            }
        }
        return false;
    }

    private void init() {
        prodCmptIpsSrcFilesMap.clear();
        List<IIpsSrcFile> allProdCmptIpsSrcFiles = ipsProject.findAllIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        for (IIpsSrcFile ipsSrcFile : allProdCmptIpsSrcFiles) {
            addProductCmpt(ipsSrcFile);
        }
        state = State.INITIALIZED;
    }

    public synchronized void clear() {
        prodCmptIpsSrcFilesMap.clear();
        state = State.NEW;
    }

    public void dispose() {
        ipsProject.getIpsModel().removeIpsSrcFilesChangedListener(addRemovelistener);
    }

    protected IIpsProject getIpsProject() {
        return ipsProject;
    }

    private static final class AddRemoveUpdater implements IIpsSrcFilesChangeListener {

        private ProductCmptCache cache;

        public AddRemoveUpdater(ProductCmptCache cache) {
            this.cache = cache;
        }

        @Override
        public void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event) {
            Set<IIpsSrcFile> changedIpsSrcFiles = event.getChangedIpsSrcFiles();
            for (IIpsSrcFile ipsSrcFile : changedIpsSrcFiles) {
                if (isRelevantIpsSrcFile(ipsSrcFile)) {
                    if (isAdd(getDelta(event, ipsSrcFile))) {
                        cache.addProductCmpt(ipsSrcFile);
                    } else if (isRemove(getDelta(event, ipsSrcFile))) {
                        // since the runtime ID cannot be calculated for a deleted file it has to
                        // be removed this way
                        cache.removeProductCmpt(ipsSrcFile);
                    }
                }
            }
        }

        private boolean isRelevantIpsSrcFile(IIpsSrcFile ipsSrcFile) {
            return isProductCmptSrcFile(ipsSrcFile) && ipsSrcFile.getIpsProject().equals(cache.ipsProject);
        }

        private boolean isProductCmptSrcFile(IIpsSrcFile ipsSrcFile) {
            return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
        }

        private IResourceDelta getDelta(IpsSrcFilesChangedEvent event, IIpsSrcFile ipsSrcFile) {
            return event.getResourceDelta(ipsSrcFile);
        }

        private boolean isAdd(IResourceDelta delta) {
            return (delta.getKind() & IResourceDelta.ADDED) != 0;
        }

        private boolean isRemove(IResourceDelta delta) {
            return (delta.getKind() & IResourceDelta.REMOVED) != 0;
        }
    }
}
