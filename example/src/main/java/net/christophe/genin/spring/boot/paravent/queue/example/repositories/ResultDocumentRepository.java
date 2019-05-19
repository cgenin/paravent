package net.christophe.genin.spring.boot.paravent.queue.example.repositories;

import net.christophe.genin.spring.boot.paravent.queue.example.model.ResultDocument;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResultDocumentRepository extends CrudRepository<ResultDocument, UUID> {


}
