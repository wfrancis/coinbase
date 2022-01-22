package proto.common.utils;

import java.util.List;

public class PriceUtils {

    public double mid(List<Double> bids, List<Double> asks) {
        if(bids == null || asks == null || bids.isEmpty() || asks.isEmpty()) {
            return Double.NaN;
        } else {
            return (bids.get(0) + asks.get(0)) / 2.0;
        }
    }


}
