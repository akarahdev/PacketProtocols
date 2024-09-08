package dev.akarah.format;

import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class PacketFormats {
    public record NewProtocol<T>(PacketFormat<T> innerType) implements PacketFormat<T> {
        @Override
        public T read(ByteBuffer stream) {
            return innerType.read(stream);
        }

        @Override
        public void write(ByteBuffer stream, T value) {
            innerType.write(stream, value);
        }

        @Override
        public int length(T value) {
            return innerType.length(value);
        }

        @Override
        public String generateJavaType() {
            return "Protocol<" + innerType.generateJavaType() + ">";
        }
    }

    public record SpecifiedArray<T>(PacketFormat<T> innerType) implements PacketFormat<T[]> {
        @Override
        public T[] read(ByteBuffer stream) {
            var len = stream.getShort();
            var array = (T[]) new Object[len];
            for(int i = 0; i < len; i++) {
                array[i] = innerType.read(stream);
            }
            return array;
        }

        @Override
        public void write(ByteBuffer stream, T[] value) {
            if(value.length > 65535)
                throw new RuntimeException("array too long to encode, length must be <= 65535");
            stream.putShort((short) value.length);
            for(var v : value) {
                innerType.write(stream, v);
            }
        }

        @Override
        public int length(T[] value) {
            int size = 0;
            for(var v : value)
                size += innerType.length(v);
            size += Short.BYTES;
            return size;
        }

        @Override
        public String generateJavaType() {
            return innerType.generateJavaType() + "[]";
        }
    }

    public record SpecifiedOptional<T>(PacketFormat<T> innerType) implements PacketFormat<Optional<T>> {
        @Override
        public Optional<T> read(ByteBuffer stream) {
            var opt = stream.get();
            if (opt == 1) {
                return Optional.of(innerType.read(stream));
            }
            return Optional.empty();
        }

        @Override
        public void write(ByteBuffer stream, Optional<T> value) {
            if (value.isPresent()) {
                stream.put((byte) 1);
                innerType.write(stream, value.get());
            } else {
                stream.put((byte) 0);
            }
        }

        @Override
        public int length(Optional<T> value) {
            return value.map(t -> 1 + innerType.length(t)).orElse(1);
        }

        @Override
        public String generateJavaType() {
            return "Optional<" + innerType.generateJavaType() + ">";
        }
    }

    public record ImpliedOptional<T>(PacketFormat<T> innerType) implements PacketFormat<Optional<T>> {
        @Override
        public Optional<T> read(ByteBuffer stream) {
            if (stream.hasRemaining()) {
                return Optional.of(innerType.read(stream));
            }
            return Optional.empty();
        }

        @Override
        public void write(ByteBuffer stream, Optional<T> value) {
            value.ifPresent(t -> innerType.write(stream, t));
        }

        @Override
        public int length(Optional<T> value) {
            return value.map(t -> 1 + innerType.length(t)).orElse(1);
        }

        @Override
        public String generateJavaType() {
            return "Optional<" + innerType.generateJavaType() + ">";
        }
    }

    public record ByteFormat() implements PacketFormat<Byte> {
        @Override
        public Byte read(ByteBuffer buf) {
            return buf.get();
        }

        @Override
        public void write(ByteBuffer buf, Byte value) {
            buf.put(value);
        }

        @Override
        public int length(Byte value) {
            return Byte.BYTES;
        }

        @Override
        public String generateJavaType() {
            return "Byte";
        }
    }

    public record IntegerFormat() implements PacketFormat<Integer> {
        @Override
        public Integer read(ByteBuffer buf) {
            return buf.getInt();
        }

        @Override
        public void write(ByteBuffer buf, Integer value) {
            buf.putInt(value);
        }

        @Override
        public int length(Integer value) {
            return Integer.BYTES;
        }

        @Override
        public String generateJavaType() {
            return "Integer";
        }
    }

    public record VariableSizedInteger() implements PacketFormat<Long> {
        private static final int SEGMENT_BITS = 0x7F;
        private static final int CONTINUE_BIT = 0x80;

        @Override
        public Long read(ByteBuffer buf) {
            long value = 0;
            int position = 0;
            byte currentByte;

            while (true) {
                currentByte = buf.get();
                value |= (long) (currentByte & SEGMENT_BITS) << position;

                if ((currentByte & CONTINUE_BIT) == 0) break;

                position += 7;

                if (position >= 64) throw new RuntimeException("VarLong is too big");
            }

            return value;
        }

        @Override
        public void write(ByteBuffer buf, Long value) {
            while(true) {
                if ((value & ~(long) SEGMENT_BITS) == 0) {
                    buf.put(value.byteValue());
                    return;
                }

                buf.put((byte) ((value & SEGMENT_BITS) | CONTINUE_BIT));

                value >>>= 7;
            }
        }

        @Override
        public int length(Long value) {
            int length = 0;

            while(true) {
                if ((value & ~(long) SEGMENT_BITS) == 0) {
                    length++;
                    return length;
                }

                value >>>= 7;
            }
        }

        @Override
        public String generateJavaType() {
            return "Long";
        }
    }

    public record StringFormat() implements PacketFormat<String> {
        @Override
        public String read(ByteBuffer buf) {
            byte[] array = new byte[buf.getInt()];
            for(int i = 0; i < array.length; i++)
                array[i] = buf.get();
            return new String(array, StandardCharsets.UTF_8);
        }

        @Override
        public void write(ByteBuffer buf, String value) {
            buf.putInt(value.getBytes(StandardCharsets.UTF_8).length);
            for(byte b : value.getBytes(StandardCharsets.UTF_8))
                buf.put(b);
        }

        @Override
        public int length(String value) {
            return Integer.BYTES + value.getBytes(StandardCharsets.UTF_8).length;
        }

        @Override
        public String generateJavaType() {
            return "String";
        }
    }

    public record PairFormat<A, B>(PacketFormat<A> first, PacketFormat<B> second)
        implements PacketFormat<Pair<A, B>> {
        @Override
        public Pair<A, B> read(ByteBuffer stream) {
            return new Pair<>(
                first.read(stream),
                second.read(stream)
            );
        }

        @Override
        public void write(ByteBuffer stream, Pair<A, B> value) {
            first.write(stream, value.getValue0());
            second.write(stream, value.getValue1());
        }

        @Override
        public int length(Pair<A, B> value) {
            return first.length(value.getValue0()) + second.length(value.getValue1());
        }

        @Override
        public String generateJavaType() {
            return "Pair<" + first.generateJavaType() + ", " + second.generateJavaType() + ">";
        }
    }

    public record TripletFormat<A, B, C>(PacketFormat<A> first, PacketFormat<B> second, PacketFormat<C> third)
        implements PacketFormat<org.javatuples.Triplet<A, B, C>> {
        @Override
        public Triplet<A, B, C> read(ByteBuffer stream) {
            return new Triplet<>(
                first.read(stream),
                second.read(stream),
                third.read(stream)
            );
        }

        @Override
        public void write(ByteBuffer stream, Triplet<A, B, C> value) {
            first.write(stream, value.getValue0());
            second.write(stream, value.getValue1());
            third.write(stream, value.getValue2());
        }

        @Override
        public int length(Triplet<A, B, C> value) {
            return first.length(value.getValue0()) + second.length(value.getValue1()) + third.length(value.getValue2());
        }

        @Override
        public String generateJavaType() {
            return "Triplet<" + first.generateJavaType() + ", " + second.generateJavaType() + "," + third.generateJavaType() + ">";
        }
    }

    public record QuartetFormat<A, B, C, D>(
        PacketFormat<A> first, PacketFormat<B> second, PacketFormat<C> third,
        PacketFormat<D> fourth)
        implements PacketFormat<Quartet<A, B, C, D>> {
        @Override
        public Quartet<A, B, C, D> read(ByteBuffer stream) {
            return new Quartet<>(
                first.read(stream),
                second.read(stream),
                third.read(stream),
                fourth.read(stream)
            );
        }

        @Override
        public void write(ByteBuffer stream, Quartet<A, B, C, D> value) {
            first.write(stream, value.getValue0());
            second.write(stream, value.getValue1());
            third.write(stream, value.getValue2());
            fourth.write(stream, value.getValue3());
        }

        @Override
        public int length(Quartet<A, B, C, D> value) {
            return first.length(value.getValue0()) + second.length(value.getValue1()) + third.length(value.getValue2())
                + fourth.length(value.getValue3());
        }

        @Override
        public String generateJavaType() {
            return "Quartet<" + first.generateJavaType() + ", " + second.generateJavaType() + "," + third.generateJavaType() +
                "," + fourth.generateJavaType() + ">";
        }
    }
}
