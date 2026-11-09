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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateControlTest {

    private Shell parentShell;

    private DateControl dateControl;

    private Shell calendarShell;

    private DateTime calendarWidget;

    @BeforeEach
    public void setUp() {
        parentShell = new Shell();
        dateControl = new DateControl(parentShell, new UIToolkit(null));
    }

    @AfterEach
    public void tearDown() {
        parentShell.dispose();
    }

    @Test
    public void testMouseUp_DateIsAppliedAfterMonthChangeByMouseWheel() {
        openCalendarWithDate(2026, Calendar.OCTOBER, 5);
        scrollWheelTo(2026, Calendar.NOVEMBER, 1);

        moveToDateAndReleaseMouse(2026, Calendar.NOVEMBER, 12);

        assertDateApplied(2026, Calendar.NOVEMBER, 12);
    }

    @Test
    public void testMouseUp_DateIsAppliedAfterMonthChangeByMouseWheelWithSameDayNumber() {
        openCalendarWithDate(2026, Calendar.OCTOBER, 12);
        scrollWheelTo(2026, Calendar.NOVEMBER, 12);

        moveToDateAndReleaseMouse(2026, Calendar.NOVEMBER, 12);

        assertDateApplied(2026, Calendar.NOVEMBER, 12);
    }

    @Test
    public void testMouseUp_DateIsAppliedAfterYearChangeByMouseWheel() {
        openCalendarWithDate(2026, Calendar.DECEMBER, 5);
        scrollWheelTo(2027, Calendar.JANUARY, 1);

        moveToDateAndReleaseMouse(2027, Calendar.JANUARY, 20);

        assertDateApplied(2027, Calendar.JANUARY, 20);
    }

    @Test
    public void testMouseUp_DateIsAppliedInUnchangedMonth() {
        openCalendarWithDate(2026, Calendar.OCTOBER, 5);

        moveToDateAndReleaseMouse(2026, Calendar.OCTOBER, 20);

        assertDateApplied(2026, Calendar.OCTOBER, 20);
    }

    @Test
    public void testMouseUp_DateIsNotAppliedAfterMonthNavigation() {
        openCalendarWithDate(2026, Calendar.OCTOBER, 5);

        moveToDateAndReleaseMouse(2026, Calendar.NOVEMBER, 1);

        assertDateNotApplied(2026, Calendar.OCTOBER, 5);
    }

    @Test
    public void testMouseUp_DateIsNotAppliedAfterYearNavigation() {
        openCalendarWithDate(2026, Calendar.DECEMBER, 5);

        moveToDateAndReleaseMouse(2027, Calendar.DECEMBER, 1);

        assertDateNotApplied(2026, Calendar.DECEMBER, 5);
    }

    @Test
    public void testMouseUp_DateIsAppliedOnClickAfterArrowNavigation() {
        openCalendarWithDate(2026, Calendar.OCTOBER, 5);
        moveToDateAndReleaseMouse(2026, Calendar.NOVEMBER, 1);

        moveToDateAndReleaseMouse(2026, Calendar.NOVEMBER, 15);

        assertDateApplied(2026, Calendar.NOVEMBER, 15);
    }

    @Test
    public void testMouseUp_DateIsAppliedOnClickAfterMouseWheelAndArrowNavigation() {
        openCalendarWithDate(2026, Calendar.OCTOBER, 5);
        scrollWheelTo(2026, Calendar.NOVEMBER, 1);
        moveToDateAndReleaseMouse(2026, Calendar.DECEMBER, 1);

        moveToDateAndReleaseMouse(2026, Calendar.DECEMBER, 10);

        assertDateApplied(2026, Calendar.DECEMBER, 10);
    }

    @Test
    public void testMouseUp_DateIsNotAppliedAfterNavigatingBackToInitialMonth() {
        openCalendarWithDate(2026, Calendar.OCTOBER, 5);

        moveToDateAndReleaseMouse(2026, Calendar.NOVEMBER, 1);
        moveToDateAndReleaseMouse(2026, Calendar.OCTOBER, 1);

        assertDateNotApplied(2026, Calendar.OCTOBER, 5);
    }

    @Test
    public void testMouseUp_DoubleClickDoesNothing() {
        openCalendarWithDate(2026, Calendar.OCTOBER, 5);
        calendarWidget.setDate(2026, Calendar.OCTOBER, 20);

        calendarWidget.notifyListeners(SWT.MouseUp, mouseEvent(2));

        assertDateNotApplied(2026, Calendar.OCTOBER, 5);
    }

    @Test
    public void testMouseWheel_ClosingCalendarBeforePendingUpdateDoesNotFail() {
        openCalendarWithDate(2026, Calendar.OCTOBER, 5);
        calendarWidget.setDate(2026, Calendar.NOVEMBER, 1);
        calendarWidget.notifyListeners(SWT.MouseWheel, new Event());
        calendarShell.dispose();

        assertDoesNotThrow(this::flushUiEvents);
    }

    private void openCalendarWithDate(int year, int month, int day) {
        dateControl.setText(formatted(year, month, day));
        dateControl.buttonClicked();
        assertThat(parentShell.getShells().length, is(1));
        calendarShell = parentShell.getShells()[0];
        assertThat(calendarShell.getChildren()[0], instanceOf(DateTime.class));
        calendarWidget = (DateTime)calendarShell.getChildren()[0];
    }

    private void scrollWheelTo(int year, int month, int day) {
        calendarWidget.setDate(year, month, day);
        calendarWidget.notifyListeners(SWT.MouseWheel, new Event());
        flushUiEvents();
    }

    private void flushUiEvents() {
        final Display display = parentShell.getDisplay();
        boolean eventDispatched;
        do {
            eventDispatched = display.readAndDispatch();
        } while (eventDispatched);
    }

    private void moveToDateAndReleaseMouse(int year, int month, int day) {
        calendarWidget.setDate(year, month, day);
        calendarWidget.notifyListeners(SWT.MouseUp, mouseEvent(1));
    }

    private void assertDateApplied(int year, int month, int day) {
        assertThat(dateControl.getText(), is(formatted(year, month, day)));
        assertThat(calendarShell.isDisposed(), is(true));
    }

    private void assertDateNotApplied(int unchangedYear, int unchangedMonth, int unchangedDay) {
        assertThat(dateControl.getText(), is(formatted(unchangedYear, unchangedMonth, unchangedDay)));
        assertThat(calendarShell.isDisposed(), is(false));
    }

    private Event mouseEvent(int count) {
        final Event event = new Event();
        event.button = 1;
        event.count = count;
        return event;
    }

    private String formatted(int year, int month, int day) {
        return dateControl.getDateFormat().formatDate(new GregorianCalendar(year, month, day).getTime());
    }
}
