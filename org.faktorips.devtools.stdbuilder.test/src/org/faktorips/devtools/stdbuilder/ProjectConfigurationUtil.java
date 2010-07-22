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

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;

public class ProjectConfigurationUtil {

    private ProjectConfigurationUtil() {
        // Cannot instantiate utility class.
    }

    public static final void setUpUseJava5Enums(IIpsProject ipsProject, boolean useFeature) throws CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        IIpsArtefactBuilderSetConfigModel builderConfig = properties.getBuilderSetConfig();
        String booleanLiteral = useFeature ? "true" : "false";
        builderConfig.setPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_USE_ENUMS, booleanLiteral, null);
        ipsProject.setProperties(properties);
    }

}
