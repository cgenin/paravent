package net.christophe.genin.spring.boot.paravent.queue.core.rest.dto;

import java.util.Objects;

public class Result {

    private String id;
    private Boolean created = true;

    public Result() {
    }

    public Result(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return Objects.equals(id, result.id) &&
                Objects.equals(created, result.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, created);
    }
}
