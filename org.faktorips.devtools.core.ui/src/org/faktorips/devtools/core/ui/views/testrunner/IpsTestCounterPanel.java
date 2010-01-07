/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.testrunner;

import java.text.MessageFormat;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.OverlayIcons;

/**
 * Counter panel.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestCounterPanel extends Composite {
    protected Text fNumberOfErrors;
    protected Text fNumberOfFailures;
    protected Text fNumberOfRuns;
    protected int fTotal;

    private ResourceManager resourceManager;

    public IpsTestCounterPanel(Composite parent) {
        super(parent, SWT.WRAP);
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 9;
        gridLayout.makeColumnsEqualWidth = false;
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        fNumberOfRuns = createLabel(Messages.IpsTestCounterPanel_Runs_Label, null, " 0/0  "); //$NON-NLS-1$ 
        fNumberOfErrors = createLabel(Messages.IpsTestCounterPanel_Errors_Label, OverlayIcons.ERROR_OVR_DESC, " 0 "); //$NON-NLS-1$ 
        fNumberOfFailures = createLabel(Messages.IpsTestCounterPanel_Failures_Label, OverlayIcons.FAILURE_OVR_DESC,
                " 0 "); //$NON-NLS-1$ 

        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                disposeResources();
            }
        });
    }

    private void disposeResources() {
        resourceManager.dispose();
    }

    private Text createLabel(String name, ImageDescriptor imageDescriptor, String init) {
        Label label = new Label(this, SWT.NONE);
        if (imageDescriptor != null) {
            // image.setBackground(label.getBackground());
            label.setImage((Image)resourceManager.get(imageDescriptor));
        }
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        label = new Label(this, SWT.NONE);
        label.setText(name);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        Text value = new Text(this, SWT.READ_ONLY);
        value.setText(init);
        value.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));
        return value;
    }

    public void reset() {
        setErrorValue(0);
        setFailureValue(0);
        setRunValue(0);
        fTotal = 0;
    }

    public void setTotal(int value) {
        fTotal = value;
        redraw();
    }

    public int getTotal() {
        return fTotal;
    }

    public void setRunValue(int value) {
        String runString = MessageFormat.format("{0}/{1}", Integer.toString(value), Integer.toString(fTotal)); //$NON-NLS-1$
        fNumberOfRuns.setText(runString);
        fNumberOfRuns.redraw();
        redraw();
    }

    public void setErrorValue(int value) {
        fNumberOfErrors.setText(Integer.toString(value));
        redraw();
    }

    public void setFailureValue(int value) {
        fNumberOfFailures.setText(Integer.toString(value));
        redraw();
    }
}
