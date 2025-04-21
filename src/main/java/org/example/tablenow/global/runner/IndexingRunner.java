package org.example.tablenow.global.runner;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.service.StoreBulkIndexer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IndexingRunner implements CommandLineRunner {

    private final StoreBulkIndexer storeBulkIndexer;

    @Override
    public void run(String... args) throws Exception {
        storeBulkIndexer.bulkIndex();
    }
}
