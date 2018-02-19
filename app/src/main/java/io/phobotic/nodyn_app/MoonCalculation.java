/*
 * Copyright (c) 2018 Jonathan Nelson <ciasaboark@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.phobotic.nodyn_app;

/**
 * File:      MoonCalculation.java
 * Author:    Angus McIntyre <angus@pobox.com>
 * Date:      31.05.96
 * Updated:   01.06.96
 * <p>
 * Java class to calculate the phase of the moon, given a date. The
 * 'moonPhase' method is a Java port of some public domain C source which
 * was apparently originally part of the Decus C distribution (whatever
 * that was). Details of the algorithm are given below.
 * <p>
 * To use this in a program, create an object of class 'MoonCalculation'.
 * <p>
 * I'm not convinced that the algorithm is entirely accurate, but I don't
 * know enough to confirm whether it is or not.
 * <p>
 * HISTORY
 * -------
 * <p>
 * 31.05.96    SLAM    Converted from C
 * 01.06.96    SLAM    Added 'phaseName' to return the name of a phase.
 * Fixed leap year test in 'daysInMonth'.
 * <p>
 * LEGAL
 * -----
 * <p>
 * This software is free. It can be used and modified in any way you
 * choose. The author assumes no liability for any loss or damage you
 * may incur through use of or inability to use this software. This
 * disclaimer must appear on any modified or unmodified version of
 * the software in which the name of the author also appears.
 **/

// MoonCalculation
//
// Class to calculate information about the phases of the moon.

public class MoonCalculation {

    // day_year - gives the day of the year for the first day of each
    // month -1. i.e. 1st January is the 0th day of the year, 1st
    // February is the 31st etc. Used by 'moonPhase'.

    public static final String moon_phase_name[] = {
            "New",
            "Waxing crescent",
            "First quarter",
            "Waxing gibbous",
            "Full",
            "Waning gibbous",
            "Third quarter",
            "Waning crescent"};

    // moon_phase_name - the English name for the different phases.
    // Change this if you need to localise the software.
    private static final int day_year[] = {-1, -1, 30, 58, 89, 119,
            150, 180, 211, 241, 272,
            303, 333};

    // MoonPhase
    //
    // Output the phase of the moon for the given year, month, day.
    // The routine calculates the year's epact (the age of the moon on Jan 1.),
    // adds this to the number of days in the year, and calculates the phase
    // of the moon for this date.
    //
    // In the algorithm:
    //
    //      diy     Is the day of the year - 1 (i.e., Jan 1 is day 0).
    //
    //      golden  Is the number of the year in the Mentonic cycle, used to
    //              determine the position of the calender moon.
    //
    //      epact   Is the age of the calender moon (in days) at the beginning
    //              of the year.  To calculate epact, two century-based
    //              corrections are applied:
    //              Gregorian:      (3 * cent)/4 - 12
    //                      is the number of years such as 1700, 1800 when
    //                      leap year was not held.
    //              Clavian:        (((8 * cent) + 5) / 25) - 5
    //                      is a correction to the Mentonic cycle of about
    //                      8 days every 2500 years.  Note that this will
    //                      overflow 16 bits in the year 409600.  Beware.
    //
    // The algorithm is accurate for the Gregorian calender only.
    //
    // The magic numbers used in the phase calculation are as follows:
    //       29.5           The moon's period in days.
    //      177             29.5 scaled by 6
    //       22             (29.5 / 8) scaled by 6 (this gets the phase)
    //       11             ((29.5 / 8) / 2) scaled by 6
    //
    // Theoretically, this should yield a number in the range 0 .. 7.  However,
    // two days per year, things don't work out too well.
    //
    // Epact is calculated by the algorithm given in Knuth vol. 1 (calculation
    // of Easter).  See also the article on Calenders in the Encyclopaedia
    // Britannica and Knuth's algorithm in CACM April 1962, page 209.
    //
    // Arguments to the function are:
    //
    // int     year     1978 = 1978
    // int     month    Jan = 1
    // int     day      1 = 1

    public int moonPhase(int year, int month, int day) {

        int phase;          // Moon phase
        int cent;           // Century number (1979 = 20)
        int epact;          // Age of the moon on Jan. 1
        int diy;            // Day in the year
        int golden;         // Moon's golden number

        if (month < 0 || month > 12) month = 0;     // Just in case
        diy = day + day_year[month];                // Day in the year
        if ((month > 2) && this.isLeapYearP(year))
            diy++;                                  // Leapyear fixup
        cent = (year / 100) + 1;                    // Century number
        golden = (year % 19) + 1;                   // Golden number
        epact = ((11 * golden) + 20                 // Golden number
                + (((8 * cent) + 5) / 25) - 5       // 400 year cycle
                - (((3 * cent) / 4) - 12)) % 30;    //Leap year correction
        if (epact <= 0)
            epact += 30;                        // Age range is 1 .. 30
        if ((epact == 25 && golden > 11) ||
                epact == 24)
            epact++;

        // Calculate the phase, using the magic numbers defined above.
        // Note that (phase and 7) is equivalent to (phase mod 8) and
        // is needed on two days per year (when the algorithm yields 8).

        phase = (((((diy + epact) * 6) + 11) % 177) / 22) & 7;

        return (phase);
    }

    // daysInMonth
    //
    // Returns the number of days in a month given the month and the year.

    public boolean isLeapYearP(int year) {
        return ((year % 4 == 0) &&
                ((year % 400 == 0) || (year % 100 != 0)));
    }

    // isLeapYearP
    //
    // Return true if the year is a leapyear

    int daysInMonth(int month, int year) {
        int result = 31;
        switch (month) {
            case 4:
            case 6:
            case 9:
            case 11:
                result = 30;
                break;
            case 2:
                result = (this.isLeapYearP(year) ? 29 : 28);
        }
        return result;
    }

    // phaseName
    //
    // Return the name of a given phase

    String phaseName(int phase) {
        return moon_phase_name[phase];
    }
}