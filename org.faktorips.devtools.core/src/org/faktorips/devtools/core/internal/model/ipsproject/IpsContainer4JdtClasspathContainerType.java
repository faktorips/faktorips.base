/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Container type for JDT classpath containers.
 * 
 * @author Jan Ortmann
 */
public class IpsContainer4JdtClasspathContainerType implements IIpsObjectPathContainerType {

    /**
     * The type's ID as specified in the plugin.xml.
     */
    public final static String ID = "JDTClasspathContainer"; //$NON-NLS-1$

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public IIpsObjectPathContainer newContainer(IIpsProject ipsProject, String optionalPath) {
        return new IpsContainer4JdtClasspathContainer(this, ipsProject, optionalPath);
    }

}
