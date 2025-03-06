package com.safetypin.authentication.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AsyncConfigTest {

    @Autowired
    private Executor emailTaskExecutor;

    @Test
    void testEmailTaskExecutorConfiguration() {
        // Verify that emailTaskExecutor is not null
        assertNotNull(emailTaskExecutor, "Email task executor should not be null");

        // Verify that it's a ThreadPoolTaskExecutor instance
        assertInstanceOf(ThreadPoolTaskExecutor.class, emailTaskExecutor, "Executor should be an instance of ThreadPoolTaskExecutor");

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) emailTaskExecutor;

        // Test core pool size
        assertEquals(2, executor.getCorePoolSize(),
                "Core pool size should be 2");

        // Test max pool size
        assertEquals(5, executor.getMaxPoolSize(),
                "Max pool size should be 5");

        // Test queue capacity
        assertEquals(100, executor.getQueueCapacity(),
                "Queue capacity should be 100");

        // Get the actual ThreadPoolExecutor and verify it's initialized
        ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();
        assertNotNull(threadPoolExecutor, "ThreadPoolExecutor should be initialized");

        // Verify thread prefix naming indirectly through a newly created thread
        executor.execute(() -> {
            String currentThreadName = Thread.currentThread().getName();
            assertTrue(currentThreadName.startsWith("EmailThread-"),
                    "Thread name should start with 'EmailThread-'");
        });
    }
}
