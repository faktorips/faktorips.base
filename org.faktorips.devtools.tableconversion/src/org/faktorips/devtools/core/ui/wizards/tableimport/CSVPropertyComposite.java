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

package org.faktorips.devtools.core.ui.wizards.tableimport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.tableconversion.csv.CSVTableFormat;

/**
 * Composite for configuring CSV table format options like field delimiters, date formats and the like.
 * 
 * @author Roman Grutza
 */
public class CSVPropertyComposite extends Composite {

    private CSVTableFormat tableFormat;

    
    public CSVPropertyComposite(Composite parent) {
        super(parent, SWT.NONE);
        UIToolkit toolkit = new UIToolkit(null);
        
        // first create a composite to fill the complete space
        Composite root = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 1;
        root.setLayout(layout);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
        root.setLayoutData(layoutData);
        
        // TODO: fix layout, widgets now aligned vertically centered

        // first row: field delimiter controls
        Composite fieldDelimiter = toolkit.createLabelEditColumnComposite(parent);
        layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
        layoutData.verticalAlignment = SWT.TOP;
        fieldDelimiter.setLayoutData(layoutData);
        
        toolkit.createLabel(fieldDelimiter, "Field delimiter");
        
        Text fieldDelimiterText = toolkit.createText(fieldDelimiter);
        fieldDelimiterText.setText("\"");
        fieldDelimiterText.setLayoutData(layoutData);
        
        // second row: date format controls
        Composite dateFormat = toolkit.createLabelEditColumnComposite(parent);
        layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
        layoutData.verticalAlignment = SWT.TOP;
        fieldDelimiter.setLayoutData(layoutData);
        
        toolkit.createLabel(dateFormat, "Date format");
        
        Text dateFormatText = toolkit.createText(dateFormat);
        dateFormatText.setText("dd.mm.yyyy");
        dateFormatText.setLayoutData(layoutData);
    }

    
    public void setTableFormat(CSVTableFormat tableFormat) {
        this.tableFormat = tableFormat;
    }

    
    // TODO: data binding code for: this <-> table format config model
}
