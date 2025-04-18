package edu.neu.ccs.prl.zeugma.internal.runtime.model;

/**
 * Immutable identifier for a Java class.
 */
public final class ClassIdentifier {
    /**
     * Fully qualified name of the class (as returned by {@link java.lang.Class#getName()}) with each '.'  replaced by a
     * '/' (e.g., {@code "java/util/List"}).
     * <p>
     * Non-null.
     */
    private final String name;
    /**
     * CRC-64 checksum of the class file for the class.
     */
    private final long checksum;

    public ClassIdentifier(String name, long checksum) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.checksum = checksum;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (int) (checksum ^ (checksum >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof ClassIdentifier)) {
            return false;
        }
        ClassIdentifier that = (ClassIdentifier) o;
        if (checksum != that.checksum) {
            return false;
        }
        return name.equals(that.name);
    }

    @Override
    public String toString() {
        return "ClassIdentifier{" + name + "', " + checksum + '}';
    }

    public String getName() {
        return name;
    }

    public long getChecksum() {
        return checksum;
    }
}

