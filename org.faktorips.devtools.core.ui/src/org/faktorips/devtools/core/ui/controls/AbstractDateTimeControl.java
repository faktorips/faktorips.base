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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.inputformat.AbstractDateFormat;

/**
 * Control for date/time input providing a text control and a button. The Text control is used with
 * a {@link FormattingTextField} for {@link Locale} specific date/time input. The button opens a
 * widget that lets the user select a specific date and/or time.
 * 
 * @since 3.7
 */
public abstract class AbstractDateTimeControl extends TextButtonControl {

    private AbstractDateFormat<String> dateFormat;

    private GregorianCalendar oldCalendar;

    private Shell dateTimeDialog;

    public AbstractDateTimeControl(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit, "", true, 24, SWT.RIGHT); //$NON-NLS-1$
        setButtonImage(IpsUIPlugin.getImageHandling().getSharedImage("Calendar.png", true)); //$NON-NLS-1$
        dateFormat = createDateFormat();
        addDisposeListener($ -> {
            if (dateTimeDialog != null) {
                dateTimeDialog.dispose();
            }
        });
    }

    protected abstract AbstractDateFormat<String> createDateFormat();

    @Override
    protected void buttonClicked() {
        showCalendarShell();
        getButtonControl().setSelection(true);
    }

    protected abstract void showCalendarShell();

    private void initOldCalendar() {
        oldCalendar = new GregorianCalendar();
        Date date = getDateFormat().parseToDate(getText());
        if (date != null) {
            getOldCalendar().setTime(date);
        }
    }

    protected void initWidgetWithCurrentDate(final DateTime dateWidget) {
        initOldCalendar();
        int year = oldCalendar.get(Calendar.YEAR);
        int month = oldCalendar.get(Calendar.MONTH);
        int day = oldCalendar.get(Calendar.DAY_OF_MONTH);
        dateWidget.setDate(year, month, day);
    }

    protected void initWidgetWithCurrentTime(final DateTime timeWidget) {
        initOldCalendar();
        int hours = oldCalendar.get(Calendar.HOUR_OF_DAY);
        int minutes = oldCalendar.get(Calendar.MINUTE);
        int seconds = oldCalendar.get(Calendar.SECOND);
        timeWidget.setTime(hours, minutes, seconds);
    }

    @Override
    protected Control createSecondControl(UIToolkit toolkit) {
        return toolkit.createToggleButton(this, "");
    }

    protected void closeCalendarShell(Shell calendarShell) {
        if (calendarShell != null && !calendarShell.isDisposed()) {
            calendarShell.setVisible(false);
            calendarShell.dispose();
            getButtonControl().setSelection(false);
        }
    }

    protected GregorianCalendar getOldCalendar() {
        return oldCalendar;
    }

    protected void setOldCalendar(GregorianCalendar oldCalendar) {
        this.oldCalendar = oldCalendar;
    }

    public AbstractDateFormat<String> getDateFormat() {
        return dateFormat;
    }

}
