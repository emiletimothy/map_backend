package edu.caltech.cs2.datastructures;

import edu.caltech.cs2.interfaces.IDeque;

import java.util.Iterator;

public class NGram implements Iterable<String>, Comparable<NGram> {
    public static final String NO_SPACE_BEFORE = ",?!.-,:'";
    public static final String NO_SPACE_AFTER = "-'><=";
    public static final String REGEX_TO_FILTER = "”|\"|“|\\(|\\)|\\*";
    public static final String DELIMITER = "\\s+|\\s*\\b\\s*";
    private IDeque<String> data;

    public static String normalize(String s) {
        return s.replaceAll(REGEX_TO_FILTER, "").strip();
    }

    public NGram(IDeque<String> x) {
        this.data = new LinkedDeque<>();
        for (String word : x) {
            this.data.add(word);
        }
    }

    public NGram(String data) {
        this(normalize(data).split(DELIMITER));
    }

    public NGram(String[] data) {
        this.data = new LinkedDeque<>();
        for (String s : data) {
            s = normalize(s);
            if (!s.isEmpty()) {
                this.data.addBack(s);
            }
        }
    }

    public NGram next(String word) {
        String[] data = new String[this.data.size()];
        Iterator<String> dataIterator = this.data.iterator();
        dataIterator.next();
        for (int i = 0; i < data.length - 1; i++) {
            data[i] = dataIterator.next();
        }
        data[data.length - 1] = word;
        return new NGram(data);
    }

    public String toString() {
        String result = "";
        String prev = "";
        for (String s : this.data) {
            result += ((NO_SPACE_AFTER.contains(prev) || NO_SPACE_BEFORE.contains(s) || result.isEmpty()) ? "" : " ") + s;
            prev = s;
        }
        return result.strip();
    }

    @Override
    public Iterator<String> iterator() {
        return this.data.iterator();
    }

    @Override
    public int compareTo(NGram other) {

        //setting up an iterator type for both
        Iterator<String> ditr = this.data.iterator();
        Iterator<String> oitr = other.iterator();

        for (int i = 0; i < this.data.size(); i++){

            if (!oitr.hasNext()){
                return 1;
            }
            //after so that we check if there is a next first

            String ditr_copy = ditr.next();
            String oitr_copy = oitr.next();

            if (ditr_copy.compareTo(oitr_copy) < 0){
                return -1;
            }
            else if (ditr_copy.compareTo(oitr_copy) > 0){
                return 1;
            }
            //if they are the same keep going
        }

        if (oitr.hasNext()){
            return -1;
        }

        return 0;

    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NGram)) {
            return false;
        }
        NGram copy = (NGram) o;

        if (this.compareTo(copy) == 0) { //this is current NGram being used since we don't know what data they'd be using
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int count = 31; //depends on position
        int hashcode = 0;

        for(String s: this.data){
            count *= 31;
            hashcode += s.hashCode() * count; //making it well distributed to different buckets
        }

        return hashcode;
    }
}