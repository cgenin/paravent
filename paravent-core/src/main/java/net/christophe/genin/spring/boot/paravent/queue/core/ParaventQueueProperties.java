package net.christophe.genin.spring.boot.paravent.queue.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "paravent.queue")
@Validated
public class ParaventQueueProperties {


    @NotNull
    private Poll poll = new Poll();

    @Valid
    private Vacuum vacuum = new Vacuum();

    @Valid
    private Instances instances = new Instances();

    @Valid
    private Rest rest = new Rest();

    private Jpa jpa = new Jpa();

    public Instances getInstances() {
        return instances;
    }


    public void setInstances(Instances instances) {
        this.instances = instances;
    }

    public Rest getRest() {
        return rest;
    }

    public void setRest(Rest rest) {
        this.rest = rest;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public Vacuum getVacuum() {
        return vacuum;
    }

    public void setVacuum(Vacuum vacuum) {
        this.vacuum = vacuum;
    }

    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(Jpa jpa) {
        this.jpa = jpa;
    }

    public static class Poll {

        @NotNull
        @Min(1)
        private Integer defaultBatch = 10;

        @NotNull
        @Min(500)
        private Long defaultTime = 5000L;

        private Map<String, Long> timeByKey = new HashMap<>();

        private Map<String, Integer> batchByKey = new HashMap<>();

        public Long getDefaultTime() {
            return defaultTime;
        }

        public void setDefaultTime(Long defaultTime) {
            this.defaultTime = defaultTime;
        }

        public Map<String, Long> getTimeByKey() {
            return timeByKey;
        }

        public void setTimeByKey(Map<String, Long> timeByKey) {
            this.timeByKey = timeByKey;
        }

        public Integer getDefaultBatch() {
            return defaultBatch;
        }

        public void setDefaultBatch(Integer defaultBatch) {
            this.defaultBatch = defaultBatch;
        }

        public Map<String, Integer> getBatchByKey() {
            return batchByKey;
        }

        public void setBatchByKey(Map<String, Integer> batchByKey) {
            this.batchByKey = batchByKey;
        }
    }

    public static class Vacuum {

        @NotNull
        private boolean enabled = true;

        @NotNull
        @Min(1000)
        private Long time = 86400000L;

        @NotNull
        private Integer max = 2000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }

        public Integer getMax() {
            return max;
        }

        public void setMax(Integer max) {
            this.max = max;
        }
    }

    public static class Rest {

        @NotNull
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Instances {

        @NotNull
        private Integer nbDefault = 1;

        private Map<String, Integer> byVerticle = new HashMap<>();

        private ForPoll forPoll = ForPoll.availableProcessor;

        @NotNull
        private Integer nbPollDefault = 1;

        private Map<String, Integer> poll = new HashMap<>();

        public Integer getNbDefault() {
            return nbDefault;
        }

        public void setNbDefault(Integer nbDefault) {
            this.nbDefault = nbDefault;
        }

        public Map<String, Integer> getByVerticle() {
            return byVerticle;
        }

        public void setByVerticle(Map<String, Integer> byVerticle) {
            this.byVerticle = byVerticle;
        }

        public Map<String, Integer> getPoll() {
            return poll;
        }

        public void setPoll(Map<String, Integer> poll) {
            this.poll = poll;
        }

        public ForPoll getForPoll() {
            return forPoll;
        }

        public void setForPoll(ForPoll forPoll) {
            this.forPoll = forPoll;
        }

        public Integer getNbPollDefault() {
            return nbPollDefault;
        }

        public void setNbPollDefault(Integer nbPollDefault) {
            this.nbPollDefault = nbPollDefault;
        }
    }

    public static class Jpa {
        private List<String> packages = new ArrayList<>();

        public List<String> getPackages() {
            return packages;
        }

        public void setPackages(List<String> packages) {
            this.packages = packages;
        }
    }

    public enum ForPoll {
        availableProcessor, fixed;
    }
}
