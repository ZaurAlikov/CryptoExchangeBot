package ru.algotrade.mapping;

import de.elbatya.cryptocoins.bittrexclient.api.model.publicapi.Market;
import de.elbatya.cryptocoins.bittrexclient.api.model.publicapi.MarketSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.algotrade.model.TradePair;

@Mapper(componentModel = "spring")
public interface TradePairBittrexMapper {
    @Mappings({
            @Mapping(target = "symbol", source = "market.marketName"),
            @Mapping(target = "baseAsset", source = "market.baseCurrency"),
            @Mapping(target = "quoteAsset", source = "market.marketCurrency"),
            @Mapping(target = "askPrice", source = "marketSummary.ask"),
            @Mapping(target = "askQty", source = "marketSummary.baseVolume"),
            @Mapping(target = "bidPrice", source = "marketSummary.bid"),
            @Mapping(target = "bidQty", source = "marketSummary.volume"),
            @Mapping(target = "marketPrice", source = "marketSummary.last"),
            @Mapping(target = "tradeLimits", expression = "java(new ru.algotrade.model.TradeLimits(" +
                    "market.marketName," +
                    "symbolInfo.getStatus().name()," +
                    "symbolInfo.getFilters().get(0).getMinPrice()," +
                    "market.minTradeSize," +
                    "symbolInfo.getFilters().get(0).getTickSize()," +
                    "symbolInfo.getFilters().get(1).getMinQty()," +
                    "symbolInfo.getFilters().get(1).getMaxQty()," +
                    "symbolInfo.getFilters().get(1).getStepSize()," +
                    "symbolInfo.getFilters().get(2).getMinNotional()))")
    })
    TradePair toTradePair(Market market, MarketSummary marketSummary);
}
