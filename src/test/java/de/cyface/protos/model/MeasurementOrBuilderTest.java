/*
 * Copyright 2021-2022 Cyface GmbH
 *
 * This file is part of the Cyface Protobuf Messages.
 *
 * The Cyface Protobuf Messages is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Cyface Protobuf Messages is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Cyface Protobuf Messages. If not, see <http://www.gnu.org/licenses/>.
 */
package de.cyface.protos.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.Test;

import com.google.protobuf.InvalidProtocolBufferException;

public class MeasurementOrBuilderTest {

    /**
     * Number of bytes of a serialized measurement (1 Byte) which contains:
     * - the format version (2 Bytes)
     */
    private static final Integer SERIALIZED_SIZE_FORMAT_VERSION_ONLY = 3;
    /**
     * Number of bytes of a serialized measurement (1 Byte) which contains:
     * - the format version (2 Bytes)
     * - one location without elevation (2+8+6+6+1+4+4=31 Byte)
     */
    private static final Integer SERIALIZED_SIZE_ONE_LOCATION_WITHOUT_ELEVATION = 34;
    /**
     * Number of bytes of a serialized measurement (1 Byte) which contains:
     * - the format version (2 Bytes)
     * - two locations without elevation (31 Byte for the first, 0+2+2+2+1+2+2=11 Byte for the second)
     */
    private static final Integer SERIALIZED_SIZE_TWO_LOCATIONS_WITHOUT_ELEVATION = 44;
    /**
     * Number of bytes of a serialized measurement (1 Byte) which contains:
     * - the format version (2 Bytes)
     * - two locations without elevation (42 Bytes)
     * - 2+2 Byte for the first, empty Elevation (isNull=true)
     * - 2+4 Bytes for the second, absolute Elevation value
     */
    private static final Integer SERIALIZED_SIZE_TWO_LOCATIONS_WITH_ONE_ELEVATION = 54;
    /**
     * Number of bytes of a serialized measurement (1 Byte) which contains:
     * - the format version (2 Bytes)
     * - first location without elevations (31 Bytes)
     * - subsequent locations without elevations (+ 3599 * ~9.2 Bytes = ~33_110)
     */
    private static final int[] SERIALIZED_SIZE_RANGE_3600_LOCATIONS_WITHOUT_ELEVATIONS = new int[] {32_000, 34_000};
    /**
     * Number of bytes of a serialized measurement (1 Byte) which contains:
     * - the format version (2 Bytes)
     * - first location without elevations (31 Bytes)
     * - subsequent locations without elevations (+ 3599 * ~9 Bytes = 33_110)
     * - 2+4 Bytes for the first, absolute Elevation value (6 Bytes)
     * - 2+2 Bytes for the second, relative Elevation value (+ 3599 * ~4.2 Bytes = ~15_115)
     */
    private static final int[] SERIALIZED_SIZE_RANGE_3600_LOCATIONS_WITH_ELEVATIONS = new int[] {48_000, 49_000};

    @Test
    void test_serializedSize_forEmptyMeasurement() throws InvalidProtocolBufferException {
        // Arrange
        final var measurement = Measurement.newBuilder().setFormatVersion(2).build();
        Validate.isTrue(measurement.isInitialized());

        // Act
        final var serialized = measurement.toByteArray();

        // Assert
        assertThat(serialized.length, is(equalTo(SERIALIZED_SIZE_FORMAT_VERSION_ONLY)));
        final var deserialized = Measurement.parseFrom(serialized);
        assertThat(deserialized.getFormatVersion(), is(equalTo(measurement.getFormatVersion())));
    }

    @Test
    void test_serializedSize_forOneLocation_withoutElevation() throws InvalidProtocolBufferException {
        // Arrange
        // noinspection SpellCheckingInspection
        final var locations = LocationRecords.newBuilder()
                .addTimestamp(1621582427000L)
                .addLatitude(51_064590) // At "Hafenbrücke" in Dresden "Alberthafen"
                .addLongitude(13_699045)
                .addAccuracy(800) // 8 m
                .addSpeed(1000) // 10 m/s
                .build();
        final var measurement = Measurement.newBuilder()
                .setFormatVersion(2)
                .setLocationRecords(locations)
                .build();
        Validate.isTrue(measurement.isInitialized());

        // Act
        final var serialized = measurement.toByteArray();

        // Assert
        assertThat(serialized.length, is(equalTo(SERIALIZED_SIZE_ONE_LOCATION_WITHOUT_ELEVATION)));
        final var deserialized = Measurement.parseFrom(serialized);
        assertThat(deserialized.getFormatVersion(), is(equalTo(measurement.getFormatVersion())));
    }

    @Test
    void test_serializedSize_forTwoLocations_withoutElevations() throws InvalidProtocolBufferException {
        // Arrange
        // noinspection SpellCheckingInspection
        final var locations = LocationRecords.newBuilder()
                .addTimestamp(1621582427000L)
                .addTimestamp(1000L) // 1 second later
                .addLatitude(51_064590) // Crossing "Hafenbrücke" in Dresden "Alberthafen"
                .addLatitude(190)
                .addLongitude(13_699045)
                .addLongitude(-700)
                .addAccuracy(800) // 8 m
                .addAccuracy(-300) // 5 m
                .addSpeed(1000) // 10 m/s
                .addSpeed(-1000) // 0 m/s
                .build();
        final var measurement = Measurement.newBuilder()
                .setFormatVersion(2)
                .setLocationRecords(locations)
                .build();
        Validate.isTrue(measurement.isInitialized());

        // Act
        final var serialized = measurement.toByteArray();

        // Assert
        assertThat(serialized.length, is(equalTo(SERIALIZED_SIZE_TWO_LOCATIONS_WITHOUT_ELEVATION)));
        final var deserialized = Measurement.parseFrom(serialized);
        assertThat(deserialized.getFormatVersion(), is(equalTo(measurement.getFormatVersion())));
        assertThat(deserialized.getLocationRecords().getElevationCount(), is(equalTo(0)));
    }

    @Test
    void test_serializedSize_forTwoLocations_withOneElevations() throws InvalidProtocolBufferException {
        // Arrange
        // noinspection SpellCheckingInspection
        final var locations = LocationRecords.newBuilder()
                .addTimestamp(1621582427000L)
                .addTimestamp(1000L) // 1 second later
                .addLatitude(51_064590) // Crossing "Hafenbrücke" in Dresden "Alberthafen"
                .addLatitude(190)
                .addLongitude(13_699045)
                .addLongitude(-700)
                .addAccuracy(800) // 8 m
                .addAccuracy(-300) // 5 m
                .addSpeed(1000) // 10 m/s
                .addSpeed(-1000) // 0 m/s
                // (!) If no elevation is present for some locations only, always set `isNull` or else elevation is 0!
                .addElevation(LocationRecords.Elevation.newBuilder().setIsNull(true).build()) // no elevation value
                .addElevation(LocationRecords.Elevation.newBuilder().setValue(480_00).build()) // 480 m
                .build();
        final var measurement = Measurement.newBuilder()
                .setFormatVersion(2)
                .setLocationRecords(locations)
                .build();
        Validate.isTrue(measurement.isInitialized());

        // Act
        final var serialized = measurement.toByteArray();

        // Assert
        assertThat(serialized.length, is(equalTo(SERIALIZED_SIZE_TWO_LOCATIONS_WITH_ONE_ELEVATION)));
        final var deserialized = Measurement.parseFrom(serialized);
        assertThat(deserialized.getFormatVersion(), is(equalTo(measurement.getFormatVersion())));
        assertThat(deserialized.getLocationRecords().getElevationCount(), is(equalTo(2)));
        assertThat(deserialized.getLocationRecords().getElevation(0).getIsNull(), is(equalTo(true)));
        assertThat(deserialized.getLocationRecords().getElevation(1).getIsNull(), is(equalTo(false)));
        assertThat(deserialized.getLocationRecords().getElevation(1).getValue(), is(equalTo(480_00)));
    }

    @Test
    void test_serializedSize_one3600Locations() throws InvalidProtocolBufferException {
        // Arrange
        final var locationsWithoutElevations = generateLocations(3600, false);
        final var locationsWithElevations = generateLocations(3600, true);
        final var measurementWithoutElevations = Measurement.newBuilder()
                .setFormatVersion(2)
                .setLocationRecords(locationsWithoutElevations)
                .build();
        final var measurementWithElevations = Measurement.newBuilder()
                .setFormatVersion(2)
                .setLocationRecords(locationsWithElevations)
                .build();
        Validate.isTrue(measurementWithoutElevations.isInitialized());
        Validate.isTrue(measurementWithElevations.isInitialized());

        // Act
        final var serializedWithoutElevations = measurementWithoutElevations.toByteArray();
        final var serializedWithElevations = measurementWithElevations.toByteArray();

        // Assert (byte range tested with 100.000 random generated measurements)
        assertThat(serializedWithoutElevations.length,
                is(greaterThan(SERIALIZED_SIZE_RANGE_3600_LOCATIONS_WITHOUT_ELEVATIONS[0])));
        assertThat(serializedWithoutElevations.length,
                is(lessThan(SERIALIZED_SIZE_RANGE_3600_LOCATIONS_WITHOUT_ELEVATIONS[1])));
        assertThat(serializedWithElevations.length,
                is(greaterThan(SERIALIZED_SIZE_RANGE_3600_LOCATIONS_WITH_ELEVATIONS[0])));
        assertThat(serializedWithElevations.length,
                is(lessThan(SERIALIZED_SIZE_RANGE_3600_LOCATIONS_WITH_ELEVATIONS[1])));
        final var deserialized = Measurement.parseFrom(serializedWithoutElevations);
        assertThat(deserialized.getFormatVersion(), is(equalTo(measurementWithoutElevations.getFormatVersion())));
    }

    @SuppressWarnings("SameParameterValue")
    private LocationRecords generateLocations(@SuppressWarnings("SameParameterValue") final int amount,
            final boolean withElevations) {
        final var builder = LocationRecords.newBuilder();
        for (var i = 0; i < amount; i++) {
            if (i == 0) {
                // noinspection SpellCheckingInspection
                builder.addTimestamp(1621582427000L)
                        .addLatitude(51_064590) // Crossing "Hafenbrücke" in Dresden "Alberthafen"
                        .addLongitude(13_699045)
                        .addAccuracy(800) // 8 m
                        .addSpeed(1000); // 10 m/s
                if (withElevations) {
                    builder.addElevation(LocationRecords.Elevation.newBuilder().setValue(48000).build()); // 480 m
                }
            } else {
                final var random = (int)(Math.random() * 20);
                final var randomPlusMinus = random - 10;
                builder.addTimestamp(1000L) // 1 second later
                        .addLatitude(randomPlusMinus * 10)
                        .addLongitude(randomPlusMinus * 100)
                        .addAccuracy(randomPlusMinus * 100)
                        .addSpeed(randomPlusMinus * 100);
                if (withElevations) {
                    builder.addElevation(LocationRecords.Elevation.newBuilder().setValue(randomPlusMinus * 10).build());
                }
            }
        }
        return builder.build();
    }
}
