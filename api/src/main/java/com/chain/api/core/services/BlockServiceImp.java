package com.chain.api.core.services;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BlockServiceImp implements BlockService {

    private List<Block> blockchain;

    @Autowired
    public void setBlockchain(List<Block> blockchain) {
        this.blockchain = blockchain;
    }

    @Override
    public ResponseEntity<?> getBlock(int blockIndex) {
        return null;
    }

    @Override
    public ResponseEntity<?> addBlock(Block body) {
        return null;
    }

    @Override
    public ResponseEntity<?> getBlocks() {
        return new ResponseEntity<>(blockchain, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> startMine() {
        return null;
    }
}
