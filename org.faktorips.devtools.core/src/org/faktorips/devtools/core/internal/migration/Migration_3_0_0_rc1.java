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

package org.faktorips.devtools.core.internal.migration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Migration to version 3.1.0.ms1.
 * <p>
 * This migration ensures that the .ipsproject files are rewritten. This is necessary because a new
 * XML element <tt>&lt;SupportedLanguages&gt;</tt> has been added.
 * 
 * @author Alexander Weickmann
 */
public class Migration_3_0_0_rc1 extends DefaultMigration {

    public Migration_3_0_0_rc1(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    protected boolean migrate(IFile file) throws CoreException {
        return false;
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        // Nothing to do
    }

    @Override
    public String getDescription() {
        return "For the new Faktor-IPS multi-language support feature a new XML " + //$NON-NLS-1$
                "element called <SupportedLanguages> has been added to the .ipsproject file."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "3.1.0.ms1"; //$NON-NLS-1$
    }

}
