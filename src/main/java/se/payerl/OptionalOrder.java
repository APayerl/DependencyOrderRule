package se.payerl;

import org.apache.maven.model.Dependency;

import java.util.List;

public class OptionalOrder extends SortOrder<OptionalOrder> {
    String first;
    String then;

    @Override
    public String getFirst() {
        return this.first;
    }

    @Override
    public String getThen() {
        return this.then;
    }

    @Override
    public OptionalOrder setFirst(String first) {
        this.first = first;
        return this;
    }

    @Override
    public OptionalOrder setThen(String then) {
        this.then = then;
        return this;
    }

    @Override
    public String toString() {
        return "OptionalOrder{first=" + first + ", then=" + then + "}";
    }

    @Override
    public String getFilter(Dependency dep) {
        return dep.getOptional();
    }

    @Override
    public String depToStr(Dependency dep) {
        return dep.getGroupId() + ":" + dep.getArtifactId() + " optional:" + dep.getOptional();
    }
}
