package dev.akarah.protocol;

import dev.akarah.format.PacketFormat;
import dev.akarah.format.PacketFormats;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.Optional;
import java.util.function.Function;

public class Argument {
    public static PacketFormat<Byte> singleByte() {
        return new PacketFormats.ByteFormat();
    }

    public static PacketFormat<Integer> integer() {
        return new PacketFormats.IntegerFormat();
    }

    public static PacketFormat<Long> varInt() {
        return new PacketFormats.VariableSizedInteger();
    }

    public static PacketFormat<String> string() {
        return new PacketFormats.StringFormat();
    }

    public static<T> PacketFormat<T[]> arrayOf(PacketFormat<T> inner) {
        return new PacketFormats.SpecifiedArray<>(inner);
    }

    public static<T> PacketFormat<Optional<T>> optionalOf(PacketFormat<T> inner) {
        return new PacketFormats.SpecifiedOptional<>(inner);
    }

    public static<T> PacketFormat<Optional<T>> terminalOptionalOf(PacketFormat<T> inner) {
        return new PacketFormats.ImpliedOptional<>(inner);
    }

    public static<A, B> PacketFormat<Pair<A, B>> pair(
        PacketFormat<A> firstFormat,
        PacketFormat<B> secondFormat
    ) {
        return new PacketFormats.PairFormat<>(firstFormat, secondFormat);
    }

    public static<A, B, C> PacketFormat<Triplet<A, B, C>> triple(
        PacketFormat<A> firstFormat,
        PacketFormat<B> secondFormat,
        PacketFormat<C> thirdFormat
    ) {
        return new PacketFormats.TripletFormat<>(firstFormat, secondFormat, thirdFormat);
    }
}
