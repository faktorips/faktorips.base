/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class ResizableWizard extends Wizard {

    private final int DEFAULT_WIDTH;
    private final int DEFAULT_HEIGHT;

    protected static final String BOUNDS_HEIGHT_KEY = "width"; //$NON-NLS-1$
    protected static final String BOUNDS_WIDTH_KEY = "height"; //$NON-NLS-1$

    final String fSectionName;

    public ResizableWizard(String sectionName, IDialogSettings settings) {
        this(sectionName, settings, 300, 400);
    }

    protected ResizableWizard(String sectionName, IDialogSettings settings, int defaultWidth, int defaultHeight) {
        DEFAULT_WIDTH = defaultWidth;
        DEFAULT_HEIGHT = defaultHeight;
        fSectionName = sectionName;
        setDialogSettings(settings);
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);

        GridData layoutData = (GridData)pageContainer.getLayoutData();
        Point size = loadSize();

        int width = Math.max(size.x, layoutData.widthHint);
        int height = Math.max(size.y, layoutData.heightHint);
        layoutData.widthHint = Math.max(width, layoutData.minimumWidth);
        layoutData.heightHint = Math.max(height, layoutData.minimumHeight);
    }

    public void saveSize() {
        final Rectangle bounds = getContainer().getCurrentPage().getControl().getParent().getClientArea();
        final IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }

        IDialogSettings section = settings.getSection(fSectionName);
        if (section == null) {
            section = settings.addNewSection(fSectionName);
        }

        section.put(BOUNDS_WIDTH_KEY, bounds.width);
        section.put(BOUNDS_HEIGHT_KEY, bounds.height);
    }

    public Point loadSize() {
        final Point size = new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        final IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return size;
        }

        final IDialogSettings section = settings.getSection(fSectionName);
        if (section == null) {
            return size;
        }

        try {
            size.x = section.getInt(BOUNDS_WIDTH_KEY);
            size.y = section.getInt(BOUNDS_HEIGHT_KEY);
        } catch (NumberFormatException e) {
            // ignore format exceptions
        }
        return size;
    }

    @Override
    public boolean performFinish() {
        saveSize();
        return true;
    }
}
