package com.thelastcodebenders.follower.client.rate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrencyRatesItem {
    private String name;
    private String code;
    private double todayLowestBuyPrice;
    private double todayHighestBuyPrice;
    private double todayLowestSellPrice;
    private double todayHighestSellPrice;
    private double yesterdayClosingBuyPrice;
    private double yesterdayClosingSellPrice;
    private double buyPrice;
    private double sellPrice;
    private double dailyChange;
    private double dailyChangePercentage;
    private String lastUpdateDate;
}
