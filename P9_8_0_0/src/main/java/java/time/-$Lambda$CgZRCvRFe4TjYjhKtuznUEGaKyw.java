package java.time;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

final /* synthetic */ class -$Lambda$CgZRCvRFe4TjYjhKtuznUEGaKyw implements TemporalQuery {
    private final /* synthetic */ Object $m$0(TemporalAccessor arg0) {
        return Instant.from(arg0);
    }

    public final Object queryFrom(TemporalAccessor temporalAccessor) {
        return $m$0(temporalAccessor);
    }
}
