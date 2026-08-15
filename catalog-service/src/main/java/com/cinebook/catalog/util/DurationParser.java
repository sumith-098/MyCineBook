package com.cinebook.catalog.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses free-text movie durations like "2h 30m", "2h", "150m", "150" into minutes, so
 * showtime overlap checks can reason about a screen's actual busy window instead of only
 * catching an exact-time clash. Owners type duration as free text (same string shown on movie
 * cards), so this is best-effort: unparseable or blank input falls back to a conservative
 * default rather than treating an unparseable movie as 0 minutes long, which would let
 * showtimes overlap it freely.
 */
public final class DurationParser {

    private static final Pattern HOURS = Pattern.compile("(\\d+)\\s*h", Pattern.CASE_INSENSITIVE);
    private static final Pattern MINUTES = Pattern.compile("(\\d+)\\s*m", Pattern.CASE_INSENSITIVE);
    private static final int DEFAULT_MINUTES = 180; // ~3 hours: runtime + trailers + cleaning buffer

    private DurationParser() {}

    public static int toMinutes(String duration) {
        if (duration == null || duration.isBlank()) return DEFAULT_MINUTES;

        Matcher hMatch = HOURS.matcher(duration);
        Matcher mMatch = MINUTES.matcher(duration);
        boolean hasH = hMatch.find();
        boolean hasM = mMatch.find();

        if (hasH || hasM) {
            int hours = hasH ? Integer.parseInt(hMatch.group(1)) : 0;
            int minutes = hasM ? Integer.parseInt(mMatch.group(1)) : 0;
            int total = hours * 60 + minutes;
            return total > 0 ? total : DEFAULT_MINUTES;
        }

        try {
            int plain = Integer.parseInt(duration.trim());
            return plain > 0 ? plain : DEFAULT_MINUTES;
        } catch (NumberFormatException e) {
            return DEFAULT_MINUTES;
        }
    }
}