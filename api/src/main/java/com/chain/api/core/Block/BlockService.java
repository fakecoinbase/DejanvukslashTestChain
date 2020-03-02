package com.chain.api.core.Block;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BlockService {
    /**
     * usage curl $HOST:$PORT/block/1
     * @param blockIndex
     * @return
     */
    @GetMapping(value = "/block/{blockIndex}", produces = "application/json")
    Mono<Block> getBlock(@PathVariable int blockIndex);

    @PostMapping(value = "/block", consumes = "application/json", produces = "application/json")
    Block addBlock(@RequestBody Block body);

    @GetMapping(produces = "application/json")
    Flux<Block> getBlocks();

    @GetMapping(value = "/start")
    Mono<Block> startMine();
}
