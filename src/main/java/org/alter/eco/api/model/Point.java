package org.alter.eco.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record Point(@JsonProperty("longitude") float longitude, @JsonProperty("latitude") float latitude) {

    @JsonCreator
    public Point {
    }

    public static Point fromString(String s) {
        s = s.replaceAll("[()]", "");
        float lo = Objects.requireNonNull(Float.valueOf(s.substring(0, s.indexOf(","))), "Null parsed as longitude");
        float la = Objects.requireNonNull(Float.valueOf(s.substring(s.indexOf(",") + 1)), "Null parsed as latitude");
        return new Point(lo, la);
    }

    public String toString() {
        return "(" + longitude + "," + latitude + ")";
    }
}
