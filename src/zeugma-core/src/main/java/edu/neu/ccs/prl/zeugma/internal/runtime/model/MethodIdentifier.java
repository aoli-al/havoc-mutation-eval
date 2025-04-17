package edu.neu.ccs.prl.zeugma.internal.runtime.model;

/**
 * Immutable identifier for a Java method.
 */
public final class MethodIdentifier {
    /**
     * Identifier for the class that declared this method.
     * <p>
     * Non-null.
     */
    private final ClassIdentifier owner;
    /**
     * Name of this method.
     * <p>
     * Non-null.
     */
    private final String name;
    /**
     * Descriptor of this method.
     * <p>
     * Non-null.
     */
    private final String descriptor;

    public MethodIdentifier(ClassIdentifier owner, String name, String desc) {
        if (owner == null || name == null || desc == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.descriptor = desc;
        this.owner = owner;
    }

    public ClassIdentifier getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public int hashCode() {
        int result = owner.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + descriptor.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof MethodIdentifier)) {
            return false;
        }
        MethodIdentifier that = (MethodIdentifier) o;
        if (!owner.equals(that.owner)) {
            return false;
        } else if (!name.equals(that.name)) {
            return false;
        }
        return descriptor.equals(that.descriptor);
    }

    @Override
    public String toString() {
        return owner.getName() + "#" + name + descriptor;
    }
}
