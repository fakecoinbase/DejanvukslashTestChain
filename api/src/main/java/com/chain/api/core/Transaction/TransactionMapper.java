package com.chain.api.core.Transaction;


import com.chain.api.core.Crypto.CryptoUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.security.PublicKey;

@Mapper(componentModel = "spring", uses = { TransactionOutputMapper.class })
public interface TransactionMapper {
    @Mappings({
            @Mapping(source = "sender", target = "sender", qualifiedByName = "publicKeyToString"),
            @Mapping(source = "receiver", target = "receiver", qualifiedByName = "publicKeyToString"),
            @Mapping(source = "outputs", target = "outputs")
    })
    TransactionResponse transactionToTransactionResponse(Transaction transaction);

    @Named("publicKeyToString")
    public static String publicKeyToString(PublicKey key) {
        if(key != null) {
            return CryptoUtil.getStringFromKey(key);
        }
        else return "";
    }
}
