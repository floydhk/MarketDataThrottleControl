import domain.MarketData;

public interface IMessageListener {

    void onMessage(MarketData data);

    void publishAggregatedMarketData(Object data);

}
