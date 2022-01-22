package proto.pricing.exchange.coinbase.schemas;

import java.util.Arrays;

/**
 * Coinbase order-book snapshot
 */
public class CoinbaseSnapshot {
    public String type;
    public String product_id;
    public String[][] asks;
    public String[][] bids;

    @Override
    public String toString() {
        return "CoinbaseSnapshot{" +
                "type='" + type + '\'' +
                ", product_id='" + product_id + '\'' +
                ", asks=" + Arrays.toString(asks) +
                ", bids=" + Arrays.toString(bids) +
                '}';
    }
}
