package com.chain.api.core.Block;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface BlockService {
    /**
     * usage curl $HOST:$PORT/block/1
     * @param hash
     * @return
     */
    @GetMapping(value = "/block/{hash}", produces = "application/json")
    ResponseEntity<?> getBlock(@PathVariable String hash);

    @PostMapping(value = "/block", consumes = "application/json", produces = "application/json")
    ResponseEntity<?> addBlock(@RequestBody Block body);

    @GetMapping(value = "/block" ,produces = "application/json")
    ResponseEntity<?> getBlocks();

}
