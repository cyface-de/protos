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
     * - 2 Byte for the first, empty Elevation
     * - 2+4 Bytes for the second, absolute Elevation value
     */
    private static final Integer SERIALIZED_SIZE_TWO_LOCATIONS_WITH_ONE_ELEVATION = 52;

    @Test
    void test_serializedSize_forEmptyMeasurement() throws InvalidProtocolBufferException {
        // Arrange
        final var measurement = Measurement.newBuilder().setFormatVersion(2).build();
        Validate.isTrue(measurement.isInitialized());

        // Act
        final var serialized = measurement.toByteArray();

        // Assert
        assertThat(serialized.length, is(equalTo(SERIALIZED_SIZE_FORMAT_VERSION_ONLY)));
        final Measurement deserialized = Measurement.parseFrom(serialized);
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
        final Measurement deserialized = Measurement.parseFrom(serialized);
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
        final Measurement deserialized = Measurement.parseFrom(serialized);
        assertThat(deserialized.getFormatVersion(), is(equalTo(measurement.getFormatVersion())));
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
                .addElevation(LocationRecords.Elevation.newBuilder().build()) // no elevation value
                .addElevation(LocationRecords.Elevation.newBuilder().setValue(48000).build()) // 480 m
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
        final Measurement deserialized = Measurement.parseFrom(serialized);
        assertThat(deserialized.getFormatVersion(), is(equalTo(measurement.getFormatVersion())));
    }

    @Test
    void test_serializedSize_one3600Locations() throws InvalidProtocolBufferException {
        // Arrange
        final var locations = generateLocations(3600, false);
        final var measurement = Measurement.newBuilder()
                .setFormatVersion(2)
                .setLocationRecords(locations)
                .build();
        Validate.isTrue(measurement.isInitialized());

        // Act
        final var serialized = measurement.toByteArray();

        // Assert (byte range tested with 100.000 random generated measurements)
        assertThat(serialized.length, is(greaterThan(32_000))); // with elevations: ~48_000
        assertThat(serialized.length, is(lessThan(34_000))); // with elevations: ~49_000
        final Measurement deserialized = Measurement.parseFrom(serialized);
        assertThat(deserialized.getFormatVersion(), is(equalTo(measurement.getFormatVersion())));
    }

    @SuppressWarnings("SameParameterValue")
    private LocationRecords generateLocations(@SuppressWarnings("SameParameterValue") final int amount,
            final boolean withElevations) {
        final var builder = LocationRecords.newBuilder();
        for (int i = 0; i < amount; i++) {
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
                final int random = (int)(Math.random() * 20);
                final int randomPlusMinus = random - 10;
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
