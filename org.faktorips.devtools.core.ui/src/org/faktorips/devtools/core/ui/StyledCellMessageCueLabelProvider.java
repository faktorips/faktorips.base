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

package org.faktorips.devtools.core.ui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * A StyledCellLabelProvider using the {@link MessageCueLabelProvider}. The method
 * {@link #update(ViewerCell)} updates the cell by calling the getImage and getText methods from a
 * {@link MessageCueLabelProvider}. This label provider gets an {@link InternalBaseLabelProvider}.
 * When updating a cell, the label provider first updates the {@link InternalBaseLabelProvider} then
 * calling the {@link MessageCueLabelProvider}. The {@link MessageCueLabelProvider} delegates to the
 * {@link InternalBaseLabelProvider} and decorates the image and text
 * <p>
 * Note: This label provider is not thread safe!
 * 
 * @author dirmeier
 */
public class StyledCellMessageCueLabelProvider extends StyledCellLabelProvider {

    private MessageCueLabelProvider messageLabelProvider;
    private InternalBaseLabelProvider internalBaseLabelProvider;

    /**
     * Creates a new <code>MessageCueLabelProvider</code>.
     * 
     * @param baseProvider The label provider to decorate the image for.
     * 
     * @throws NullPointerException If baseProvider or ipsProject is <code>null</code>.
     */
    public StyledCellMessageCueLabelProvider(StyledCellLabelProvider baseProvider, IIpsProject ipsProject) {
        ArgumentCheck.notNull(baseProvider, this);
        ArgumentCheck.notNull(ipsProject, this);

        internalBaseLabelProvider = new InternalBaseLabelProvider(baseProvider);
        messageLabelProvider = new MessageCueLabelProvider(internalBaseLabelProvider, ipsProject);
        baseProvider.addListener(new ILabelProviderListener() {
            @Override
            public void labelProviderChanged(LabelProviderChangedEvent event) {
                propagateEvent();
            }
        });
    }

    /**
     * Propagates a {@link LabelProviderChangedEvent} from the wrapped <code>baseProvider</code> to
     * all registered listeners of this label provider.
     */
    private void propagateEvent() {
        fireLabelProviderChanged(new LabelProviderChangedEvent(this));
    }

    @Override
    public void update(ViewerCell cell) {
        internalBaseLabelProvider.setCell(cell);
        Object o = cell.getElement();
        cell.setImage(messageLabelProvider.getImage(o));
        cell.setText(messageLabelProvider.getText(o));
    }

    private static class InternalBaseLabelProvider extends LabelProvider {

        private Image actualImage;

        private String actualText;

        private StyledCellLabelProvider cellLabelProvider;

        public InternalBaseLabelProvider(StyledCellLabelProvider cellLabelProvider) {
            this.cellLabelProvider = cellLabelProvider;
        }

        @Override
        public Image getImage(Object element) {
            return actualImage;
        }

        @Override
        public String getText(Object element) {
            return actualText;
        }

        public void setCell(ViewerCell cell) {
            cellLabelProvider.update(cell);
            actualImage = cell.getImage();
            actualText = cell.getText();
        }

    }

}
