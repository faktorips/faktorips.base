/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.StringUtil;

/**
 * Column label provider for the value of a Builder Set property
 * 
 * @author Roman Grutza
 */
public class BuilderSetPropertyLabelProvider extends ColumnLabelProvider {

    private IIpsArtefactBuilderSetConfigModel model;
    private IIpsProject ipsProject;

    public BuilderSetPropertyLabelProvider(IIpsProject ipsProject,
            IIpsArtefactBuilderSetConfigModel ipsArtefactBuilderSetConfigModel) {

        this.ipsProject = ipsProject;
        model = ipsArtefactBuilderSetConfigModel;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof IIpsBuilderSetPropertyDef propertyDef) {
            String propertyValue = model.getPropertyValue(propertyDef.getName());
            if (propertyValue == null || "".equals(propertyValue)) { //$NON-NLS-1$
                // value not set in .ipsproject file, use disabled value
                propertyValue = propertyDef.getDisableValue(ipsProject);
            } else if (propertyDef.getName().equals("loggingFrameworkConnector")) { //$NON-NLS-1$
                // Special treatment of qualified names:
                // Prevent the table column to be too wide by removing package information from
                // the following type. The full qualified name is shown only when the combo box
                // is opened.
                propertyValue = StringUtil.unqualifiedName(propertyValue);
            }
            return propertyValue;
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public Color getBackground(Object element) {
        Color bgColor = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        if (element instanceof IIpsBuilderSetPropertyDef propertyDef) {
            if (!propertyDef.isAvailable(ipsProject)) {
                bgColor = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
            }
        }
        return bgColor;
    }
}
