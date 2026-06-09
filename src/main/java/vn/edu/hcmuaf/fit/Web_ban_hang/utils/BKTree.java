package vn.edu.hcmuaf.fit.Web_ban_hang.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BKTree {


    private static final class Node {
        final String word;
        final Map<Integer, Node> children = new HashMap<>();

        Node(String word) { this.word = word; }
    }

    private Node root;
    public void add(String word) {
        if (word == null || word.isEmpty()) return;

        if (root == null) {
            root = new Node(word);
            return;
        }

        Node cur = root;
        while (true) {
            int dist = levenshtein(word, cur.word);
            if (dist == 0) return;                  

            Node child = cur.children.get(dist);
            if (child == null) {
                cur.children.put(dist, new Node(word)); 
                return;
            }
            cur = child;                                
        }
    }

    public List<String> search(String query, int maxDist) {
        List<String> results = new ArrayList<>();
        if (root != null) {
            searchNode(root, query, maxDist, results);
        }
        return results;
    }

    public boolean isEmpty() { return root == null; }

    private void searchNode(Node node, String query, int maxDist, List<String> out) {
        int dist = levenshtein(query, node.word);

        if (dist <= maxDist) {
            out.add(node.word);
        }

        int lo = dist - maxDist;
        int hi = dist + maxDist;

        for (Map.Entry<Integer, Node> entry : node.children.entrySet()) {
            int edge = entry.getKey();
            if (edge >= lo && edge <= hi) {
                searchNode(entry.getValue(), query, maxDist, out);
            }
        }
    }


    public static int levenshtein(String a, String b) {
        int la = a.length(), lb = b.length();

        if (la == 0) return lb;
        if (lb == 0) return la;

        if (la < lb) { String tmp = a; a = b; b = tmp; int t = la; la = lb; lb = t; }

        int[] prev = new int[lb + 1];
        int[] curr = new int[lb + 1];
        for (int j = 0; j <= lb; j++) prev[j] = j;

        for (int i = 1; i <= la; i++) {
            curr[0] = i;
            char ca = a.charAt(i - 1);

            for (int j = 1; j <= lb; j++) {
                if (ca == b.charAt(j - 1)) {
                    curr[j] = prev[j - 1];          
                } else {
                    curr[j] = 1 + Math.min(
                            prev[j - 1],             
                            Math.min(prev[j],       
                                     curr[j - 1])   
                    );
                }
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }

        return prev[lb];
    }
}
