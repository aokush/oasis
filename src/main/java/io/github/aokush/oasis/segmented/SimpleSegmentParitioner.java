package io.github.aokush.oasis.segmented;

import java.math.BigInteger;

/**
 * Generates a value that determines the segment where an entry should be stored
 *
 * @author AKuseju
 * @param <K>
 */
class SimpleSegmentParitioner implements SegmentParitioner {

    final int modulo;

    SimpleSegmentParitioner(int modulo) {
        this.modulo = modulo;
    }

    @Override
    public String getSegmentHash(Object item) {
        return String.valueOf(toLong(item.toString()) % modulo);
    }

    private long toLong(String value) {

        StringBuilder builder = new StringBuilder();

        for (char c : value.toCharArray()) {
            builder.append(Character.digit(c, 10));
        }

        BigInteger bigInt = new BigInteger(builder.toString());

        return bigInt.longValueExact();
    }

}
