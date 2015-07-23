import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SubstringSetTest {

  final static List<String> TEST_SET_1 = Arrays.asList(
      "aac",
      "abc",
      "aab",
      "baba"
  );

  final static List<String> TEST_SET_2 = Arrays.asList(
      "aac",
      "aab"
  );

  SubstringSet s;

  @Before
  public void setup() {
    s = new SubstringSet();
  }

  @Test
  public void insert_1() {
    s.insert("a");
    assertEquals(1, s.size());
  }

  @Test
  public void insert_2() {
    s.insert("a");
    s.insert("ab");

    assertEquals(3, s.size());
  }

  @Test
  public void insert_3() {
    s.insert("a");
    s.insert("ab");
    s.insert("ba");

    assertEquals(4, s.size());
  }

  @Test
  public void insert_4() {
    s.insert("a");
    s.insert("ab");
    s.insert("ba");
    s.insert("bad");

    assertEquals(7, s.size());
  }

  @Test
  public void insert_5() {
    s.insert("a");
    s.insert("a");

    assertEquals(1, s.size());
  }

  @Test
  public void insert_6() {
    s.insert("aa");
    s.insert("aa");

    assertEquals(2, s.size());
  }

  @Test
  public void insert_7() {
    s.insert("aab");

    assertEquals(5, s.size());
  }

  @Test
  public void find_partition_1() {
    insert(TEST_SET_1, 14);

    SubstringSet.PartitionResult p = s.getPartition(s.root, 12);
    assertNotNull(p);
    assertEquals(8, p.getNumElementsBeforePartition());
    assertEquals('b', p.getPartition().getC().charValue());
    assertEquals(5, p.getPartition().getCount());
  }

  @Test
  public void find_partition_2() {
    insert(TEST_SET_1, 14);

    SubstringSet.PartitionResult p = s.getPartition(s.root, 9);
    assertNotNull(p);
    assertEquals(8, p.getNumElementsBeforePartition());
    assertEquals('b', p.getPartition().getC().charValue());
    assertEquals(5, p.getPartition().getCount());
  }

  @Test
  public void find_partition_3() {
    insert(TEST_SET_1, 14);

    SubstringSet.PartitionResult p = s.getPartition(s.root, 13);
    assertNotNull(p);
    assertEquals(8, p.getNumElementsBeforePartition());
    assertEquals('b', p.getPartition().getC().charValue());
    assertEquals(5, p.getPartition().getCount());
  }

  @Test
  public void find_partition_4() {
    insert(TEST_SET_1, 14);

    SubstringSet.PartitionResult p = s.getPartition(s.root, 1);
    assertNotNull(p);
    assertEquals(0, p.getNumElementsBeforePartition());
    assertEquals('a', p.getPartition().getC().charValue());
    assertEquals(8, p.getPartition().getCount());
  }

  @Test
  public void find_partition_5() {
    insert(TEST_SET_1, 14);

    SubstringSet.PartitionResult p = s.getPartition(s.root, 14);
    assertNotNull(p);
    assertEquals(13, p.getNumElementsBeforePartition());
    assertEquals('c', p.getPartition().getC().charValue());
    assertEquals(1, p.getPartition().getCount());
  }

  @Test
  public void find_kth_1() {
    insert(TEST_SET_1, 14);

    assertEquals("bc", s.find(13));
    assertEquals("bab", s.find(11));
    assertEquals("b", s.find(9));
    assertEquals("a", s.find(1));
    assertEquals("c", s.find(14));
    assertEquals("ac", s.find(8));
  }

  @Test
  public void find_kth_2() {
    insert(TEST_SET_2, 8);

    assertEquals("aab", s.find(3));
    assertEquals("c", s.find(8));
    assertNull(s.find(23));
  }

  void insert(List<String> samples, int expectedSize) {
    samples.forEach(s::insert);
    assertEquals(expectedSize, s.size());
  }

}
