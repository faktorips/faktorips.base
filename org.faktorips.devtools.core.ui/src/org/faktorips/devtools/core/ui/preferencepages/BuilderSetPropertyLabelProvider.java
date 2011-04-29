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

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
        this.model = ipsArtefactBuilderSetConfigModel;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof IIpsBuilderSetPropertyDef) {
            IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;
            String propertyValue = model.getPropertyValue(propertyDef.getName());
            if (propertyValue == null || "".equals(propertyValue)) { //$NON-NLS-1$
                // value not set in .ipsproject file, use default
                propertyValue = propertyDef.getDefaultValue(ipsProject);
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
        if (element instanceof IIpsBuilderSetPropertyDef) {
            IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;
            if (!propertyDef.isAvailable(ipsProject)) {
                bgColor = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
            }
        }
        return bgColor;
    }
}