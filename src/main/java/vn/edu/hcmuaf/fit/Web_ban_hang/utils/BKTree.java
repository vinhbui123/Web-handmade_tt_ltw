package vn.edu.hcmuaf.fit.Web_ban_hang.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//  BK-Tree (Burkhard-Keller Tree) for fuzzy string lookup node in range [dist-k, dist+k]

//  A BK-Tree organises strings by edit distance so that all strings within
//  a given Levenshtein distance

public class BKTree {


    private static final class Node {
        final String word;
        /** children keyed by exact edit distance from this node's word */
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
            if (dist == 0) return;                      // already in tree

            Node child = cur.children.get(dist);
            if (child == null) {
                cur.children.put(dist, new Node(word)); // insert as new leaf
                return;
            }
            cur = child;                                // descend
        }
    }


//  Return all words in the tree whose Levenshtein distance

    public List<String> search(String query, int maxDist) {
        List<String> results = new ArrayList<>();
        if (root != null) {
            searchNode(root, query, maxDist, results);
        }
        return results;
    }

    public boolean isEmpty() { return root == null; }

    // ---------------------------------------------------------------
    // Recursive BK-Tree search
    // ---------------------------------------------------------------
    private void searchNode(Node node, String query, int maxDist, List<String> out) {
        int dist = levenshtein(query, node.word);

        if (dist <= maxDist) {
            out.add(node.word);
        }

        // BK-Tree invariant: the child at edge k contains words whose distance
        // to node.word is exactly k.  We only need to recurse into children
        // where k ∈ [dist - maxDist, dist + maxDist].
        int lo = dist - maxDist;
        int hi = dist + maxDist;

        for (Map.Entry<Integer, Node> entry : node.children.entrySet()) {
            int edge = entry.getKey();
            if (edge >= lo && edge <= hi) {
                searchNode(entry.getValue(), query, maxDist, out);
            }
        }
    }

    // ---------------------------------------------------------------
    // Levenshtein (edit) distance — standard DP, O(|a|·|b|) time,
    // O(|b|) space (two-row rolling array).
    // ---------------------------------------------------------------

    public static int levenshtein(String a, String b) {
        int la = a.length(), lb = b.length();

        // Trivial cases
        if (la == 0) return lb;
        if (lb == 0) return la;

        // Ensure 'b' is the shorter string to minimise memory
        if (la < lb) { String tmp = a; a = b; b = tmp; int t = la; la = lb; lb = t; }

        int[] prev = new int[lb + 1];
        int[] curr = new int[lb + 1];

        // Initialise first row
        for (int j = 0; j <= lb; j++) prev[j] = j;

        for (int i = 1; i <= la; i++) {
            curr[0] = i;
            char ca = a.charAt(i - 1);

            for (int j = 1; j <= lb; j++) {
                if (ca == b.charAt(j - 1)) {
                    curr[j] = prev[j - 1];          // no operation needed
                } else {
                    curr[j] = 1 + Math.min(
                            prev[j - 1],             // substitute
                            Math.min(prev[j],        // delete from a
                                     curr[j - 1])    // insert into a
                    );
                }
            }

            // Swap rows
            int[] tmp = prev; prev = curr; curr = tmp;
        }

        return prev[lb];
    }
}
