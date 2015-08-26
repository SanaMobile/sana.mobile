/**Copyright (c) 2015, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Sana nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.util;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;

/**
 * Convenience wrappers around common calculations.
 *
 * @author Sana Development Team
 */

/**
 *
 */
public class Functions {
    public static final int MILLISECONDS = Calendar.MILLISECOND;
    public static final int SECONDS = Calendar.SECOND;
    public static final int MINUTES = Calendar.MINUTE;
    public static final int HOURS = Calendar.HOUR;
    public static final int DAYS = Calendar.DAY_OF_YEAR;
    public static final int WEEKS = Calendar.WEEK_OF_YEAR;
    public static final int MONTHS = Calendar.MONTH;
    public static final int YEARS = Calendar.YEAR;

    /**
     * Convenience wrapper around {@link java.util.Calendar#get(int) get(int)}
     * to determine whether the specified field value of the first argument
     * should be considered to occur before the second. Allowed field values
     * are:
     * <pre>
     *  {@link #MILLISECONDS}
     *  {@link #SECONDS}
     *  {@link #MINUTES}
     *  {@link #HOURS}
     *  {@link #DAYS}
     *  {@link #WEEKS}
     *  {@link #MONTHS}
     *  {@link #YEARS}
     * </pre>
     *
     * @param arg1
     * @param arg2
     * @param field The {@link java.util.Calendar Calendar} field to compare
     * @return <code>true</code> if <code>arg1</code> is before
     *      <code>arg2</code>
     * @throws NullPointerException If <code>arg1</code> or <code>arg2</code>
     *      is <code>null</code>.
     * @throws IllegalArgumentException If <code>field</code> is invalid.
     */
    protected static boolean isBefore(Calendar arg1, Calendar arg2, int field) {
        // Argument validation
        if (arg1 == null)
            throw new NullPointerException("arg1 is null");
        if (arg2 == null)
            throw new NullPointerException("arg2 is null");
        boolean result = false;
        switch (field) {
            case MILLISECONDS:
            case SECONDS:
            case MINUTES:
            case HOURS:
            case DAYS:
            case WEEKS:
            case MONTHS:
            case YEARS:
                // TODO add additional comparators
                result = (arg1.get(field) < arg2.get(field));
                break;
            default:
                throw new IllegalArgumentException("Invalid Calendar units: "
                        + field + " See Functions.difference(Calendar,Calendar)"
                        + " for allowed values");
        }
        return result;
    }

    /**
     * Convenience method for calculating the years between a
     * {@link java.util.Date Date} and the current system time.
     *
     * @param val
     * @return The number of years between the date and now.
     * @throws NullPointerException If <code>val</code> is <code>null</code>.
     */
    public static long age(Date val) {
        return period(new Date(), val, YEARS);
    }

    /**
     * Convenience method for calculating the days between two
     * {@link java.util.Date Date} objects.
     *
     * @param arg1
     * @param arg2
     * @return The number of dayss between the two dates.
     * @throws NullPointerException If <code>arg1</code> or <code>arg2</code>
     *      is <code>null</code>.
     */
    public static long days(Date arg1, Date arg2) {
        return period(arg1, arg2, DAYS);
    }

    /**
     * Convenience method for calculating the weeks between two
     * {@link java.util.Date Date} objects.
     *
     * @param arg1
     * @param arg2
     * @return The number of weeks between the two dates.
     * @throws NullPointerException If <code>arg1</code> or <code>arg2</code>
     *      is <code>null</code>.
     */
    public static long weeks(Date arg1, Date arg2) {
        return period(arg1, arg2, WEEKS);
    }

    /**
     * Convenience method for calculating the months between two
     * {@link java.util.Date Date} objects.
     *
     * @param arg1
     * @param arg2
     * @return The number of months between the two dates.
     * @throws NullPointerException If <code>arg1</code> or <code>arg2</code>
     *      is <code>null</code>.
     */
    public static long months(Date arg1, Date arg2) {
        return period(arg1, arg2, MONTHS);
    }

    /**
     * Convenience method for calculating the years between two
     * {@link java.util.Date Date} objects.
     *
     * @param arg1
     * @param arg2
     * @return The number of years between the two dates.
     * @throws NullPointerException If <code>arg1</code> or <code>arg2</code>
     *      is <code>null</code>.
     */
    public static long years(Date arg1, Date arg2) {
        return period(arg1, arg2, YEARS);
    }


    /**
     * Calculates the time between two {@link java.util.Date Date} objects
     * using {@link java.util.Date#getTime() Date.getTime()}.
     *
     * @param arg1 The value to use as the minuend.
     * @param arg2 The value to use as the subtrahend.
     * @return The milliseconds between the arguments.
     * @throws NullPointerException If <code>arg1</code> or <code>arg2</code>
     *      is <code>null</code>.
     */
    public static long difference(Date arg1, Date arg2) {
        // Argument validation
        if (arg1 == null) throw new NullPointerException("null minuend");
        if (arg2 == null) throw new NullPointerException("null subtrahend");

        // Do calculation
        return arg1.getTime() - arg2.getTime();
    }

    /**
     * Calculates the milliseconds between two {@link java.util.Calendar Calendar}
     * objects. Equivalent to a call to
     * {@link #difference(Calendar, Calendar, int) difference(Calendar, Calendar, int)}
     * with the units specified as {@link #MILLISECONDS}.
     *
     * @param arg1 The value to use as the minuend.
     * @param arg2 The value to use as the subtrahend.
     * @return The milliseconds between the arguments.
     * @throws NullPointerException If <code>arg1</code> or <code>arg2</code>
     *      is null.
     */
    public static long difference(Calendar arg1, Calendar arg2) {
        return difference(arg1,arg2,MILLISECONDS);
    }

    /**
     * Calculates the difference between two {@link java.util.Calendar Calendar}
     * objects and returns the {@link java.lang.Math#floor(double) floor} of
     * the result in the specified <code>units</code>. The units value must be
     * one of:
     * <pre>
     *  {@link #MILLISECONDS}
     *  {@link #SECONDS}
     *  {@link #MINUTES}
     *  {@link #HOURS}
     *  {@link #DAYS}
     *  {@link #WEEKS}
     *  {@link #MONTHS}
     *  {@link #YEARS}
     * </pre>
     *
     * @param arg1 The value to use as the minuend.
     * @param arg2 The value to use as the subtrahend.
     * @param units The {@link java.util.Calendar Calendar} field to use for
     *      expressing the difference.
     * @return The difference between the arguments in the specified units.
     * @throws NullPointerException If <code>arg1</code> or <code>arg2</code>
     *      is null.
     */
    public static long difference(Calendar arg1, Calendar arg2, int units) {
        // Argument validation
        if (arg1 == null) throw new NullPointerException("null minuend");
        if (arg2 == null) throw new NullPointerException("null subtrahend");

        // Do calculation
        long minuend;
        long subtrahend;
        long delta;
        double mod = 1.0;

        // Compute difference using units as Calendar field
        switch(units) {
            // First several we chain together the multiplier
            case WEEKS:
                mod = mod*7;
            case DAYS:
                mod = mod*24;
            case HOURS:
                mod = mod*60;
            case MINUTES:
                mod = mod*60;
            case SECONDS:
                mod = mod*1000;
            case MILLISECONDS:
                delta = arg1.getTimeInMillis() - arg2.getTimeInMillis();
                delta = (long) Math.floor(delta/mod);
                break;

            case MONTHS:
            case YEARS:
                minuend = arg1.get(Calendar.YEAR);
                subtrahend = arg2.get(Calendar.YEAR);
                delta = minuend - subtrahend;
                if(isBefore(arg1,arg2,Calendar.DAY_OF_YEAR)) delta--;
                break;
            default:
                throw new IllegalArgumentException("Invalid Calendar units: "
                        + units + " See Functions.difference(Calendar,Calendar)"
                        + " for allowed values");
        }
        return delta;
    }

    /**
     * Calculates the value of the difference between two
     * {@link java.lang.Number Number} objects as a <code>double</code>.
     *
     * @param arg1 The value to use as the minuend.
     * @param arg2 The value to use as the subtrahend.
     * @return The <code>double</code> value of <code>arg1 - arg2</code>
     * @throws NullPointerException If <code>arg1</code> or <code>arg2</code>
     *      is null.
     */
    public static double difference(Number arg1, Number arg2) {
        // Argument validation
        if (arg1 == null) throw new NullPointerException("null minuend");
        if (arg2 == null) throw new NullPointerException("null subtrahend");

        // Do calculation
        Double val1 = (arg1 == null) ? 0 : arg1.doubleValue();
        Double val2 = (arg2 == null) ? 0 : arg2.doubleValue();
        return val1 - val2;
    }

    /**
     * Calculates the sum of an array {@link java.lang.Number Number} objects
     * as a <code>double</code>.
     *
     * @param args The values to sum.
     * @return The sum of the parameters as a <code>double</code>
     * @throws NullPointerException If <code>args</code> is null.
     * @throws IllegalArgumentException If length of <code>args</code> is zero.
     */
    public static double sum(Number... args) {
        // Argument validation
        if (args == null)
            throw new NullPointerException("Can not compute sum of null");
        if (args.length == 0)
            throw new IllegalArgumentException(
                    "Can not compute sum of zero values");

        // Do calculation
        Double result = Double.valueOf(0);
        for (Number arg : args) {
            result += arg.doubleValue();
        }
        return result;
    }

    /**
     * Calculates the sum of an array {@link java.lang.Number Number} objects
     * as a <code>double</code>.
     *
     * @param args The values to sum.
     * @return The sum of the parameters as a <code>double</code>
     * @throws NullPointerException If <code>args</code> is null.
     * @throws IllegalArgumentException If length of <code>args</code> is zero.
     */
    public static double sum(Collection<? extends Number> args) {
        // Argument validation
        if (args == null)
            throw new NullPointerException("Can not compute sum of null");
        if (args.size() == 0)
            throw new IllegalArgumentException(
                    "Can not compute sum of zero values");

        // Do calculation
        Double result = Double.valueOf(0);
        for (Number arg : args) {
            result += arg.doubleValue();
        }
        return result;
    }

    /**
     * Calculates the mean value, as a <code>double</code> of one or more
     * {@link java.lang.Number Number} objects using the {@link #sum(Number...)
     * sum(args)} as the dividend and length of <code></code>.
     *
     * @param args
     *            The values used to calculate the mean
     * @return
     * @throws NullPointerException
     *             If <code>args</code> is null.
     * @throws IllegalArgumentException
     *             if length of <code>args</code> is zero.
     */
    public static double mean(Number... args) {
        // Argument validation
        if (args == null)
            throw new NullPointerException("Can not compute mean of null");
        if (args.length == 0)
            throw new IllegalArgumentException(
                    "Can not compute mean of zero values");

        // Do calculation
        return sum(args) / args.length;

    }

    /**
     * Calculates the mean value, as a <code>double</code> of one or more
     * {@link java.lang.Number Number} objects using the {@link #sum(Number...)
     * sum(args)} as the dividend and length of <code></code>.
     *
     * @param args
     *            The values used to calculate the mean
     * @return
     * @throws NullPointerException
     *             If <code>args</code> is null.
     * @throws IllegalArgumentException
     *             if length of <code>args</code> is zero.
     */
    public static double mean(Collection<? extends Number> args) {
        // Argument validation
        if (args == null)
            throw new NullPointerException("Can not compute mean of null");
        if (args.size() == 0)
            throw new IllegalArgumentException(
                    "Can not compute mean of zero values");

        // Do calculation
        return sum(args) / args.size();

    }

    /**
     * Calculates the difference between two times, given as long values, and
     * returns the period between them in the specified <code>units</code>. The
     * units value must be one of:
     * <pre>
     *  {@link #MILLISECONDS}
     *  {@link #SECONDS}
     *  {@link #MINUTES}
     *  {@link #HOURS}
     *  {@link #DAYS}
     *  {@link #WEEKS}
     *  {@link #MONTHS}
     *  {@link #YEARS}
     * </pre>
     * All values will be returned as the absolute value of the difference.
     *
     * @param arg1 The value to use as the minuend.
     * @param arg2 The value to use as the subtrahend.
     * @param units The time units to use for expressing the difference.
     * @return The long value of the difference between the arguments in the
     *      specified units.
     */
    public static long period(long arg1, long arg2, int units) {
        long delta = arg1 - arg2;
        DateTime start = new DateTime(arg1);
        DateTime end = new DateTime(arg2);
        // Compute delta into appropriate units
        switch(units) {
            case YEARS:
                delta = Years.yearsBetween(start, end).getYears();
                break;
            case MONTHS:
                delta = Months.monthsBetween(start, end).getMonths();
                break;
            case WEEKS:
                delta = Weeks.weeksBetween(start, end).getWeeks();
                break;
            case DAYS:
                delta = Days.daysBetween(start, end).getDays();
                break;
            case HOURS:
                delta = Hours.hoursBetween(start, end).getHours();
                break;
            case MINUTES:
                delta = Minutes.minutesBetween(start, end).getMinutes();
                break;
            case SECONDS:
                delta = Double.valueOf(Math.floor(delta/1000.0)).longValue();
                break;
            case MILLISECONDS:
                // Here for completeness but already calculated
                break;
            default:
                throw new IllegalArgumentException("Invalid units: "
                        + units + " See Functions.difference(Calendar,Calendar)"
                        + " for allowed values");
        }
        return Math.abs(delta);
    }

    /**
     * Calculates the difference between two {@link java.util.Calendar Calendar}
     * objects and returns the period between them in the specified
     * <code>units</code>. The units value must be one of:
     * <pre>
     *  {@link #MILLISECONDS}
     *  {@link #SECONDS}
     *  {@link #MINUTES}
     *  {@link #HOURS}
     *  {@link #DAYS}
     *  {@link #WEEKS}
     *  {@link #MONTHS}
     *  {@link #YEARS}
     * </pre>
     *
     * @param arg1 The value to use as the minuend.
     * @param arg2 The value to use as the subtrahend.
     * @param units The time units to use for expressing the difference.
     * @return The long value of the period between the two
     *      {@link java.util.Calendar Calendar} objects.
     * @throws NullPointerException If <code>arg1</code> or <code>arg2</code>
     *      is null.
     */
    public static long period(Calendar arg1, Calendar arg2, int units) {
        // Argument validation
        if (arg1 == null) throw new NullPointerException("null minuend");
        if (arg2 == null) throw new NullPointerException("null subtrahend");
        return period(arg1.getTimeInMillis(),arg2.getTimeInMillis(),units);
    }


    /**
     * Calculates the difference between two {@link java.util.Date Date}
     * objects and returns the period between them in the specified
     * <code>units</code>. The units value must be one of:
     * <pre>
     *  {@link #MILLISECONDS}
     *  {@link #SECONDS}
     *  {@link #MINUTES}
     *  {@link #HOURS}
     *  {@link #DAYS}
     *  {@link #WEEKS}
     *  {@link #MONTHS}
     *  {@link #YEARS}
     * </pre>
     *
     * @param arg1 The value to use as the minuend.
     * @param arg2 The value to use as the subtrahend.
     * @param units The time units to use for expressing the difference.
     * @return The long value of the period between the two
     *      {@link java.util.Date Date} objects.
     * @throws NullPointerException If <code>arg1</code> or <code>arg2</code>
     *      is null.
     */
    public static long period(Date arg1, Date arg2, int units) {
        // Argument validation
        if (arg1 == null) throw new NullPointerException("null minuend");
        if (arg2 == null) throw new NullPointerException("null subtrahend");
        return period(arg1.getTime(),arg2.getTime(),units);
    }

    /**
     * Returns the the first day of the year whose age would correspond to now
     * minus a number of years.
     * @param age The number of years from now. Positive age parameter is interpreted as years
     *            prior.
     * @return A new date object as the first day of the first month n years from now.
     */
    public static Date dateFromAge(int age){
        DateTime dateTime = new DateTime();
        Date date = dateTime.minusYears(age).withMonthOfYear(1).withDayOfMonth(1).toDate();
        return date;
    }
}
