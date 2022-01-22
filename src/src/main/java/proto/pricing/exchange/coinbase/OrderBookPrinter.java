package proto.pricing.exchange.coinbase;

import com.lmax.disruptor.EventHandler;
import proto.common.Publisher;
import proto.common.TickEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import proto.common.backtest.DataRecorder;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.Executors;

/**
 * OrderBookPrinter connects to Coinbase Pro Websocket then publishes data to listeners
 */
public class OrderBookPrinter implements Publisher<String> {

    private static final String SERVER = "wss://ws-feed.exchange.coinbase.com";

    private final Disruptor<TickEvent> disruptor;
    private WebSocketClient wsc;
    private final PriceProcessor priceProcessor;
    private final DataRecorder exchangePriceRecorder;

    private final String instrument;

    public OrderBookPrinter(String instrument) {
        this.priceProcessor        = new PriceProcessor();
        this.exchangePriceRecorder = new DataRecorder("exchangeInput.dat");
        this.instrument            = instrument;

        EventHandler[] eventHandlers = {priceProcessor, exchangePriceRecorder};
        this.disruptor = new Disruptor<>(
                TickEvent.EVENT_FACTORY,
                1024,
                Executors.defaultThreadFactory(),
                ProducerType.SINGLE, new BusySpinWaitStrategy());
        this.disruptor.handleEventsWith(eventHandlers);
    }

    @Override
    public void start() {
        this.priceProcessor.start();
        this.disruptor.start();
        try {
            this.wsc = new WebSocketClient(new URI(SERVER)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    try {
                        send(generateL2SubscriptionMessage(instrument));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMessage(String message) {
                    publish(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Connection closed. Reason: " + reason);
                    shutdown();
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            wsc.connect();
        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }
    }

    @Override
    public void shutdown() {
        this.wsc.close();
        this.priceProcessor.shutdown();
        this.disruptor.shutdown();
    }

    @Override
    public void publish(String message) {
        final long seq = disruptor.getRingBuffer().next();
        final TickEvent tickEvent = disruptor.getRingBuffer().get(seq);
        tickEvent.setValue(message);
        disruptor.getRingBuffer().publish(seq);
    }

    /**
     * Build Coinbase Pro WebSocket L2 API request.
     */
    private String generateL2SubscriptionMessage(String instrument) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode subscription = mapper.createObjectNode();
        subscription.put("type", "subscribe");
        ArrayNode productIds = mapper.createArrayNode();
        productIds.add(instrument);
        subscription.putIfAbsent("product_ids", productIds);
        ObjectNode channelValues = mapper.createObjectNode();
        channelValues.put("name", "ticker");
        channelValues.putIfAbsent("product_ids", productIds);
        ArrayNode channels = mapper.createArrayNode();
        channels.add("level2");
        channels.add(channelValues);
        subscription.putIfAbsent("channels", channels);
        return mapper.writer().writeValueAsString(subscription);
    }
}