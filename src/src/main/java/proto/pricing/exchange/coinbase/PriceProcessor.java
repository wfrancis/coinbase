package proto.pricing.exchange.coinbase;

import com.lmax.disruptor.BusySpinWaitStrategy;
import proto.common.Publisher;
import proto.common.Subscriber;
import proto.common.TickEvent;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import proto.common.backtest.DataRecorder;
import proto.pricing.exchange.coinbase.schemas.CoinbaseSnapshot;
import proto.pricing.exchange.coinbase.schemas.CoinbaseL2TickUpdate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * The price processor subscribes to listen to the coinbase websocket data coming from the publisher,
 * then republishes to the printer/display/data recorder processor(s)
 */
public class PriceProcessor implements Subscriber<TickEvent<String>>, Publisher<String> {

    // TODO: make configurable
    private static final Integer LIMIT_BOOK = 1;

    public static String[] asksStr = new String[LIMIT_BOOK];
    public static String[] bidsStr = new String[LIMIT_BOOK];

    private final Disruptor<TickEvent> disruptor;

    private static final ConcurrentSkipListMap<Double, Double> bidBookSnapshot = new ConcurrentSkipListMap<>((o1, o2) -> Double.compare(o2, o1));
    private static final ConcurrentSkipListMap<Double, Double> askBookSnapshot = new ConcurrentSkipListMap<>((o1, o2) -> Double.compare(o1, o2));

    public PriceProcessor() {
        final PrinterProcessor orderBookPrinterProcessor = new PrinterProcessor();
        final DataRecorder     orderbookRecorder         = new DataRecorder("orderbookOutput.dat");
        EventHandler[] eventHandlers = {orderBookPrinterProcessor, orderbookRecorder};
        this.disruptor = new Disruptor<>(
                TickEvent.EVENT_FACTORY,
                1024,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE, new BusySpinWaitStrategy());
        this.disruptor.handleEventsWith(eventHandlers);
    }

    /**
     * process order book snapshot
     * @param snapshot¡
     */
    public void processSnapshot(CoinbaseSnapshot snapshot) {
        String[][] bids = snapshot.bids;
        String[][] asks = snapshot.asks;

        for (String[] bid : bids) {
            bidBookSnapshot.put(Double.parseDouble(bid[0]), Double.parseDouble(bid[1]));
        }

        for (String[] ask : asks) {
            askBookSnapshot.put(Double.parseDouble(ask[0]), Double.parseDouble(ask[1]));
        }

    }

    /**
     * process L2 update tick
     * @param update
     */
    public void processL2Update(CoinbaseL2TickUpdate update) {
        String[][] l2Update = update.changes;
        String side = l2Update[0][0];
        double price = Double.parseDouble(l2Update[0][1]);
        double size = Double.parseDouble(l2Update[0][2]);
        if ("sell".equals(side)) {
            if (Double.compare(size, 0.0) == 0) {
                askBookSnapshot.remove(price);
            } else {
                askBookSnapshot.put(price, size);
            }
        } else {
            if (Double.compare(size, 0.0) == 0) {
                bidBookSnapshot.remove(price);
            } else {
                bidBookSnapshot.put(price, size);
            }
        }
    }

    /**
     * process incoming data from coinbase
     * @param event
     * @param sequence
     * @param endOfBatch
     * @throws Exception
     */
    @Override
    public void onEvent(TickEvent event, long sequence, boolean endOfBatch) throws Exception {
        String message = (String) event.getValue();
        if("TEST!!!".equals(message)) {
            System.out.println(message);
            return;
        }
        Any objectFromJsonString = JsonIterator.deserialize(message);
        if ("snapshot".equals(objectFromJsonString.get("type").toString())) {
            processSnapshot(JsonIterator.deserialize(message, CoinbaseSnapshot.class));
            processPriceData();
        } else if ("l2update".equals(objectFromJsonString.get("type").toString())) {
            processL2Update(JsonIterator.deserialize(message, CoinbaseL2TickUpdate.class));
            processPriceData();
        }
    }

    /**
     * process data and republish to listeners
     */
    public void processPriceData() {
        int depth = LIMIT_BOOK;
        int i = 0;
        List<Double> b = new ArrayList<>();
        for (Map.Entry<Double, Double> element : bidBookSnapshot.entrySet()) {
            b.add(element.getKey());
            bidsStr[i] = element.getKey() + "@" + element.getValue();
            i++;
            if (i >= depth) {
                break;
            }
        }
        List<Double> a = new ArrayList<>();
        i = 0;
        for (Map.Entry<Double, Double> element : askBookSnapshot.entrySet()) {
            a.add(element.getKey());
            asksStr[i] = element.getKey() + "@" + element.getValue();
            i++;
            if (i >= depth) {
                break;
            }
        }

        String msg = "asks: " + Arrays.toString(asksStr) + "\tbids:" + Arrays.toString(bidsStr);
        publish(msg);
    }

    @Override
    public void start() {
        disruptor.start();
    }

    @Override
    public void shutdown() {
        disruptor.shutdown();
    }

    /**
     * publish orderbook data to listeners
     * @param data
     */
    @Override
    public void publish(String data) {
        final long seq = disruptor.getRingBuffer().next();
        final TickEvent valueEvent = disruptor.getRingBuffer().get(seq);
        valueEvent.setValue(data);
        disruptor.getRingBuffer().publish(seq);
    }
}
