import domain.MarketData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import java.time.Duration;


public class MarketDataProcessor implements IMessageListener {

    private FluxSink<Object> handler;

    public MarketDataProcessor() {

        Flux.create(emitter -> { handler= emitter; },FluxSink.OverflowStrategy.DROP)
                .window(Duration.ofMillis(1000))   // 1 second
                .subscribe(flux -> flux.take(100)  // take first 100 msg
                        .distinct(x -> ((MarketData)x).getSymbol())
                        .subscribe(this::publishAggregatedMarketData));


    }


    @Override
    public void onMessage(MarketData data) {

        System.out.println("onMessage incoming data="+ data);

        handler.next(data);
    }

    @Override
    public void publishAggregatedMarketData(Object data) {

        System.out.println("publish AggregatedMarketData data="+  data);
    }


}
