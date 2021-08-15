# Document

Document是单一一条数据的抽象实现,将其抽象为Document
在springboot的封装中
org.springframework.data.elasticsearch.annotations.Document的实现类 可能更好方便我们理解,换句话说 就是将一个pojo抽象成为了Document类并进行映射

## lucene-extensions


### document
#### BinaryRange

二进制编码范围的Field字段

**变量定义**

```
public static final int BYTES = InetAddressPoint.BYTES;
private static final FieldType TYPE;
```

这里采用了静态初始化
```
static {
    TYPE = new FieldType();
    TYPE.setDimensions(2, BYTES);
    TYPE.freeze();
}
```

构造函数 
```
public BinaryRange(String name, byte[] encodedRange)
```

类内方法
```
public static Query newIntersectsQuery(String field, byte[] encodedRange)
```

实现方法

```
new RangeFieldQuery(field, encodedRange, 1, relation) {
            @Override
            protected String toString(byte[] ranges, int dimension) {
                return "[" + new BytesRef(ranges, 0, BYTES) + " TO " + new BytesRef(ranges, BYTES, BYTES) + "]";
            }
        };
```
主要实现了toString方法

同样的FieldType是jar包内的类,我们也进去看看他到底是什么
## lucene-jar

### index

#### IndexableField

代表用于索引的单个字段。
IndexWriter使用Iterable IndexableField作为文档。

成员变量:
```
public String name();
```

内部方法:
```
  public IndexableFieldType fieldType(); //描述此字段的属性。
  public TokenStream tokenStream(Analyzer analyzer, TokenStream reuse);//创建用于对该字段建立索引的TokenStream。
  public BytesRef binaryValue(); 
  public String stringValue();
  default CharSequence getCharSequenceValue() {
    return stringValue();
  }
  public Reader readerValue();
  public Number numericValue();
```

### document

包内需要关注的 是field和Document类 
剩下的例如Point Range xxField 大都是为了实现接口的不同的类 换句话说 
可以理解为 类的多态  重载

#### Document

文档是索引和搜索的基本单元
一个Document是一个fields的集合,每一个field都应该有名字和文本

Document 实现了 IndexableField的可迭代接口

并且继承和实现了 iterator方法
**成员变量**
```
private final List<IndexableField> fields = new ArrayList<>();
```
**构造函数**

```
public Iterator<IndexableField> iterator() {
    return fields.iterator();
  }
```
**类内方法**

```
public final void add(IndexableField field) {
    fields.add(field);
  }
public final void removeField(String name)
public final void removeFields(String name) 
public final BytesRef[] getBinaryValues(String name) 
public final BytesRef getBinaryValue(String name)
public final IndexableField getField(String name)
public IndexableField[] getFields(String name) 
public final List<IndexableField> getFields()
public final String[] getValues(String name)
public final String get(String name)
public final String toString() 
```
都是些常见的CRUD操作 反而简单理解
根据iter或者是field数组的遍历
同时判断 
field.name().equals(name))
进行各种各样的赋值操作

#### Field
直接为文档创建一个字段。用户应使用其中一个糖类：

```
TextField: Reader or String indexed for full-text search
StringField: String indexed verbatim as a single token
IntPoint:int  indexed for exact/range queries.
LongPoint:long  indexed for exact/range queries.
FloatPoint:float  indexed for exact/range queries.
DoublePoint:double  indexed for exact/range queries.
SortedDocValuesField:byte[]  indexed column-wise for sorting/faceting
SortedSetDocValuesField:SortedSet<byte[]>  indexed column-wise for sorting/faceting
NumericDocValuesField:long  indexed column-wise for sorting/faceting
SortedNumericDocValuesField:SortedSet<long>  indexed column-wise for sorting/faceting
StoredField: Stored-only value for retrieving in summary results
```
#### FieldType
具体属性
```
  private boolean stored;
  private boolean tokenized = true;
  private boolean storeTermVectors;
  private boolean storeTermVectorOffsets;
  private boolean storeTermVectorPositions;
  private boolean storeTermVectorPayloads;
  private boolean omitNorms;
  private IndexOptions indexOptions = IndexOptions.NONE;
  private boolean frozen;
  private DocValuesType docValuesType = DocValuesType.NONE;
  private int dimensionCount;
  private int indexDimensionCount;
  private int dimensionNumBytes;
  private Map<String, String> attributes;
```

#### IndexableFieldType

描述field属性
接口方法
```
  public boolean stored(); //应存储该字段的值，则为True
  public boolean tokenized(); //分析器应分析该字段的值，则为True
  public boolean storeTermVectors();//termvector 为True
  public boolean storeTermVectorOffsets();//termvector offset 为True
  public boolean storeTermVectorPositions();//termvector position 为true
  public boolean storeTermVectorPayloads();
  public boolean omitNorms();//字段中应省略归一化值。
  public IndexOptions indexOptions();// 返回索引变化
  public DocValuesType docValuesType();// 文档类型
  public int pointDimensionCount();//这是正数（代表点尺寸的数量），该字段被索引为一个点。
  public int pointIndexDimensionCount();
  public int pointNumBytes();
  public Map<String, String> getAttributes();
```


### analysis

#### Analyzer

Analyzer的出现在这里是作为文档说明的成分出现的,顺势补上之前的Token阐述部分,读Analyzer的源码.分析器将构建TokenStream，用于分析文本。因此，它代表了一种从文本中提取索引词的策略。

为了定义要进行的分析，子类必须定义其createComponents（String）中的TokenStreamComponents。然后在每次调用#tokenStream（String，Reader）时重用这些组件。

**变量定义**

```
private final ReuseStrategy reuseStrategy;
private Version version = Version.LATEST;
public static final ReuseStrategy GLOBAL_REUSE_STRATEGY
public static final ReuseStrategy PER_FIELD_REUSE_STRATEGY 
CloseableThreadLocal<Object> storedValue = new CloseableThreadLocal<>();
```

这里面Version 是版本定义  需要和各个版本的es进行适配 这里定义是的是最新版本

ReuseStrategy 是内部静态类 重用策略

**CloseableThreadLocal** 自动关闭回收的ThreadLocal


**构造函数**
```
public Analyzer() {
    this(GLOBAL_REUSE_STRATEGY);
  }

public Analyzer(ReuseStrategy reuseStrategy) {
    this.reuseStrategy = reuseStrategy;
  }
```

**类内方法**

```
protected Reader initReader(String fieldName, Reader reader)
protected Reader initReaderForNormalization(String fieldName, Reader reader)
protected AttributeFactory attributeFactory(String fieldName)
```

**公开方法**

```
public final TokenStream tokenStream(final String fieldName, final String text)
public final BytesRef normalize(final String fieldName, final String text)
public int getPositionIncrementGap(String fieldName)
public int getOffsetGap(String fieldName)
public final ReuseStrategy getReuseStrategy()
public void setVersion(Version v)
public Version getVersion()
public void close() 
```

**tokenStream的初始化**
策略获取到tokenStream可重用组件,并且初始化一个Reader
如果组件为空 则创建组件 并且加入可重用策略组
保证组件不为空的条件下 设置组建的Reader为初始化的Reader
最后返回 组件的get方法

**normalize方法**
normalize的返回是字节流 也就是之前定义的ByteRef
参数是fieldName和文本

定义一个String 的文本变量 用final修饰

当初始化 Reader成功后 (Reader reader = new StringReader(text))
调用initReaderForNormalization 将Field作为参数 传递给Reader 但是这里 方法内 未对field进行操作直接返回Reader 这里的作用 **应该是保证Field不为空**
创建一个buffer char[64]数组
同时创建一个StringBuilder

循环操作:
调用reader的read方法,将buffer进行初始化.
如果read方法返回的标志位为-1 则证明读取为空 跳出循环
builder append buffer 和 偏移量 read方法返回的标志位

同时将builder的数值传递给文本变量

这里完成初始化 64个字符进行分割 尾追写入


接下来进行实例操作 

创建一个实例工厂类 用final修饰 传入参数 fieldName

之后用返回为TokenStream的normalize方法 创建一个TokenStream对象

```
  protected TokenStream normalize(String fieldName, TokenStream in) {
    return in;
  }
```
此外这里的TokenStream 调用  
```
new StringTokenStream(attributeFactory, filteredText, text.length())
```
实现
**StringTokenStream 是TokenStream的子类 上文中详细阐述了TokenStream,String TokenStream类,两者近似相同 区别在于 内部方法的实现不同 尤其是incrementToken方法 内部调用的是 CharTermAttribute OffsetAttribute 记录数据 这两个模块的设计思路近似于 StringBuilder**

继续回到normalize函数
成功完成初始化之后
用final定义一个TermToBytesRefAttribute对象,用tokenstream的addAttribute(TermToBytesRefAttribute.class)方法实现 
这里面全都是反射的思想

之后tokenstream进行reset 也就是清空对象

之后将TermToBytesRefAttribute对象 进行深拷贝  前后进行incrementToken()进行判断

进行返回 ByteRef 深拷贝之后的对象

**静态内部类**

```
public static final class TokenStreamComponents 
public static abstract class ReuseStrategy
private static final class StringTokenStream extends TokenStream
```
TokenStreamComponents类:
内部的两个成员变量 
 Consumer<Reader> source;
 TokenStream sink;
Consumer容器 生产者消费者模型 jdk1.8 新增**表示一个接受单个输入参数且不返回结果的操作**

进行变量的binding

ReuseStrategy 类
```
public abstract TokenStreamComponents  getReusableComponents
public abstract void setReusableComponents
getStoredValue
setStoredValue
```
## ElasticSearch
