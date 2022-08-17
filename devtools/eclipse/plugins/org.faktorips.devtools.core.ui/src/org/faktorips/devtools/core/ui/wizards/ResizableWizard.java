/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public abstract class ResizableWizard extends Wizard {

    protected static final String BOUNDS_HEIGHT_KEY = "width"; //$NON-NLS-1$
    protected static final String BOUNDS_WIDTH_KEY = "height"; //$NON-NLS-1$

    private final int defaultWidth;
    private final int defaultHight;

    private final String dialogId;

    protected ResizableWizard(String dialogId) {
        this(dialogId, 300, 400);
    }

    public ResizableWizard(String dialogId, IDialogSettings settings) {
        this(dialogId, settings, 300, 400);
    }

    protected ResizableWizard(String dialogId, int defaultWidth, int defaultHeight) {
        this(dialogId, IpsUIPlugin.getDefault().getDialogSettings(), defaultWidth, defaultHeight);
    }

    protected ResizableWizard(String dialogId, IDialogSettings settings, int defaultWidth, int defaultHeight) {
        this.defaultWidth = defaultWidth;
        defaultHight = defaultHeight;
        this.dialogId = dialogId;
        setDialogSettings(settings);
    }

    protected String getDialogId() {
        return dialogId;
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
        pageContainer.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                saveSize();
            }

        });
    }

    public void saveSize() {
        Control control = getContainer().getCurrentPage().getControl();
        if (!control.isDisposed()) {
            final Rectangle bounds = control.getParent().getClientArea();
            final IDialogSettings settings = getDialogSettings();
            if (settings == null) {
                return;
            }

            IDialogSettings section = settings.getSection(getDialogId());
            if (section == null) {
                section = settings.addNewSection(getDialogId());
            }

            section.put(BOUNDS_WIDTH_KEY, bounds.width);
            section.put(BOUNDS_HEIGHT_KEY, bounds.height);
        }
    }

    public Point loadSize() {
        final Point size = new Point(defaultWidth, defaultHight);

        final IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return size;
        }

        final IDialogSettings section = settings.getSection(getDialogId());
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

}
