package com.chain.api.core.Net;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
public class MiningTask {
    Thread thread;
    CreateBlockThread createBlockThread;
}
