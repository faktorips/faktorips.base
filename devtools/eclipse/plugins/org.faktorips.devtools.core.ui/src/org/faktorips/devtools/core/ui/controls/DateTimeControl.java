/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

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
import org.faktorips.devtools.core.ui.inputformat.AbstractDateFormat;
import org.faktorips.devtools.core.ui.inputformat.DateTimeISOStringFormat;

/**
 * Control for date/time input providing a text control and a button. The Text control is used with
 * a {@link FormattingTextField} for {@link Locale} specific date/time input. The button opens a
 * calendar widget that lets the user select a specific date in a calendar and a time of day.
 * 
 * @since 3.7
 */
public class DateTimeControl extends AbstractDateTimeControl {

    public DateTimeControl(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit);
    }

    @Override
    protected AbstractDateFormat<String> createDateFormat() {
        return DateTimeISOStringFormat.newInstance();
    }

    @Override
    protected void showCalendarShell() {
        final Shell dateTimeDialog = new Shell(getTextControl().getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dateTimeDialog.setLayout(new GridLayout(2, false));

        final DateTime dateWidget = new DateTime(dateTimeDialog, SWT.CALENDAR | SWT.BORDER);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        dateWidget.setLayoutData(gridData);
        initWidgetWithCurrentDate(dateWidget);
        final DateTime timeWidget = new DateTime(dateTimeDialog, SWT.TIME | SWT.SHORT);
        initWidgetWithCurrentTime(timeWidget);

        Button ok = new Button(dateTimeDialog, SWT.PUSH);
        ok.setText(Messages.DateTimeControl_Ok);
        ok.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setFieldValueToSelectedDateAndTime(dateWidget, timeWidget);
                closeCalendarShell(dateTimeDialog);
            }
        });
        dateTimeDialog.setDefaultButton(ok);
        dateTimeDialog.pack();
        dateTimeDialog.open();
        Point absoluteButtonLocation = getButtonControl().toDisplay(new Point(0, 0));
        dateTimeDialog.setLocation(absoluteButtonLocation.x - 170, absoluteButtonLocation.y + 32);
        dateTimeDialog.setVisible(true);
        dateTimeDialog.setFocus();
    }

    private void setFieldValueToSelectedDateAndTime(final DateTime dateWidget, final DateTime timeWidget) {
        GregorianCalendar calendar = new GregorianCalendar(dateWidget.getYear(), dateWidget.getMonth(),
                dateWidget.getDay(), timeWidget.getHours(), timeWidget.getMinutes());
        setText(getDateFormat().formatDate(calendar.getTime()));
    }

}
