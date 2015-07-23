import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class Solution {

  public static void main(String[] args) {
    try {
      final SubstringSet s = new SubstringSet();

      final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      //final BufferedReader br = new BufferedReader(
      //    new InputStreamReader(
      //        new FileInputStream("/tmp/test-case-5")));
      String input = null;

      LocalDateTime st = LocalDateTime.now();
      // Input
      input = br.readLine();
      int nStrings = Integer.parseInt(input);
      int totalStringLength = 0;
      for(int i = 0; i < nStrings; ++i) {
        input = br.readLine();
        totalStringLength += input.length();
        // System.out.println(String.format("Inserting : %s", input));
        s.insert(input);
      }
      LocalDateTime insertEnd = LocalDateTime.now();

      // Query
      input = br.readLine();
      int nQueries = Integer.parseInt(input);
      for(int i = 0; i < nQueries; ++i) {
        input = br.readLine();
        int query = Integer.parseInt(input);
        String result = s.find(query);
        System.out.println((result != null) ? result : "INVALID");
      }
      LocalDateTime queryEnd = LocalDateTime.now();
      //System.out.println(
      //String.format("NumStrings=%d, AvgStringLength=%.2f, ConstructionTimeMs=%d, NumQueries=%d, TotalQueryTimeMs=%d",
      //    nStrings,
      //    ((double) totalStringLength / nStrings),
      //    Duration.between(st, insertEnd).toMillis(),
      //    nQueries,
      //    Duration.between(insertEnd, queryEnd).toMillis()));
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  static class SubstringSet {

    final SuffixTrieNode root;

    public SubstringSet() {
      this.root = new SuffixTrieNode('.', new TreeMap<>(), 0);
    }

    // -----------------
    // public API
    // -----------------
    public int insert(String s) {
      return insertSuffixesOf(s);
    }

    public int size() {
      return root.getCount();
    }

    public String get(int k) {
      return find(k);
    }

    // -----------------
    // Internal : PUT
    // -----------------
    int insertSuffixesOf(String s) {
      // ( Inefficient First Pass )
      // char[] buf = s.toCharArray();
      // for(int j = 1; j <= s.length(); ++j) {
      //   for(int i = 0; i <= (s.length() - j); ++i) {
      //     // System.out.println(String.format("Inserting : %s", s.substring(i, i+j)));
      //     insertSuffix(buf, i, i + j - 1);
      //   }
      // }
      int count = 0;
      char[] buf = s.toCharArray();
      for(int i = 0; i < s.length(); ++i) {
        // System.out.println(String.format("Inserting : %s", s.substring(i, s.length())));
        count += insertSuffix(buf, i, s.length() - 1);
      }
      return count;
    }

    int insertSuffix(String suffix) {
      return insertSuffixAt(root, suffix.toCharArray(), 0, suffix.length() - 1);
    }

    int insertSuffix(char[] suffix, int st, int en) {
      assert(st <= en);
      return insertSuffixAt(root, suffix, st, en);
    }

    // Impl uses stack recursion impl to 'conditionally' bubble up count
    int insertSuffixAt(SuffixTrieNode n, char[] buf, int idx, int en) {
      if(idx > en) {
        return 0;
      }

      SuffixTrieNode next = null;
      boolean insertedNew = false;
      char c = buf[idx];
      if(n.getChildren().containsKey(c)) {
        next = n.getChildren().get(c);
      } else {
        next = SuffixTrieNode.createFor(c);
        n.getChildren().put(c, next);
        insertedNew = true;
      }

      int added = insertSuffixAt(next, buf, idx + 1, en);
      if(insertedNew) {
        ++added;
      }
      if(added > 0) {
        n.accumulateCountBy(added);
      }
      return added;
    }

    // -----------------
    // Internal : GET
    // -----------------
    String find(int order) {
      if(order < 1 || order > size()) {
        return null;
      }

      StringBuilder b = new StringBuilder();
      PartitionResult start = getPartition(root, order);
      SuffixTrieNode n = start.getPartition();
      int k = order - start.getNumElementsBeforePartition();
      while(true) {
        b.append(n.getC());
        if(k == 1) {
          return b.toString();
        }
        PartitionResult partition = getPartition(n, k-1);
        n = partition.getPartition();
        k -= (partition.getNumElementsBeforePartition() + 1);
      }
    }

    PartitionResult getPartition(SuffixTrieNode n, int k) {
      int numElementsBeforePartition = 0;
      Map<Character, SuffixTrieNode> children = n.getChildren();
      Iterator<Character> i = n.getChildren().keySet().iterator();
      while(i.hasNext()) {
        SuffixTrieNode next = children.get(i.next());
        if(numElementsBeforePartition + next.getCount() < k) {
          numElementsBeforePartition += next.getCount();
          continue;
        }
        return new PartitionResult(numElementsBeforePartition, next);
      }
      return new PartitionResult(numElementsBeforePartition, null);
    }

    class PartitionResult {
      final int numElementsBeforePartition;
      final SuffixTrieNode partition;
       PartitionResult(int numElementsBeforePartition, SuffixTrieNode partition) {
       this.numElementsBeforePartition = numElementsBeforePartition;
       this.partition = partition;
       }
       public int getNumElementsBeforePartition() {
       return numElementsBeforePartition;
       }
       public SuffixTrieNode getPartition() {
       return partition;
       }
    }

    // -----------------
    // Internal : Core Data-Structures
    // -----------------
    static class SuffixTrieNode {
      final Character c;
      final TreeMap<Character, SuffixTrieNode> children;
      int count;

      public void accumulateCountBy(int k) {
        count += k;
      }

      static public SuffixTrieNode createFor(char c) {
        return new SuffixTrieNode(c, new TreeMap<>(), 1);
      }

       private SuffixTrieNode(Character c, TreeMap<Character, SuffixTrieNode> children, int count) {
       this.c = c;
       this.children = children;
       this.count = count;
       }
       public Character getC() {
       return c;
       }
       public TreeMap<Character, SuffixTrieNode> getChildren() {
       return children;
       }
       public int getCount() {
       return count;
       }
    }

    // -----------------
    // Debug Utilities
    // -----------------
    public void show() {
      visitDfs(root, (n) -> {
        System.out.println(String.format("%c|%d", n.getC(), n.getCount()));
      });
      visitBfs(root, (n) -> {
        StringBuilder b = new StringBuilder();
      });
    }

    void visitDfs(SuffixTrieNode n, Consumer<SuffixTrieNode> v) {
      v.accept(n);
      for(SuffixTrieNode c : n.getChildren().values()) {
        visitDfs(c, v);
      }
    }

    void visitBfs(SuffixTrieNode n, Consumer<SuffixTrieNode> v) {
      Deque<SuffixTrieNode> q = new ArrayDeque<>();
      q.add(n);
      while(!q.isEmpty()) {
        SuffixTrieNode next = q.poll();
        v.accept(next);
        next.getChildren().values().forEach(q::push);
      }
    }
  }
}
