package proto;

import proto.pricing.exchange.coinbase.OrderBookPrinter;

/**
 * App Main
 */
public class App {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Please, provide an instrument name to query. Exiting...");
            System.exit(1);
        }
        else {
            System.out.printf("Streaming order book for instrument: [%s], press Ctrl-C to quit.%n", args[0]);
        }

        final OrderBookPrinter orderBookPrinter = new OrderBookPrinter(args[0]);
        orderBookPrinter.start();

        /**
         * Callback for Control-c
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                orderBookPrinter.shutdown();
                System.out.println("Shutdown complete...");
            } catch (Exception ignored) {}
        }));
    }
}
