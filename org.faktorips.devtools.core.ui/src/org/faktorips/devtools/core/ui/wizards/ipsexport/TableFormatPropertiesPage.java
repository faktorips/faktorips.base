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

package org.faktorips.devtools.core.ui.wizards.ipsexport;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.TableFormatConfigurationCompositeFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.tableconversion.ITableFormat;

/**
 * Wizard page to configure a table format's properties like a date format or field/text delimiters.
 * 
 * @author Roman Grutza
 */
public class TableFormatPropertiesPage extends WizardPage {

    private static final String PAGE_NAME = "TableFormatPropertiesPage"; //$NON-NLS-1$
    private Composite pageControl;

    private UIToolkit toolkit = new UIToolkit(null);
    private ITableFormat tableFormat;
    
    
    public TableFormatPropertiesPage(ITableFormat tableFormat) {
        super(PAGE_NAME);
        setTitle(Messages.TableFormatPropertiesPage_title);
        this.tableFormat = tableFormat;
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        this.pageControl = toolkit.createGridComposite(parent, 1, false, false);
        setControl(pageControl);
        
        Group configurationGroup = toolkit.createGroup(pageControl, Messages.TableFormatPropertiesPage_configGroupLabel);
        Object layoutData = configurationGroup.getLayoutData();
        if (layoutData instanceof GridData) {
            ((GridData)layoutData).grabExcessVerticalSpace = true;
        }
        
        try {
            TableFormatConfigurationCompositeFactory factory =
                IpsUIPlugin.getDefault().getTableFormatPropertiesControlFactory(tableFormat);
            if (factory != null) {
                factory.createPropertyComposite(configurationGroup, toolkit);
            }
        } catch (Exception e) {
            IpsPlugin.log(e);
        }
        
        pageControl.layout();
    }
}
