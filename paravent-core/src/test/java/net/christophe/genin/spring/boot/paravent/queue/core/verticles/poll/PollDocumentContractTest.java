package net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll;


import io.vavr.control.Option;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.DocumentDb;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.MetadataDb;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueType;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.DocumentManager;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.MetadataManager;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PollDocumentContractTest {

    private QueueDb queueDb;
    private DocumentManager documentManager;
    private MetadataManager metadataManager;

    @Before
    public void before() {
        queueDb = new QueueDb("uuid", new Date().getTime(), QueueType.document);
        documentManager = mock(DocumentManager.class);
        metadataManager = mock(MetadataManager.class);
    }

    @Test
    public void should_with_empty() throws Exception {
        test(Option.none(), Option.none());
    }

    @Test
    public void should_with_one_doc() throws Exception {
        final Option<DocumentDb> optDocument = Option.of(new DocumentDb());
        final Option<MetadataDb> optMetadata = Option.none();
        test(optDocument, optMetadata);
    }

    @Test
    public void should_with_all() throws Exception {
        final DocumentDb value = new DocumentDb();
        value.setMetadataId(queueDb.getUuid());
        final Option<DocumentDb> optDocument = Option.of(value);
        final Option<MetadataDb> optMetadata = Option.of(new MetadataDb());
        test(optDocument, optMetadata);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_be_incorrect() throws Exception {
        queueDb.setType(QueueType.metadata);
        final DocBean docBean = new DocBean(queueDb, Option.none(), Option.none());
        docBean.setDocumentManager(documentManager);
        docBean.setMetadataManager(metadataManager);
        docBean.run(queueDb);

    }

    private void test(Option<DocumentDb> optDocument, Option<MetadataDb> optMetadata) throws Exception {
        final DocBean docBean = new DocBean(queueDb, optDocument, optMetadata);
        docBean.setDocumentManager(documentManager);
        docBean.setMetadataManager(metadataManager);

        when(documentManager.findById(queueDb.getUuid())).thenReturn(optDocument);
        when(metadataManager.findById(queueDb.getUuid())).thenReturn(optMetadata);
        assertThat(docBean.run(queueDb)).isTrue();
    }


    private static class DocBean extends PollDocumentContract {


        private final QueueDb queueDb;
        private final Option<DocumentDb> optDocument;
        private final Option<MetadataDb> optMetadata;

        private DocBean(QueueDb queueDb, Option<DocumentDb> optDocument, Option<MetadataDb> optMetadata) {
            super("test");
            this.queueDb = queueDb;
            this.optDocument = optDocument;
            this.optMetadata = optMetadata;
        }

        @Override
        protected boolean execute(QueueDb queueDb, Option<DocumentDb> optDocument, Option<MetadataDb> optMetadata) {
            assertThat(queueDb).isEqualTo(this.queueDb);
            assertThat(optDocument.isEmpty()).isEqualTo(this.optDocument.isEmpty());
            assertThat(optMetadata.isEmpty()).isEqualTo(this.optMetadata.isEmpty());
            return true;
        }
    }
}
