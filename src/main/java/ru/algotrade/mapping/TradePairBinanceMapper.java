package ru.algotrade.mapping;

import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.domain.market.TickerPrice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.algotrade.model.TradePair;

@Mapper(componentModel = "spring")
public interface TradePairBinanceMapper {
    @Mappings({
            @Mapping(target = "symbol", source = "symbolInfo.symbol"),
            @Mapping(target = "baseAsset", source = "symbolInfo.baseAsset"),
            @Mapping(target = "quoteAsset", source = "symbolInfo.quoteAsset"),
            @Mapping(target = "askPrice", source = "bookTicker.askPrice"),
            @Mapping(target = "askQty", source = "bookTicker.askQty"),
            @Mapping(target = "bidPrice", source = "bookTicker.bidPrice"),
            @Mapping(target = "bidQty", source = "bookTicker.bidQty"),
            @Mapping(target = "marketPrice", source = "tickerPrice.price"),
            @Mapping(target = "tradeLimits", expression = "java(new ru.algotrade.model.TradeLimits(" +
                    "symbolInfo.getSymbol()," +
                    "symbolInfo.getFilters().get(0).getMinPrice()," +
                    "symbolInfo.getFilters().get(0).getMaxPrice()," +
                    "symbolInfo.getFilters().get(0).getTickSize()," +
                    "symbolInfo.getFilters().get(1).getMinQty()," +
                    "symbolInfo.getFilters().get(1).getMaxQty()," +
                    "symbolInfo.getFilters().get(1).getStepSize()," +
                    "symbolInfo.getFilters().get(2).getMinNotional()))")
    })
    TradePair toTradePair(SymbolInfo symbolInfo, TickerPrice tickerPrice, BookTicker bookTicker);
}
