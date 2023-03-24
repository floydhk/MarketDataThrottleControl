package domain;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MarketData {

    private String symbol;
    private double bid;
    private double ask;
    private LocalDateTime updatedTime;
}
