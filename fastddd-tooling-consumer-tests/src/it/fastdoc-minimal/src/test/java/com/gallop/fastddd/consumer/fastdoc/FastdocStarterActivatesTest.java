package com.gallop.fastddd.consumer.fastdoc;

import com.gallop.fastddd.fastdoc.FastDocService;
import com.gallop.fastddd.fastdoc.web.FastDocController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = FastdocMinimalApp.class)
class FastdocStarterActivatesTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    FastDocService fastDocService;

    @Autowired
    FastDocController fastDocController;

    @Test
    void fastDocBeansArePresent() {
        assertNotNull(applicationContext);
        assertNotNull(fastDocService);
        assertNotNull(fastDocController);
    }
}
