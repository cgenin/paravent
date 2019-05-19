package net.christophe.genin.spring.boot.paravent.queue.core.rest.api;

import net.christophe.genin.spring.boot.paravent.queue.core.ParaventQueueManager;
import net.christophe.genin.spring.boot.paravent.queue.core.rest.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Default enpoint to put datas in the queue.
 */
@RestController
public class Save {
    private static final Logger LOGGER = LoggerFactory.getLogger(Save.class);

    private final ParaventQueueManager manager;

    @Autowired
    public Save(ParaventQueueManager manager) {
        this.manager = manager;
    }

    @RequestMapping(value = "/api/paravent/queue/metadata/{key}", method = {RequestMethod.POST})
    public Result saveMetadata(@PathVariable("key") String key, @RequestBody Map<String, Object> body) {
        LOGGER.debug("saveMetadata for " + key + " / " + body);
        return new Result(manager.saveMetadata(key, body));
    }

    @RequestMapping(value = "/api/paravent/queue/document/{key}", method = {RequestMethod.POST}, consumes = "multipart/form-data")
    public Result saveDocumentPdf(@PathVariable("key") String key,
                                  @RequestPart(name = "filename", required = false) String filename,
                                  @RequestPart(name = "metadata", required = false) String metadata,
                                  @RequestPart(name = "document") @Valid @NotNull @NotBlank MultipartFile document) {
        LOGGER.debug("save document for " + key + " / " + filename);
        return new Result(manager.saveDocument(key, filename, metadata, document));

    }
}
