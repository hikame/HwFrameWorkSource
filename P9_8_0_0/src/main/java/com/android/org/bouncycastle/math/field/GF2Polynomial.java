package com.android.org.bouncycastle.math.field;

import com.android.org.bouncycastle.util.Arrays;

class GF2Polynomial implements Polynomial {
    protected final int[] exponents;

    GF2Polynomial(int[] exponents) {
        this.exponents = Arrays.clone(exponents);
    }

    public int getDegree() {
        return this.exponents[this.exponents.length - 1];
    }

    public int[] getExponentsPresent() {
        return Arrays.clone(this.exponents);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GF2Polynomial)) {
            return false;
        }
        return Arrays.areEqual(this.exponents, ((GF2Polynomial) obj).exponents);
    }

    public int hashCode() {
        return Arrays.hashCode(this.exponents);
    }
}
