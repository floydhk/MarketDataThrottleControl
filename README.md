


Assumptions

1. There is only have one thread (i.e. one source) calling onMessage method at an unknown rate.
2. If the messages received over 100 times per second, only the first 100 messages would be processed, the rest message will be discarded.
3. In the same sliding window (1 second window), only 1 message will be processed for each symbol.
