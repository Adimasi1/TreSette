package view.game;

import controller.ViewEvent.*;
import model.events.DealSnapshot;

/** Utility to extract a DealSnapshot from supported view events (null if none). */
final class SnapshotUtil {
    private SnapshotUtil() {}
    static DealSnapshot extract(Object event){
        if(event instanceof DealStarted ds) return ds.snapshot();
        if(event instanceof TrickStarted ts) return ts.snapshot();
        if(event instanceof CardPlayed cp) return cp.snapshot();
        if(event instanceof TrickEnded te) return te.snapshot();
        if(event instanceof DealEnded de) return de.snapshot();
        if(event instanceof ScoresUpdated su) return su.snapshot();
        if(event instanceof SignMade sm) return sm.snapshot();
        return null;
    }
}
