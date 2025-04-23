package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public final class ModifierUtil {
    private ModifierUtil() {
        throw new AssertionError();
    }

    /**
     * Returns a list created by replacing a range of values in the specified parent list starting at the specified
     * position and of the specified length. The values in the specified range are replaced with the specified
     * replacement values. Does not modify the specified parent list.
     *
     * @param parent       the parent list
     * @param position     the position at which values are to be inserted
     * @param replacements the values to be used to replace the original values
     * @return list created by replacing the specified range of values in the specified parent list with the specified
     * replacement values
     * @throws NullPointerException     if {@code parent == null} or {@code replacements == null}
     * @throws IllegalArgumentException if {@code position < 0}, {@code length < 0} or
     *                                  {@code position + length > parent.size()}
     */
    public static ByteList replaceRange(ByteList parent, int position, int length, ByteList replacements) {
        if (parent == null || replacements == null) {
            throw new NullPointerException();
        }
        if (position < 0 || length < 0 || position + length > parent.size()) {
            throw new IllegalArgumentException();
        }
        return parent.subList(0, position)
                .concatenate(replacements)
                .concatenate(parent.subList(position + length, parent.size()));
    }
}
