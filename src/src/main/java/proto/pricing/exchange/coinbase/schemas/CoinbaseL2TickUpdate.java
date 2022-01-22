package proto.pricing.exchange.coinbase.schemas;

import java.util.Arrays;

/**
 *  Coinbase L2 tick update
 */
public class CoinbaseL2TickUpdate {
    public String type;
    public String product_id;
    public String[][] changes;
    public String time;

    @Override
    public String toString() {
        return "CoinbaseL2TickUpdate{" +
                "type='" + type + '\'' +
                ", product_id='" + product_id + '\'' +
                ", changes=" + Arrays.toString(changes) +
                ", time='" + time + '\'' +
                '}';
    }
}
