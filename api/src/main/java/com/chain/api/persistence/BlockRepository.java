package com.chain.api.persistence;

import com.chain.api.core.Block.Block;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BlockRepository extends ReactiveCrudRepository<Block, String> {
    Mono<Block> findByBlockId(int blockId);
}
