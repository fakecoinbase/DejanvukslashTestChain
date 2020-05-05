package com.chain.api.core.Transaction;

import com.chain.api.core.Crypto.CryptoUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.security.PublicKey;

@Mapper(componentModel = "spring")
public interface TransactionOutputMapper {
    @Mappings({
            @Mapping(source = "to", target = "to", qualifiedByName = "publicKeyOutputToString")
    })
    TransactionOutputResponse transactionOutputToTransactionOutputResponse(TransactionOutput transactionOutput);

    @Named("publicKeyOutputToString")
    public static String publicKeyToString(PublicKey key) {
        if(key != null) {
            return CryptoUtil.getStringFromKey(key);
        }
        else return "";
    }
}
