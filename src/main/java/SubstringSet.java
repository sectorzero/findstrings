import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Description :
 * https://www.hackerrank.com/challenges/find-strings
 *
 * Usage :
 *  insert("aab")
 *  insert("aac");
 *  get(3);
 *  get(4);
 *  get(23);
 *
 * Reasoning for Design Choice and Improvements :
 * 1. A brute-force solution would be to :
 *   collect all O(m^2) substrings of O(n) strings
 *   find the unique ones, possibly using a hashmap or removing dups after sorting
 *   sort - n strings of m length - comparision sort = mnlogn
 *   find the kth order - logN
 *   => (n * m^2) + (m * n * logn) + (log n)
 *
 * 2. Basically this is dominated by collecting and sorting the substrings. Better
 * approach to dealing with substrings of strings and also generating a union of them
 * is to use a 'suffix-trie' or 'suffix-tree'.
 *   - A 'suffix-trie' is great, but dominated by O(n * m^2) construction time - basically
 *   adding all the suffixes of n strings and also takes a worst-case of O(n * m^2) space. But
 *   it makes the query time O(m * k).
 *   - I have used this approach of using the suffix-trie in my implementation
 *   - A 'suffix-tree' can be built using Ukkonen's algorithm in O(n * m) time. This is
 *     an intented improvment to speed up construction time, but not implemented
 *     here due to time constraints.
 *
 * Design :
 * a. Generating an union of all substrings of a set of strings
 * - This is solved by a standard suffix-trie where we keep track of all the suffixes of all the strings
 * in a single suffix-trie.
 * Eg. in 'abba' and 'aba', the common substring 'ba' is captured by including suffixes of both strings
 *
 * b. How to answer the 'order' query :
 * For this I designed a 'size augmented suffix-trie' structure. Each node/edge keeps a track of the
 * 'total number of substrings' under that node. This is easy because when we add a new suffix of a string
 * we can count how many new nodes/edges were added new and those are the number of new substrings added.
 * Eg. { "aab", "aac" } =>
 *              root(8)
 *         a(6)         b(1)       c(1)
 *     a(3) b(1) c(1)
 *  b(1) c(1)
 * Since the elements/edges are ordered lexiographically, we can find the parition where the 'kth order' belongs.
 * By using the size augumented suffix-trie the kth order substring can simply be found by following down
 * the right partition from the root as we narrow down to the required substring in at most O(m * k) steps. k is the
 * constant factor to find the partition each node ( at-most 26 linear scan )
 *
 * Eg. Same set above,
 * 1. find(7)
 * => root [6,1,1], pick partition 1
 * => k=1, b, reached base-case ( k == 1 )
 * => 'b'
 *
 * 2. find(4)
 * => root [6,1,1], pick partition 1
 * => k=4, a, [3, 1, 1], pick partition 1 ( a counts for 1st substring, so the 3rd substring is partition 1)
 * => k=3, a, [1, 1], pick partition 2 ( a counts for 1st substring, so the 3rd substring is partition 2 )
 * => k=1, c, reached base-case ( k == 1 )
 * => 'aac'
 *
 * 3. find(1)
 * => root [6,1,1], pick partition 1
 * => k=1, a, reached base-case ( k == 1 )
 * => 'a'
 *
 * HackerRank Evaluation and Comment :
 * - Functional-Correctness : [ 6 / 6 ]
 * - Time-Complexity : [ 3-4 / 6 ]  : This is where the construction time of O(m^2 * n) is hurting
 * Case Correctness  Timing NumStrings  AvgStringLength ConstructionTimeMs  NumQueries  TotalQueryTimeMs
 * 1    OK           OK     -
 * 2    OK           OK     -
 * 3    OK           OK     -
 * 4    OK           OK     20          383.50          1699                100         15
 * 5    OK           FAIL   40          1575.65         18745               421         64
 * 6    OK           FAIL   50          1370.20         13384               500         73
 * ( Machine : 2.6 GHz Intel Core i5 / 16 GB 1600 MHz DDR3 / x86_64 / Oracle JRE Java 8 )
 *
 * You can see that this solution is highly optimized towards query-time. As discussed above, the construction
 * time can be reduced by using 'suffix-tree' with Ukkonen's construction algo bringing the asymptotics down from
 * O(n * m^2) -> O(n * m)
 */
public class SubstringSet {

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

  @Value
  class PartitionResult {
    final int numElementsBeforePartition;
    final SuffixTrieNode partition;
    /**
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
    */
  }

  // -----------------
  // Internal : Core Data-Structures
  // -----------------
  @Getter
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    /**
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
    */
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
