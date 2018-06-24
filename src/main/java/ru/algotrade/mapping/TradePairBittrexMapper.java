package ru.algotrade.mapping;

import org.mapstruct.Mapper;
import ru.algotrade.model.TradePair;

@Mapper(componentModel = "spring")
public interface TradePairBittrexMapper {

    TradePair toTradePair();
}
