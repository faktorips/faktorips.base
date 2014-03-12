/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.IVersion;
import org.faktorips.devtools.core.model.IVersionProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;

/**
 * A composite that allows defining since which version of the model a new
 * {@link IpsObjectPartContainer} is available.
 * 
 */
public final class VersionsComposite {

    private final UIToolkit toolkit;

    private Composite parent;

    Text text;

    private BindingContext bindingContext;

    private IVersionControlledElement part;

    public VersionsComposite(Composite parent, IVersionControlledElement part, UIToolkit toolkit) {
        this.parent = parent;
        this.toolkit = toolkit;
        this.part = part;
        bindingContext = new BindingContext();

        parent.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (bindingContext != null) {
                    bindingContext.dispose();
                }
            }
        });
        createLayout();
        bind();
    }

    private void createLayout() {
        Composite composite = toolkit.createGridComposite(parent, 2, true, true);
        Composite sinceVersionComposite = toolkit.createGridComposite(composite, 2, false, true);
        Label label = toolkit.createFormLabel(sinceVersionComposite, Messages.IpsPartEditDialog_versionAvailableSince);
        GridData grid = new GridData(SWT.HORIZONTAL, SWT.VERTICAL, true, false);
        label.setLayoutData(grid);

        text = toolkit.createText(sinceVersionComposite);

        text.setToolTipText(Messages.IpsPartEditDialog_versionTooltip);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.paintBordersForComposite(sinceVersionComposite);
    }

    private void bind() {
        bindingContext.bindContent(text, new VersionPMO(part), VersionPMO.PROPERTY_SINCE_VERSION);
        bindingContext.updateUI();
    }

    public static class VersionPMO extends IpsObjectPartPmo {

        private static final String PROPERTY_SINCE_VERSION = "sinceVersion"; //$NON-NLS-1$

        public VersionPMO(IVersionControlledElement versionElement) {
            super(versionElement);
        }

        @Override
        public IVersionControlledElement getIpsObjectPartContainer() {
            return (IVersionControlledElement)super.getIpsObjectPartContainer();
        }

        @Override
        public void setIpsObjectPartContainer(IIpsObjectPartContainer part) {
            throw new IllegalArgumentException("It is not allowed to change the part in this pmo!"); //$NON-NLS-1$
        }

        public void setSinceVersion(String newVersion) {
            IVersion<?> version = parseVersion(newVersion);
            getIpsObjectPartContainer().setSinceVersion(version);
        }

        private IVersion<?> parseVersion(String versionString) {
            if (!StringUtils.isEmpty(versionString)) {
                return null;
            } else {
                IVersionProvider<?> versionProvider = getIpsObjectPartContainer().getIpsProject().getVersionProvider();
                return versionProvider.getVersion(versionString);
            }
        }

        public String getSinceVersion() {
            IVersion<?> sinceVersion = getIpsObjectPartContainer().getSinceVersion();
            if (sinceVersion != null) {
                return sinceVersion.asString();
            }
            return StringUtils.EMPTY;
        }
    }
}
