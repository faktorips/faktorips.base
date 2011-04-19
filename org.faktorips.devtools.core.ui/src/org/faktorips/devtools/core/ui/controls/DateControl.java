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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
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

    private DateISOStringFormat dateFormat;

    private Shell calendarShell;

    private DateTime dateWidget;

    private GregorianCalendar oldCalendar;

    public DateControl(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit, "", true, 24, SWT.RIGHT); //$NON-NLS-1$
        setButtonImage(IpsUIPlugin.getImageHandling().getSharedImage("Calendar.png", true)); //$NON-NLS-1$
        dateFormat = new DateISOStringFormat();
    }

    @Override
    protected void buttonClicked() {
        showClendarShell();
        getButtonControl().setSelection(true);
    }

    private void showClendarShell() {
        calendarShell = new Shell(getTextControl().getShell(), SWT.NONE);
        calendarShell.setLayout(new FillLayout());

        dateWidget = new DateTime(calendarShell, SWT.CALENDAR | SWT.MEDIUM);
        initWidgetWithCurretDate(dateWidget);

        dateWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                GregorianCalendar newCalendar = new GregorianCalendar(dateWidget.getYear(), dateWidget.getMonth(),
                        dateWidget.getDay());
                if ((oldCalendar.get(Calendar.MONTH) != newCalendar.get(Calendar.MONTH) || oldCalendar
                        .get(Calendar.YEAR) != newCalendar.get(Calendar.YEAR))
                        && oldCalendar.get(Calendar.DAY_OF_MONTH) == newCalendar.get(Calendar.DAY_OF_MONTH)) {
                    // only month or year changed --> the user did not selected a date
                    oldCalendar = newCalendar;
                    return;
                }
                setFieldValueToSelectedDate(dateWidget);
                closeCalendarShell(calendarShell);
            }
        });
        dateWidget.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                closeCalendarShell(calendarShell);
            }
        });
        Point absoluteButtonLocation = getButtonControl().toDisplay(new Point(0, 0));
        calendarShell.setBounds(absoluteButtonLocation.x - 155, absoluteButtonLocation.y + 25, 180, 170);
        dateWidget.setVisible(true);
        calendarShell.setVisible(true);
        calendarShell.setFocus();
    }

    protected void initWidgetWithCurretDate(final DateTime dateWidget) {
        oldCalendar = new GregorianCalendar();
        Date date = dateFormat.parseToDate(getText());
        if (date != null) {
            oldCalendar.setTime(date);
        }
        int year = oldCalendar.get(Calendar.YEAR);
        int month = oldCalendar.get(Calendar.MONTH);
        int day = oldCalendar.get(Calendar.DAY_OF_MONTH);
        dateWidget.setDate(year, month, day);
    }

    @Override
    protected Control createSecondControl(UIToolkit toolkit) {
        Button toggleButton = toolkit.createToggleButton(this, ""); //$NON-NLS-1$
        return toggleButton;
    }

    private void closeCalendarShell(Shell calendarShell) {
        if (calendarShell != null && !calendarShell.isDisposed()) {
            calendarShell.setVisible(false);
            calendarShell.dispose();
            getButtonControl().setSelection(false);
        }
    }

    protected void setFieldValueToSelectedDate(final DateTime dateWidget) {
        GregorianCalendar calendar = new GregorianCalendar(dateWidget.getYear(), dateWidget.getMonth(),
                dateWidget.getDay());
        setText(dateFormat.formatDate(calendar.getTime()));
    }

    @Override
    public void dispose() {
        if (calendarShell != null) {
            calendarShell.dispose();
        }
        super.dispose();
    }
}
