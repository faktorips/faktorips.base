/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.jdtcontainer;

import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Container type for JDT classpath containers.
 * 
 * @author Jan Ortmann
 */
public class IpsContainer4JdtClasspathContainerType implements IIpsObjectPathContainerType {

    /**
     * The type's ID as specified in the plugin.xml.
     */
    public static final String ID = "JDTClasspathContainer"; //$NON-NLS-1$

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public IIpsObjectPathContainer newContainer(IIpsProject ipsProject, String optionalPath) {
        return new IpsContainer4JdtClasspathContainer(optionalPath, ipsProject);
    }

}
