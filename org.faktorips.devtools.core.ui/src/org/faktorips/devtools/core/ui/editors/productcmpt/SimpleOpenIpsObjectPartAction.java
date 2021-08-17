/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;

/**
 * Opens an editor for an {@link IIpsObjectPartContainer}.
 */
public class SimpleOpenIpsObjectPartAction<T extends IIpsObjectPartContainer> extends OpenEditorAction {

    private Supplier<T> partSupplier;
    private Function<T, String> captionGenerator;

    /**
     * Creates a {@link SimpleOpenIpsObjectPartAction} that opens the given part and uses the given
     * caption as {@link #getText() text} and {@link #getToolTipText() tooltip text}.
     */
    public SimpleOpenIpsObjectPartAction(final T part, final String caption) {
        this(() -> part, t -> caption);
    }

    /**
     * Creates a {@link SimpleOpenIpsObjectPartAction} that opens the part supplied by the given
     * supplier when {@link #run() run} and uses the caption created from the part by the given
     * function as {@link #getText() text} and {@link #getToolTipText() tooltip text}.
     */
    public SimpleOpenIpsObjectPartAction(Supplier<T> partSupplier, Function<T, String> captionGenerator) {
        super(new SuppliedSelectionProvider<>(partSupplier));
        this.partSupplier = partSupplier;
        this.captionGenerator = captionGenerator;
    }

    @Override
    public String getText() {
        T part = partSupplier.get();
        if (part != null) {
            return captionGenerator.apply(part);
        } else {
            return StringUtils.EMPTY;
        }
    }

    /* used only for test */
    void setPartSupplier(Supplier<T> partSupplier) {
        this.partSupplier = partSupplier;
    }

    @Override
    public String getToolTipText() {
        return getText();
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        T part = partSupplier.get();
        if (part != null) {
            return IpsUIPlugin.getImageHandling().getImageDescriptor(part.getIpsObject());
        } else {
            return null;
        }
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && partSupplier.get() != null;
    }

    private static class SuppliedSelectionProvider<T extends IIpsObjectPartContainer> implements ISelectionProvider {

        private Supplier<T> partSupplier;

        public SuppliedSelectionProvider(Supplier<T> partSupplier) {
            this.partSupplier = partSupplier;
        }

        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
            // nothing to do
        }

        @Override
        public ISelection getSelection() {
            return new StructuredSelection(partSupplier.get());
        }

        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            // nothing to do
        }

        @Override
        public void setSelection(ISelection selection) {
            // nothing to do
        }

    }

}
