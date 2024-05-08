package se.payerl;

import org.apache.maven.model.Dependency;

public interface SortOrder<Order> {
    String getFirst();
    String getThen();
    Order setFirst(String first);
    Order setThen(String then);
    String toString();
    String getFilter(Dependency dep);
    String depToStr(Dependency dep);
}
