package dzcorp.jpa.util;

import java.util.List;

public class Paginated<T> {


    public List<T> results;

    public int totalPage;

    public long totalRecord;


    public Paginated(List<T> results, int totalPage, long totalRecord) {
        this.results = results;
        this.totalPage = totalPage;
        this.totalRecord = totalRecord;
    }

    public Paginated() {
    }
}
