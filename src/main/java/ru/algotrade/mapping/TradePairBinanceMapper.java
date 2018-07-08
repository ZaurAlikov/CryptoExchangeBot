package ru.algotrade.mapping;

import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.TickerPrice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.algotrade.model.Candle;
import ru.algotrade.model.TradePair;

import java.util.List;

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
            @Mapping(target = "candles", source = "candlesticks"),
            @Mapping(target = "tradeLimits", expression = "java(new ru.algotrade.model.TradeLimits(" +
                    "symbolInfo.getSymbol()," +
                    "symbolInfo.getStatus().name()," +
                    "symbolInfo.getFilters().get(0).getMinPrice()," +
                    "symbolInfo.getFilters().get(0).getMaxPrice()," +
                    "symbolInfo.getFilters().get(0).getTickSize()," +
                    "symbolInfo.getFilters().get(1).getMinQty()," +
                    "symbolInfo.getFilters().get(1).getMaxQty()," +
                    "symbolInfo.getFilters().get(1).getStepSize()," +
                    "symbolInfo.getFilters().get(2).getMinNotional()))")
    })
    TradePair toTradePair(SymbolInfo symbolInfo, TickerPrice tickerPrice, BookTicker bookTicker, List<Candlestick> candlesticks);

    @Mappings({
            @Mapping(target = "openTime", source = "candlestick.openTime"),
            @Mapping(target = "open", source = "candlestick.open"),
            @Mapping(target = "high", source = "candlestick.high"),
            @Mapping(target = "low", source = "candlestick.low"),
            @Mapping(target = "close", source = "candlestick.close"),
            @Mapping(target = "volume", source = "candlestick.volume"),
            @Mapping(target = "closeTime", source = "candlestick.closeTime"),
            @Mapping(target = "quoteAssetVolume", source = "candlestick.quoteAssetVolume"),
    })
    Candle toCandle(Candlestick candlestick);
}
