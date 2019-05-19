package net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll;

import io.vavr.control.Option;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.DocumentDb;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.MetadataDb;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueType;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.DocumentManager;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.MetadataManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public abstract class PollDocumentContract extends AbstractPollContract {

    @Autowired
    private DocumentManager documentManager;

    @Autowired
    private MetadataManager metadataManager;

    /**
     * COnstructor.
     *
     * @param key         the id.
     */
    protected PollDocumentContract(String key) {
        super(key, QueueType.document);
    }


    @Override
    public boolean run(QueueDb queueDb) throws Exception {
        validate(queueDb);
        final Option<DocumentDb> optDocument = documentManager.findById(queueDb.getUuid());
        final Option<MetadataDb> optMetadata = optDocument
                .filter(d -> Objects.nonNull(d.getMetadataId()))
                .flatMap(d -> metadataManager.findById(d.getMetadataId()));
        return execute(queueDb, optDocument, optMetadata);
    }


    protected abstract boolean execute(QueueDb queueDb, Option<DocumentDb> optDocument, Option<MetadataDb> optMetadata) throws Exception;

    public DocumentManager getDocumentManager() {
        return documentManager;
    }

    public void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    public MetadataManager getMetadataManager() {
        return metadataManager;
    }

    public void setMetadataManager(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }
}
