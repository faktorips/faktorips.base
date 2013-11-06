/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.inputFormat.AbstractDateFormat;
import org.faktorips.devtools.core.ui.inputFormat.TimeISOStringFormat;

/**
 * Control for time input providing a text control and a button. The Text control is used with a
 * {@link FormattingTextField} for {@link Locale} specific time input. The button opens a clock
 * widget that lets the user select a specific time of day.
 * 
 * @since 3.7
 */
public class TimeControl extends AbstractDateTimeControl {

    public TimeControl(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit);
    }

    @Override
    protected AbstractDateFormat<String> createDateFormat() {
        return TimeISOStringFormat.newInstance();
    }

    @Override
    protected void showCalendarShell() {
        final Shell timeDialog = new Shell(getTextControl().getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        timeDialog.setLayout(new GridLayout(2, false));

        final DateTime timeWidget = new DateTime(timeDialog, SWT.TIME | SWT.SHORT);
        initWidgetWithCurrentTime(timeWidget);

        Button ok = new Button(timeDialog, SWT.PUSH);
        ok.setText(Messages.DateTimeControl_Ok);
        ok.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setFieldValueToSelectedDateAndTime(timeWidget);
                closeCalendarShell(timeDialog);
            }
        });
        timeDialog.setDefaultButton(ok);
        timeDialog.pack();
        timeDialog.open();
        Point absoluteButtonLocation = getButtonControl().toDisplay(new Point(0, 0));
        timeDialog.setLocation(absoluteButtonLocation.x - 100, absoluteButtonLocation.y + 32);
        timeDialog.setVisible(true);
        timeDialog.setFocus();
    }

    private void setFieldValueToSelectedDateAndTime(final DateTime timeWidget) {
        GregorianCalendar calendar = new GregorianCalendar(1970, Calendar.JANUARY, 1, timeWidget.getHours(),
                timeWidget.getMinutes());
        setText(getDateFormat().formatDate(calendar.getTime()));
    }

}
