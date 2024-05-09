package se.payerl;

import org.apache.maven.model.Dependency;

public class ScopeOrder extends SortOrder<ScopeOrder> {
    String first;
    String then;

    public ScopeOrder() { }
    public ScopeOrder(String first, String then) {
        this.first = first;
        this.then = then;
    }

    @Override
    public String getFirst() {
        return first;
    }

    @Override
    public String getThen() {
        return then;
    }

    @Override
    public ScopeOrder setFirst(String first) {
        this.first = first;
        return this;
    }

    @Override
    public ScopeOrder setThen(String then) {
        this.then = then;
        return this;
    }

    @Override
    public String toString() {
        return "ScopeOrder{first=" + first + ", then=" + then + "}";
    }

    @Override
    public String getFilter(Dependency dep) {
        return dep.getScope();
    }

    @Override
    public String depToStr(Dependency dep) {
        return dep.getGroupId() + ":" + dep.getArtifactId() + " scope:" + dep.getScope();
    }
}
