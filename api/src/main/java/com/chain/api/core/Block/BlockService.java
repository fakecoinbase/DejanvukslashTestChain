package com.chain.api.core.Block;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface BlockService {
    /**
     * usage curl $HOST:$PORT/block/1
     * @param blockId
     * @return
     */
    @GetMapping(value = "/block/{blockId}", produces = "application/json")
    Mono<Block> getBlock(@PathVariable int blockId);

    @GetMapping(value = "/start")
    Mono<Block> startMine();
}
