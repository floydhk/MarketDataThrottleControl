import domain.MarketData;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


class MarketDataProcessorTest {

    static List<String> symbolList = new ArrayList<String>();
    final int sleepTimeForEachSymbolRound = 101;

    @BeforeAll
    protected static void setUp() throws Exception{

         for (int i=1; i<=101; i++) {
            symbolList.add("S"+ i);
         }
    }

    //  Test only 100 times msg are allowed to publish the message per second.
    @ParameterizedTest()
    @CsvSource({"100, 10, 100","101,10,100"})
    void publishNotExceedThresholdPerSecond(int noOfSymbols, int noOfTimesForEachSymbol, int expectedTotalPubMsg) throws Exception {

        List<Object> publishedMsgList = new ArrayList<>();
        List<String> symbolList= MarketDataProcessorTest.symbolList.stream().limit(noOfSymbols).collect(Collectors.toList());

        IMessageListener marketDataProcessor = new MarketDataProcessor() {
            @Override
            public void publishAggregatedMarketData(Object data) {

                publishedMsgList.add(data);
                super.publishAggregatedMarketData(data);
            }
        };

        generateAndSendMarketData(marketDataProcessor, symbolList, noOfTimesForEachSymbol);

        Assert.assertEquals(expectedTotalPubMsg, publishedMsgList.size());
    }

    // Test published Message will not be generated more than 1 time per symbol per second and count the total no of published per symbol
    @ParameterizedTest()
    @CsvSource({"3,3,1", "3,12,2"})
    void publishNotMoreThanOnePerSymbolPerSecond(int noOfSymbols, int noOfMessageUpdateForEachSymbol, int expectedNoOfMessageUpdateForEachSymbol) throws Exception {

        Map<String, Integer> publishMsgCountMap = new HashMap<>();
        List<String> symbolList = MarketDataProcessorTest.symbolList.stream().limit(noOfSymbols).collect(Collectors.toList());


        IMessageListener marketDataProcessor = new MarketDataProcessor() {
            @Override
            public void publishAggregatedMarketData(Object data) {
                publishMsgCountMap.put(((MarketData)data).getSymbol(), publishMsgCountMap.getOrDefault(((MarketData)data).getSymbol(), 0) + 1);
                super.publishAggregatedMarketData(data);
            }
        };

        generateAndSendMarketData(marketDataProcessor, symbolList, noOfMessageUpdateForEachSymbol);

        symbolList.forEach(s -> {
            Assert.assertEquals(expectedNoOfMessageUpdateForEachSymbol, publishMsgCountMap.getOrDefault(s, 0).intValue());
        });
    }

    // Test only the latest data will be published per symbol per second and count the total no of update per symbol
    @ParameterizedTest()
    @CsvSource({"3,11"})
    void publishTheLatestDataPerSymbolPerSecond(int noOfSymbols, int noOfUpdateForEachSymbol) throws Exception {

        Map<String, Object> publishedMsgMap = new HashMap<>();
        List<String> symbolList = MarketDataProcessorTest.symbolList.stream().limit(noOfSymbols).collect(Collectors.toList());

        IMessageListener marketDataProcessor = new MarketDataProcessor() {
            @Override
            public void publishAggregatedMarketData(Object data) {

                publishedMsgMap.put(((MarketData)data).getSymbol(), data);
                super.publishAggregatedMarketData(data);
            }
        };

        generateAndSendMarketData(marketDataProcessor, symbolList, noOfUpdateForEachSymbol);

        symbolList.forEach(s -> {
            Assert.assertEquals(noOfUpdateForEachSymbol, ((MarketData)publishedMsgMap.get(s)).getBid(),0);
        });
    }

    private void generateAndSendMarketData(IMessageListener marketDataProcessor, List<String> symbolList, int noOfUpdatePerSymbol) throws Exception {

        for (int i = 1; i <= noOfUpdatePerSymbol; i++) {

            for (String symbol : symbolList) {
                MarketData marketData=MarketData.builder().symbol(symbol).bid(i).ask(i).updatedTime(LocalDateTime.now()).build();
                marketDataProcessor.onMessage(marketData);
            }

            Thread.sleep(sleepTimeForEachSymbolRound);
        }
    }


}