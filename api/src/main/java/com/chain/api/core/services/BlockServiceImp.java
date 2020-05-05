package com.chain.api.core.services;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockMapper;
import com.chain.api.core.Block.BlockResponse;
import com.chain.api.core.Block.BlockService;
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

    @Autowired
    public void setBlockMapper(BlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }

    @Autowired
    public void setBlockchain(List<Block> blockchain) {
        this.blockchain = blockchain;
    }

    @Override
    public ResponseEntity<?> getBlock(String hash) {
        Block foundBlock = blockchain.stream().filter(block -> block.getHash().equals(hash)).findAny().orElse(null);

        return new ResponseEntity<>(blockMapper.blockToBlockResponse(foundBlock), HttpStatus.FOUND);
    }

    @Override
    public ResponseEntity<?> addBlock(Block body) {
        return null;
    }

    @Override
    public ResponseEntity<?> getBlocks() {
        List<BlockResponse> blockResponse = blockchain.stream().map(block -> blockMapper.blockToBlockResponse(block)).collect(Collectors.toList());
        return new ResponseEntity<>(blockResponse, HttpStatus.OK);
    }
}
