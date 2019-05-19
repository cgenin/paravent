package net.christophe.genin.spring.boot.paravent.queue.example;

import io.vavr.control.Option;
import io.vertx.core.json.JsonObject;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll.PollMetadataContract;
import net.christophe.genin.spring.boot.paravent.queue.example.model.ResultMetadata;
import net.christophe.genin.spring.boot.paravent.queue.example.repositories.ResultMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestMetadataConsumer extends PollMetadataContract {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestMetadataConsumer.class);

    @Autowired
    private ResultMetadataRepository resultMetadataRepository;

    public TestMetadataConsumer() {
        super("test");
    }

    @Transactional
    @Override
    protected boolean execute(QueueDb queueDb, Option<JsonObject> optMetadata) throws Exception {
        // Thread.sleep((long) (Math.random() * 200));
        final ResultMetadata resultMetadata = new ResultMetadata();
        resultMetadata.setId(queueDb.getUuid());
        resultMetadata.setJs(optMetadata.getOrElse(new JsonObject()));


        //  LOGGER.info("Consume " + queueDb);
        return resultMetadataRepository.findById(resultMetadata.getId())
                .map(obj -> false)
                .orElseGet(() -> {
                    resultMetadataRepository.save(resultMetadata);
                    return true;
                });
    }
}
