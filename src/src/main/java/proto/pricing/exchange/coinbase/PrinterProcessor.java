package proto.pricing.exchange.coinbase;

import proto.common.Subscriber;
import proto.common.TickEvent;

/**
 * Subscriber which prints tick events to standard out
 */
public class PrinterProcessor implements Subscriber<TickEvent> {

    // TODO: handle slow consumer case
    @Override
    public void onEvent(TickEvent event, long sequence, boolean endOfBatch) {
        System.out.println(event.getValue());
    }
}
