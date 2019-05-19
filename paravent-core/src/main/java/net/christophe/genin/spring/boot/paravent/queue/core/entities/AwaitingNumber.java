package net.christophe.genin.spring.boot.paravent.queue.core.entities;

import java.util.Objects;

public class AwaitingNumber {

    private String key;
    private Long nb;

    public AwaitingNumber(String key, Long nb) {
        this.key = key;
        this.nb = nb;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getNb() {
        return nb;
    }

    public void setNb(Long nb) {
        this.nb = nb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AwaitingNumber that = (AwaitingNumber) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(nb, that.nb);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, nb);
    }
}
