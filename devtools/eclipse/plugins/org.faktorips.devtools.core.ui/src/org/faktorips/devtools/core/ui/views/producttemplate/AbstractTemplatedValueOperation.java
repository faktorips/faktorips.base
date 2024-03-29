/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.producttemplate;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.SubMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;

/** Base class for operations that modify templated values. */
public abstract class AbstractTemplatedValueOperation implements ICoreRunnable {

    private Set<IIpsSrcFile> filesToSave = new HashSet<>();

    public AbstractTemplatedValueOperation() {
        super();
    }

    void checkForSave(ITemplatedValue value) {
        IIpsSrcFile ipsSrcFile = value.getIpsSrcFile();
        if (!ipsSrcFile.isDirty()) {
            filesToSave.add(ipsSrcFile);
        }
    }

    void save(SubMonitor monitor) {
        for (IIpsSrcFile ipsSrcFile : filesToSave) {
            try {
                ipsSrcFile.save(monitor.split(1));
            } catch (IpsException e) {
                IpsPlugin.log(e);
            }
        }
    }

}
