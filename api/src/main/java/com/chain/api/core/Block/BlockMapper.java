package com.chain.api.core.Block;

import com.chain.api.core.Transaction.TransactionMapper;
import com.chain.api.core.Transaction.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = { TransactionMapper.class })
public interface BlockMapper {
    @Mappings({
            @Mapping(source = "transactions", target = "transactions")
    })
    BlockResponse blockToBlockResponse(Block block);
}
