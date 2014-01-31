/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;

public class ProjectConfigurationUtil {

    private ProjectConfigurationUtil() {
        // Cannot instantiate utility class
    }

    public static final void setUpUseJava5Enums(IIpsProject ipsProject, boolean useFeature) throws CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        IIpsArtefactBuilderSetConfigModel builderConfig = properties.getBuilderSetConfig();
        String booleanLiteral = useFeature ? "true" : "false";
        builderConfig.setPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_USE_ENUMS, booleanLiteral, null);
        ipsProject.setProperties(properties);
    }

}
