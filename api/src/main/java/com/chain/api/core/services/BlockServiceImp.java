package com.chain.api.core.services;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockMapper;
import com.chain.api.core.Block.BlockResponse;
import com.chain.api.core.Block.BlockService;
import com.chain.api.core.Transaction.TransactionUtil;
import com.chain.api.core.Transaction.UnconfirmedTransactions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class BlockServiceImp implements BlockService {

    private List<Block> blockchain;

    private BlockMapper blockMapper;

    private UnconfirmedTransactions unconfirmedTransactions;

    @Autowired
    public void setBlockMapper(BlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }

    @Autowired
    public void setBlockchain(List<Block> blockchain) {
        this.blockchain = blockchain;
    }

    @Autowired
    public void setUnconfirmedTransactions(UnconfirmedTransactions unconfirmedTransactions) { this.unconfirmedTransactions = unconfirmedTransactions; }

    @Override
    public ResponseEntity<?> getBlock(String hash) {
        Block foundBlock = blockchain.stream().filter(block -> block.getHash().equals(hash)).findAny().orElse(null);

        BlockResponse blockResponse = blockMapper.blockToBlockResponse(foundBlock);

        blockResponse.getTransactions().stream().forEach(tx -> {
            tx.setVerified(true); // transactions mined in a block are considered verified by default
            tx.setOwnerBlock(blockResponse.getHash());
        });

        return new ResponseEntity<>(blockResponse, HttpStatus.FOUND);
    }

    @Override
    public ResponseEntity<?> addBlock(Block body) {
        return null;
    }

    @Override
    public ResponseEntity<?> getBlocks() {
        List<BlockResponse> blocksResponse = blockchain.stream().map(block -> {
            BlockResponse blockResponse = blockMapper.blockToBlockResponse(block);

            blockResponse.getTransactions().stream().forEach(tx -> {
                tx.setVerified(true); // transactions mined in a block are considered verified by default
                tx.setOwnerBlock(blockResponse.getHash());
            });

            return blockResponse;

        }).collect(Collectors.toList());
        return new ResponseEntity<>(blocksResponse, HttpStatus.OK);
    }
}
