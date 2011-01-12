/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.AbstractDateFormat;
import org.faktorips.devtools.core.ui.controller.fields.DateISOStringFormat;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;

/**
 * Control for date input providing a text control and a button. The Text control is used with a
 * {@link FormattingTextField} for locale specific date input. The button opens a calendar widget
 * that lets the user select a specific date in a calendar.
 * 
 * @author Stefan Widmaier
 * @since 3.2
 */
public class DateControl extends TextButtonControl {

    private AbstractDateFormat dateFormat;

    public DateControl(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit, "", true, 25); //$NON-NLS-1$
        setButtonImage(IpsUIPlugin.getImageHandling().getSharedImage("Calendar.png", true)); //$NON-NLS-1$
        if (toolkit.getFormToolkit() != null) {
            // Forms: Rahmen fuer Text-Control zeichnen lassen
            toolkit.getFormToolkit().paintBordersFor(this);
        }
        dateFormat = new DateISOStringFormat();
    }

    @Override
    protected void buttonClicked() {
        showClendarShell();
    }

    private void showClendarShell() {
        final Shell calendarShell = new Shell(getTextControl().getShell(), SWT.ON_TOP | SWT.NO_TRIM | SWT.NO_FOCUS);
        calendarShell.setLayout(new FillLayout());
        new MainShellListener(getTextControl().getShell(), calendarShell);

        final DateTime dateWidget = new DateTime(calendarShell, SWT.CALENDAR | SWT.MEDIUM);
        dateWidget.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // as of now only Doubleclick should apply the selected date to the textControl
                // setFieldValueToSelectedDate(dateWidget);
            }
        });
        dateWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                if (e.y > 20) {
                    /*
                     * Ignore double-clicks on arrow-buttons (for changing month and year) in the
                     * upper part of DateTime, as they would close the widget.
                     */
                    setFieldValueToSelectedDate(dateWidget);
                    closeCalendarShell(calendarShell);
                }
            }
        });
        dateWidget.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                closeCalendarShell(calendarShell);
            }
        });
        Point absoluteButtonLocation = getButtonControl().toDisplay(new Point(0, 0));
        calendarShell.setBounds(absoluteButtonLocation.x - 200, absoluteButtonLocation.y + 25, 225, 190);
        dateWidget.setSize(225, 190);
        dateWidget.setVisible(true);
        calendarShell.setVisible(true);
        calendarShell.setFocus();

        initWidgetWithCurretDate(dateWidget);
    }

    protected void initWidgetWithCurretDate(final DateTime dateWidget) {
        GregorianCalendar calendar = new GregorianCalendar();
        Date date = dateFormat.parseToDate(getText());
        if (date != null) {
            calendar.setTime(date);
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        dateWidget.setDate(year, month, day);
    }

    private void closeCalendarShell(Shell calendarShell) {
        if (calendarShell != null && !calendarShell.isDisposed()) {
            calendarShell.setVisible(false);
            calendarShell.dispose();
        }
    }

    protected void setFieldValueToSelectedDate(final DateTime dateWidget) {
        GregorianCalendar calendar = new GregorianCalendar(dateWidget.getYear(), dateWidget.getMonth(),
                dateWidget.getDay());
        setText(dateFormat.formatDate(calendar.getTime()));
    }

    private class MainShellListener implements ShellListener {
        private Shell shellToListenTo;
        private Shell calendarShell;

        public MainShellListener(Shell shell, Shell calShell) {
            shellToListenTo = shell;
            calendarShell = calShell;
            shellToListenTo.addShellListener(this);
        }

        @Override
        public void shellIconified(ShellEvent e) {
            unregisterIfShellCalendarWasDisposedOf();
            closeCalendarShell(calendarShell);
        }

        @Override
        public void shellDeiconified(ShellEvent e) {
            // nothing to do
        }

        @Override
        public void shellDeactivated(ShellEvent e) {
            // nothing to do
        }

        @Override
        public void shellClosed(ShellEvent e) {
            unregisterIfShellCalendarWasDisposedOf();
            closeCalendarShell(calendarShell);
        }

        @Override
        public void shellActivated(ShellEvent e) {
            // nothing to do
        }

        protected void unregisterIfShellCalendarWasDisposedOf() {
            if (calendarShell.isDisposed() && !shellToListenTo.isDisposed()) {
                shellToListenTo.removeShellListener(this);
            }
        }

    }
}
