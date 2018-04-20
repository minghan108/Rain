package rain.com.rain;

import java.util.Collections;
import java.util.TreeMap;

/**
 * Created by MSI\mliu on 18/04/18.
 */

public class SortedMap {
    TreeMap<String, Double> sorted_map = new TreeMap<String, Double>();
    TreeMap<String, Double> reverse_sorted_map = new TreeMap<String,Double>();

    public SortedMap(TreeMap<String, Double> sorted_map, TreeMap<String, Double> reverse_sorted_map){
        this.sorted_map = sorted_map;
        this.reverse_sorted_map = reverse_sorted_map;
    }

    public TreeMap<String, Double> getSortedMap(){
        return sorted_map;
    }

    public TreeMap<String, Double> getReverse_sorted_map(){
        return reverse_sorted_map;
    }
}
