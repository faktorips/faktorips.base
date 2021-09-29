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
import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.inputformat.AbstractDateFormat;
import org.faktorips.devtools.core.ui.inputformat.DateISOStringFormat;

/**
 * Control for date input providing a text control and a button. The Text control is used with a
 * {@link FormattingTextField} for locale specific date input. The button opens a calendar widget
 * that lets the user select a specific date in a calendar.
 * 
 * @author Stefan Widmaier
 * @since 3.2
 */
public class DateControl extends AbstractDateTimeControl {

    private DateTime dateWidget;

    public DateControl(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit);
    }

    @Override
    protected AbstractDateFormat<String> createDateFormat() {
        return DateISOStringFormat.newInstance();
    }

    @Override
    protected void showCalendarShell() {
        final Shell calendarShell = new Shell(getTextControl().getShell(), SWT.NONE);
        calendarShell.setLayout(new FillLayout());

        dateWidget = new DateTime(calendarShell, SWT.CALENDAR | SWT.MEDIUM);
        initWidgetWithCurrentDate(dateWidget);

        dateWidget.addMouseListener(new DateSelectionMouseAdapter(calendarShell));
        dateWidget.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                closeCalendarShell(calendarShell);
            }
        });

        Point absoluteButtonLocation = getButtonControl().toDisplay(new Point(0, 0));
        Point preferredSize = dateWidget.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        calendarShell.setBounds(absoluteButtonLocation.x - preferredSize.x + 24, absoluteButtonLocation.y + 25,
                preferredSize.x,
                preferredSize.y);
        dateWidget.setVisible(true);
        calendarShell.setVisible(true);
    }

    private void setFieldValueToSelectedDate(final DateTime dateWidget) {
        GregorianCalendar calendar = new GregorianCalendar(dateWidget.getYear(), dateWidget.getMonth(),
                dateWidget.getDay());
        setText(getDateFormat().formatDate(calendar.getTime()));
    }

    private final class DateSelectionMouseAdapter extends MouseAdapter {
        private final Shell calendarShell;

        private DateSelectionMouseAdapter(Shell calendarShell) {
            this.calendarShell = calendarShell;
        }

        @Override
        public void mouseUp(MouseEvent e) {
            if (e.count != 1) {
                // do nothing on double click to avoid closing too early
                return;
            }

            GregorianCalendar newCalendar = new GregorianCalendar(dateWidget.getYear(), dateWidget.getMonth(),
                    dateWidget.getDay());
            Calendar oldCalendar = getOldCalendar();
            if ((oldCalendar.get(Calendar.MONTH) != newCalendar.get(Calendar.MONTH) || oldCalendar
                    .get(Calendar.YEAR) != newCalendar.get(Calendar.YEAR))
                    && oldCalendar.get(Calendar.DAY_OF_MONTH) == newCalendar.get(Calendar.DAY_OF_MONTH)) {
                // only month or year changed --> the user did not selected a date
                setOldCalendar(newCalendar);
                return;
            }
            setFieldValueToSelectedDate(dateWidget);
            closeCalendarShell(calendarShell);
        }
    }
}
