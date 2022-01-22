package proto.common.backtest;

import proto.common.Subscriber;
import proto.common.TickEvent;

/**
 * record data for analysis/replay
 */
public class DataRecorder implements Subscriber<TickEvent> {

    public DataRecorder(String file) {
        // recorder could be either output to DB/File/ect
    }

    public void shutdown()  {
    }

    @Override
    public void onEvent(TickEvent event, long sequence, boolean endOfBatch) {
        // record
    }
}
