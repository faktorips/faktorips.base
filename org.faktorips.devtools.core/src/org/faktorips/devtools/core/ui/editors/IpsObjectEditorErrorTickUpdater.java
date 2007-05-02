/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;

/**
 * The <code>IpsObjectEditorErrorTickUpdater</code> will register as a IIpsProblemChangedListener
 * to listen on ips problem changes of the editor's input. It updates the title images when ips marker
 * of ips objects changed (e.g. warnings or errors).
 * 
 * @author Joerg Ortmann
 */
public class IpsObjectEditorErrorTickUpdater implements IIpsProblemChangedListener{

    private IpsObjectEditor ipsObjectEditor;
    private IpsProblemsLabelDecorator decorator;

    public IpsObjectEditorErrorTickUpdater(IpsObjectEditor ipsObjectEditor) {
        this.ipsObjectEditor = ipsObjectEditor;
        decorator = new IpsProblemsLabelDecorator();
        IpsPlugin.getDefault().getIpsProblemMarkerManager().addListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public void problemsChanged(IResource[] changedResources) {
        IResource correspondingResource = ipsObjectEditor.getIpsSrcFile().getCorrespondingResource();
        if (correspondingResource != null) {
            for (int i = 0; i < changedResources.length; i++) {
                if (changedResources[i].equals(correspondingResource)) {
                    updateEditorImage(changedResources[i]);
                }
            }
        }
    }

    /**
     * Returns the image of the ips object inside the ips object editor which is optional decorated
     * with an ips marker image if a marker exists.
     */
    Image getDecoratedImage() {
        Image titleImage = ipsObjectEditor.getIpsSrcFile().getIpsObjectType().getImage(
                ipsObjectEditor.isDataChangeable().booleanValue());
        return decorator.decorateImage(titleImage, ipsObjectEditor.getIpsObject());
    }
    
    private void updateEditorImage(IResource changedResources) {
        Image image = getDecoratedImage();
        postImageChange(image);
    }

    private void postImageChange(final Image newImage) {
        Shell shell = ipsObjectEditor.getEditorSite().getShell();
        if (shell != null && !shell.isDisposed()) {
            shell.getDisplay().syncExec(new Runnable() {
                public void run() {
                    ipsObjectEditor.updatedTitleImage(newImage);
                }
            });
        }
    }

    public void dispose() {
        decorator.dispose();
        
        IpsPlugin.getDefault().getIpsProblemMarkerManager().removeListener(this);
    }
}
