/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.forms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * A section is an area of the user interface 
 */
public abstract class IpsSection extends Composite {
    
    private Section section;
    private boolean isRefreshing = false;

    private int style;
    private int layoutData;
    private UIToolkit toolkit;
    
    public IpsSection(
            Composite parent, 
            int style, 
            int layoutData, 
            UIToolkit toolkit) {
        super(parent, SWT.NONE);
        this.style = style;
        this.layoutData = layoutData;
        this.toolkit = toolkit;
    }
    
    /**
     * Constructs the section's controls. This has to to be called explicitly
     * by subclasses <b>after</b> they have initialized any subclass specific
     * instance variables. The <code>IpsSection</code> does not call this
     * method in it's constructor, because in subclasses in might be neccessary
     * to initialize instance variable first, but the call to the super constructor
     * has to be the first statement in the subclass constructor.  
     */
    public void initControls() {
        toolkit.getFormToolkit().adapt(this);
        setLayoutData(new GridData(layoutData));
        setLayout(toolkit.createNoMarginGridLayout(1, false));
        section = toolkit.getFormToolkit().createSection(this, style);
        section.setLayoutData(new GridData(layoutData));
        Composite client = toolkit.createGridComposite(section, 1, false, false);
        client.setLayoutData(new GridData(layoutData));
        initClientComposite(client, toolkit);
        section.setClient(client);
        toolkit.getFormToolkit().paintBordersFor(client);
    }

    protected abstract void initClientComposite(Composite client, UIToolkit toolkit);
    
    protected Section getSectionControl() {
        return section;
    }
    
    public void setText(String text) {
        section.setText(text);
    }
    
    public void setDescription(String description) {
        section.setDescription(description);
    }
    
    public boolean isRefreshing() {
        return isRefreshing;
    }
    
    public void refresh() {
        isRefreshing = true;
        try {
            performRefresh();
        }
        finally {
            isRefreshing = false;
        }
    }
    
    protected abstract void performRefresh();
}
