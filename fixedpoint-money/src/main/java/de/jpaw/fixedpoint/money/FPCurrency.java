package de.jpaw.fixedpoint.money;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import de.jpaw.api.iso.CurrencyData;
import de.jpaw.fixedpoint.FixedPointBase;
import de.jpaw.fixedpoint.FixedPointSelector;
import de.jpaw.fixedpoint.types.MicroUnits;

/** Class to store the notion of a currency, with the option to override the number of decimals (fractional digits).
 * By default, the number of decimals corresponds to the one of the real currency as defined by ISO 4217.
 * Instances of this class are immutable.
 */
public final class FPCurrency implements Serializable {
    private static final long serialVersionUID = -626929186120783201L;
    
    /** A cache for frequently used precisions, to avoid GC due to frequently created objects... */
    private final static ConcurrentHashMap<CurrencyData, FPCurrency> precisionCacheStd = new ConcurrentHashMap<CurrencyData, FPCurrency>(64);
    private final static ConcurrentHashMap<CurrencyData, FPCurrency> precisionCacheMicros = new ConcurrentHashMap<CurrencyData, FPCurrency>(64);

    /** The currency's feature provider. */
    private final CurrencyData currency;
    
    /** The numeric reference object, of value 0. */
    private final FixedPointBase<?> zero;

    /** The result of the toString() operation, after first execution. */
    private transient String asString = null;

    /** Constructs a new FPCurreny instance for a given currency and precision / storage type. */
    public FPCurrency(CurrencyData currency, FixedPointBase<?> referenceType) {
        if (currency == null || referenceType == null)
            throw new NullPointerException("currency and reference type must be non-null");
        this.currency = currency;
        this.zero = referenceType.getZero();
    }

    /** Constructs a new FPCurreny instance for a given currency, selecting the precision / storage type by the currency. */
    public FPCurrency(CurrencyData currency) {
        if (currency == null)
            throw new NullPointerException("currency must be non-null");
        this.currency = currency;
        this.zero = FixedPointSelector.getZeroForScale(currency.getDefaultFractionDigits());
    }
    
    /** Constructs a new currency, for the same ISO code, but with default precision. May return the same object. */ 
    public FPCurrency withDefaultPrecision() {
        if (currency.getDefaultFractionDigits() == zero.getScale())
            return this;
        return stdPrecisionOf(currency);
    }

    /** Constructs a new currency, for the same ISO code, but with exactly 6 decimals precision. May return the same object. */ 
    public FPCurrency withMicrosPrecision() {
        if (currency.getDefaultFractionDigits() == 6)
            return this;
        return microsPrecisionOf(currency);
    }
    
    /** Returns a possibly cached instance for a currency. */
    public static FPCurrency stdPrecisionOf(CurrencyData currency) {
        FPCurrency result = precisionCacheStd.get(currency);
        if (result == null) {
            // no previously cached result, 
            result = new FPCurrency(currency);
            FPCurrency result2 = precisionCacheStd.putIfAbsent(currency, result);
            // accepted race condition: parallel creation of objects. However, all calls should return the same instance.
            if (result2 != null)
                result = result2;
        }
        return result;
    }

    /** Returns a possibly cached instance for a currency. */
    public static FPCurrency microsPrecisionOf(CurrencyData currency) {
        FPCurrency result = precisionCacheMicros.get(currency);
        if (result == null) {
            // no previously cached result, 
            result = new FPCurrency(currency, MicroUnits.ZERO);
            FPCurrency result2 = precisionCacheMicros.putIfAbsent(currency, result);
            // accepted race condition: parallel creation of objects. However, all calls should return the same instance.
            if (result2 != null)
                result = result2;
        }
        return result;
    }

    @Override
    public String toString() {
        if (asString == null) {
            // build a hashed string
            asString = (zero.getScale() == currency.getDefaultFractionDigits())
                    ? currency.getCurrencyCode()
                    : currency.getCurrencyCode() + ":" + zero.getScale();
        }
        return asString;
    }

    // default Eclipse autogenerated methods below

    /** Returns a FixedPointBase<?> type representing 0 in the currency's scale. */
    public FixedPointBase<?> getZero() {
        return zero;
    }
    
    public CurrencyData getCurrency() {
        return currency;
    }

    // derived getters for convenience
    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }

    public int getDecimals() {
        return zero.getScale();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return (prime * currency.getCurrencyCode().hashCode()) * prime + zero.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FPCurrency other = (FPCurrency) obj;
        // Equals should be avoided because we don't know the object behind. Just compare the currency codes.
        return currency.getCurrencyCode().equals(other.currency.getCurrencyCode()) && zero.equals(other.zero);
    }

}
