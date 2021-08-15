# ES7.8.0源码解析

## ES模块构成

1. lucene基本内容 lucene的jar包
2. 在jar包之上的一些lucene功能的新增
3. Elasticsearch源码部分

## lucene-jar

### search

#### Query
抽象类 具体的实现有
```
TermQuery
BooleanQuery
WildcardQuery
PhraseQuery
PrefixQuery
MultiPhraseQuery
FuzzyQuery
RegexpQuery
TermRangeQuery
PointRangeQuery
ConstantScoreQuery
DisjunctionMaxQuery
MatchAllDocsQuery
```
也就是我们search所发送的命令所对应的类

### analysis


#### tokenattributes

##### PositionIncrementAttributeImpl

常见的get和set方法实现

##### PositionIncrementAttribute

内置set和set方法 位置自增

#### FilteringTokenFilter

该类是TokenFilter的抽象类 用来去除 token
内部有两个方法需要注意 

position-embedding 

```
accept
incrementToken //来自TokenStream,进行重载
```

**变量定义**

```
private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  private int skippedPositions;
```

**方法定义**

```
accept
incrementToken
reset
end
```
其中实现相应逻辑的是 也就是incrementToken
```
  @Override
  public final boolean incrementToken() throws IOException {
    skippedPositions = 0;
    while (input.incrementToken()) {
      if (accept()) {
        if (skippedPositions != 0) {
          posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
        }
        return true;
      }
      skippedPositions += posIncrAtt.getPositionIncrement();
    }

    // reached EOS -- return false
    return false;
  }
```


#### Tokenizer

**变量定义**

```
protected Reader input = ILLEGAL_STATE_READER;
private Reader inputPending = ILLEGAL_STATE_READER;

private static final Reader ILLEGAL_STATE_READER = new Reader() {
    @Override
    public int read(char[] cbuf, int off, int len) {
      throw new IllegalStateException("TokenStream contract violation: reset()/close() call missing, " +
          "reset() called multiple times, or subclass does not call super.reset(). " +
          "Please see Javadocs of TokenStream class for more information about the correct consuming workflow.");
    }

    @Override
    public void close() {} 
  };
```
ILLEGAL_STATE_READER 初始化空读
Reader java.io 缓存读取 继承了 Readable以及Closeable
**Closeable 关闭自动回收资源**
**构造函数**
```
protected Tokenizer(AttributeFactory factory) {
    super(factory);
}
```
他通过TokenStream中的构造函数实现

**类内方法**

```
public void close() throws IOException;//继承自TokenStream
protected final int correctOffset(int currentOff);//返回正确的偏移量
public final void setReader(Reader input);//设置Reader 基本上是常用类 这里的reader 常常应该是各种各样的analyzer
public void reset() throws IOException;//继承自tokenStream
```


#### TokenFilter

**变量定义**:
protected final TokenStream input;

**构造函数**:
  protected TokenFilter(TokenStream input) {
    super(input);
    this.input = input;
  }
  //TokenStream继承自AttributeSource 所以可以调用

protected TokenStream(AttributeSource input) {
    super(input);
    assert assertFinal();
  }
  
  方法
**类内方法**:

```
public void end() throws IOException 
public void close() throws IOException 
public void reset() throws IOException
```
完全继承TokenStream
#### TokenStream

Token流 这是一个抽象类
主要的子类或者是实现类有Tokenizer以及TokenFilter
Tokenizer 输入是Reader类,TokenFilter的输入是另一个TokenStream

**变量定义**:

```
public static final AttributeFactory DEFAULT_TOKEN_ATTRIBUTE_FACTORY =
    AttributeFactory.getStaticImplementation(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY, PackedTokenAttributeImpl.class);
```

**构造函数**

```
  protected TokenStream(AttributeSource input) {
    super(input);
    assert assertFinal();
  }
  protected TokenStream(AttributeFactory factory) {
    super(factory);
    assert assertFinal();
  }
  
```
对应上述说是的Tokenizer以及TokenFilter的两种实现方法

类内方法

```
private boolean assertFinal() //判断方法是否私有
public abstract boolean incrementToken() throws IOException;
public void end() throws IOException;
public void reset() throws IOException {}
public void close() throws IOException {}
```


### tokenattributes

#### TermToBytesRefAttribute

### util

#### RamUsageEstimator

可以理解为直接操作内存
// 以后注意 大项目的common或者util类 底层 必须得带这个
在内存块中分配一定的空间合理操作并分配内存

统计所占内存 各种数据结构(Map TreeNode)

主要的两个方法
```
sizeOfObject
alignObjectSize
```
**内部类**
RamUsageQueryVisitor

#### BytesRef
byte[] 数组的封装 

**构造函数**
```
  public BytesRef() {
    this(EMPTY_BYTES);
  }
  public BytesRef(byte[] bytes, int offset, int length) {
    this.bytes = bytes;
    this.offset = offset;
    this.length = length;
    assert isValid();
  }
  public BytesRef(byte[] bytes) {
    this(bytes, 0, bytes.length);
  }
  public BytesRef(int capacity) {
    this.bytes = new byte[capacity];
  }
  public BytesRef(CharSequence text) {
    this(new byte[UnicodeUtil.maxUTF8Length(text.length())]);
    length = UnicodeUtil.UTF16toUTF8(text, 0, text.length(), bytes);
  }

```
常用的一般是最后一个.
#### AttributeFactory

AttributeFactory 是 Attribute的工厂类 
用于创建AttributeImpl实例 


**设计重点** 反射

1. 首先new出来的对象我们无法访问其中的私有属性，但是通过反射出来的对象我们可以通过setAccessible()方法来访问其中的私有属性。
2. 在使用new创建一个对象实例的时候必须知道类名，但是通过反射创建对象有时候不需要知道类名也可以

**设计模式**: 工厂模式

**变量定义**:
```
private static final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
  private static final MethodType NO_ARG_CTOR = MethodType.methodType(void.class);
  private static final MethodType NO_ARG_RETURNING_ATTRIBUTEIMPL = MethodType.methodType(AttributeImpl.class);
```

**内部类定义**:


StaticImplementationAttributeFactory
AttributeFactory返回给定的实例以实现其实现的属性。

类内包括 AttributeFactory和Class 这两个成员变量

内部重要实现方法

createAttributeInstance

类内创建
返回是createInstance()或者createAttributeInstance()

**方法**
findAttributeImplCtor

#### AttributeSource

AttributeSouce 用来集成所有的attribute的实现类和方法的列表。同事将Attribute属性的实际类型传递给addAttribute类， 单例模式

**内部类定义**： State 实现了Cloneable借口 表明该内部类是可以被复制的。其数据结构采用链表的形式进行传递

**变量定义**：
  
  ```
  private final Map<Class<? extends Attribute>, AttributeImpl> attributes;
  private final Map<Class<? extends AttributeImpl>, AttributeImpl> attributeImpls;
  private final State[] currentState;
  private final AttributeFactory factory;
  private static final ClassValue<Class<? extends Attribute>[]> implInterfaces;
  ```

**ClassValue java.lang 将计算值与（可能）每种类型懒散地关联。如果动态语言需要为在消息发送调用站点遇到的每个类构造消息分配表 继承需要实现computeValue方法 在这个方法内创建一个不重复集合 将调用借口的方法作为子类 asSubClass加入到set中,最终返回一个数组**


官方对于 implInterfaces的解释是缓存，用于存储已知实现类的所有接口以提高性能（慢反射）

attribute attributeImpls实现外部只读且异步，因此采用final和private修饰
**初始化实现**

```
public AttributeSource(AttributeSource input)
通过Attribute实体创建Source

public AttributeSource(AttributeFactory factory)
通过工厂类创建实体
```

**迭代器实现**
```
public final Iterator<Class<? extends Attribute>> getAttributeClassesIterator()
public final Iterator<AttributeImpl> getAttributeImplsIterator() 
```
上述两个迭代器
第一个是返回所有继承了Attribute类的迭代器
第二是返回所有的attributeImpl类的迭代器。

第二个迭代器getAttributeImplsIterator内有常见的三种方法 删除 迭代，是否有些一个迭代，其中标志位的判断是当前的State 当初始状态不为空 则实例化一个新的迭代器，根据链表的数据结构 进行遍历返回。
AttributeImpl att=state.attribute;
若初始状态为空 则返回空集合的迭代器.

```
static Class<? extends Attribute>[] getAttributeInterfaces(final Class<? extends AttributeImpl> clazz)
```
其返回值便是implInterfaces.

**增加**

```
添加具有一个或多个Attribute接口的自定义AttributeImpl实例。
public final void addAttributeImpl(final AttributeImpl att)
public final <T extends Attribute> T addAttribute(Class<T> attClass) 
```

**是否存在**

```
public final boolean hasAttributes() {
    return !this.attributes.isEmpty();
  }
public final boolean hasAttribute(Class<? extends Attribute> attClass) {
    return this.attributes.containsKey(attClass);
  }
```
判断获取attribute的map是否为空 或者是判断是否包含相对应的key
之后也是一些常见的hashmap的操作 
例如 
```
getAttribute
clearAttributes
endAttributes
removeAllAttributes
captureState
restoreState
hashcode
equals
reflectAsString  //返回当前attribute的值并且转换为String
reflectWith //此方法迭代所有Attribute实现，并调用
对应的AttributeImpl
cloneAttributes //Cloneable 借由State操作 从而实现AttributeSource的拷贝
copyTo //赋值给一个新的对象
```


**所用设计模式**

单例模式

#### AttributeFactory


所用设计模式：
工厂模式

## lucene-extension

lucene额外新增功能在org.apache.lucene包内，现对包文件进行注意阅读和分析

### sanalysis.miscellaneous

#### DeDuplicatingTokenFilter

检查Token流中是否有重复的Token序列。 Token序列的长度最小-6是一种很好的启发式方法，因为它避免了过滤常见的习惯用语/短语，但会检测出较长的段，这些段通常是文本的剪切+粘贴副本。

在内部，每个Token被散列/模化为一个字节（每个Token有256个可能的值），然后使用{@link DuplicateByteSequenceSpotter}记录在一个可见字节序列中。 这个特里传递到TokenFilter构造函数中，因此单个对象可以在多个文档中重复使用。

generateDuplicates设置控制是否从结果中过滤出重复的标记或将其输出（{@link DuplicateSequenceAttribute}属性可用于在emitDuDuplicates为true时检查先前的目击次数）

上述内容是其在备注中对该文件的一些阐述和功能说明。主要的作用就是去重也就是句子切割成词后，对句子中的重复此进行切割。别切之后对于词进行hash处理，形成词向量

其继承链如下:

DeDuplicatingTokenFilter<-[FilteringTokenFilter](#FilteringTokenFilter)<-[TokenFilter](#TokenFilter)<-[TokenStream](#TokenStream)<-[AttributeSource](#AttributeSource)

**构造函数**

```

public DeDuplicatingTokenFilter(TokenStream in, DuplicateByteSequenceSpotter byteStreamDuplicateSpotter) {
        this(in, byteStreamDuplicateSpotter, false);
    }

    /**
     * 
     * @param in
     *            The input token stream
     * @param byteStreamDuplicateSpotter
     *            object which retains trie of token sequences
     * @param emitDuplicates
     *            true if duplicate tokens are to be emitted (use
     *            {@link DuplicateSequenceAttribute} attribute to inspect number
     *            of prior sightings of tokens as part of a sequence).
     */
public DeDuplicatingTokenFilter(TokenStream in, DuplicateByteSequenceSpotter byteStreamDuplicateSpotter, boolean emitDuplicates) {
        super(new DuplicateTaggingFilter(byteStreamDuplicateSpotter, in));
        this.emitDuplicates = emitDuplicates;
    }
```

**成员变量**

```
private final DuplicateSequenceAttribute seqAtt = addAttribute(DuplicateSequenceAttribute.class);
private final boolean emitDuplicates;
static final MurmurHash3.Hash128 seed = new MurmurHash3.Hash128();
```

**内部类**

DuplicateTaggingFilter 创建了一颗trie树 [DuplicateByteSequenceSpotter](#DuplicateByteSequenceSpotter)

其中相对重要的方法有
```
public void loadAllTokens() throws IOException {
         

            pos = 0;
            boolean isWrapped = false;
            State priorStatesBuffer[] = new State[windowSize];
            //windowSize 初始值为DuplicateByteSequenceSpotter.TREE_DEPTH;
            short priorMaxNumSightings[] = new short[windowSize];
            int cursor = 0;
            while (input.incrementToken()) {
                BytesRef bytesRef = termBytesAtt.getBytesRef();
                //BytesRef 对于字节流的封装 在lucene.util中实现 为final class
                long tokenHash = MurmurHash3.hash128(bytesRef.bytes, bytesRef.offset, bytesRef.length, 0, seed).h1;
                //MurmurHash3 Hash算法包 位于org.elasticsearch.common.hash
                byte tokenByte = (byte) (tokenHash & 0xFF);
                //"&0xFF" 就像计算机中的一把剪刀，当‘&’操作符两边数的bit位数相同时不改变数的大小，只是专门截出一个字节的长度,只是为了取得低八位。 0x0F 取得4个比特位
                short numSightings = byteStreamDuplicateSpotter.addByte(tokenByte);
                //添加到树节点上
                priorStatesBuffer[cursor] = captureState();
                // 如果最新token标记为重复，则修改先前捕获的状态对象
                if (numSightings >= 1) {
                    int numLengthsToRecord = windowSize;
                    //记录长度
                    int pos = cursor;
                    //当前指针位置
                    while (numLengthsToRecord > 0) {
                        if (pos < 0) {
                            pos = windowSize - 1;
                            //当向右的指针小于0 将指针位置重新置为对尾
                        }
                        //更新指针所指向的byte流的大小
                        priorMaxNumSightings[pos] = (short) Math.max(priorMaxNumSightings[pos], numSightings);
                        numLengthsToRecord--;
                        pos--;
                        //这里用的是尾部双指针 有点快慢指针的操作环形列表的意思 因为pos<0则进行重置 知道numLengthsToRecord 为0 才会退出循环
                    }
                }
                // 将光标移动到下一个内存空间
                cursor++;
                if (cursor >= windowSize) {
                    // wrap around the buffer
                    cursor = 0;
                    isWrapped = true;
                }
                // clean out the end of the tail that we may overwrite if the
                // next iteration adds a new head
                if (isWrapped) {
                    // tokenPos is now positioned on tail - emit any valid
                    // tokens we may about to overwrite in the next iteration
                    if (priorStatesBuffer[cursor] != null) {
                        recordLengthInfoState(priorMaxNumSightings, priorStatesBuffer, cursor);
                    }
                }
            } // end loop reading all tokens from stream

            // Flush the buffered tokens
            int pos = isWrapped ? nextAfter(cursor) : 0;
            while (pos != cursor) {
                recordLengthInfoState(priorMaxNumSightings, priorStatesBuffer, pos);
                pos = nextAfter(pos);
            }
        }
```
私有方法
```
recordLengthInfoState //将short类型的priorMaxNumSightings 转换为State[] 链表数组
```


#### DisableGraphAttribute
接口类 继承了 Attribute

此借口可用于指示在此TokenStream中不应考虑PositionLengthAttribute。查询解析器可以提取此信息，以确定是否应将此TokenStream作为图进行分析。

实现方法
```
    @Override
    public void clear() {}

    @Override
    public void reflectWith(AttributeReflector reflector) {
    }

    @Override
    public void copyTo(AttributeImpl target) {}
```

#### DuplicateByteSequenceSpotter
这部分是一个树的数据结构 相对重要所以直接贴源码
```
public class DuplicateByteSequenceSpotter {
    public static final int TREE_DEPTH = 6;
    public static final int MAX_HIT_COUNT = 255;
    private final TreeNode root;
    private boolean sequenceBufferFilled = false;
    private final byte[] sequenceBuffer = new byte[TREE_DEPTH];
    private int nextFreePos = 0;


    private final int[] nodesAllocatedByDepth;
    private int nodesResizedByDepth;
    private long bytesAllocated;
    static final long TREE_NODE_OBJECT_SIZE = RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + RamUsageEstimator.NUM_BYTES_OBJECT_REF;
    static final long ROOT_TREE_NODE_OBJECT_SIZE = TREE_NODE_OBJECT_SIZE + RamUsageEstimator.NUM_BYTES_OBJECT_REF;
    static final long LIGHTWEIGHT_TREE_NODE_OBJECT_SIZE = TREE_NODE_OBJECT_SIZE + RamUsageEstimator.NUM_BYTES_OBJECT_REF;    
    static final long LEAF_NODE_OBJECT_SIZE = TREE_NODE_OBJECT_SIZE + Short.BYTES + Integer.BYTES;

    public DuplicateByteSequenceSpotter() {
        this.nodesAllocatedByDepth = new int[4];
        this.bytesAllocated = 0;
        root = new RootTreeNode((byte) 1, null, 0);
    }


    public void startNewSequence() {
        sequenceBufferFilled = false;
        nextFreePos = 0;
    }

    public short addByte(byte b) {
        // Add latest byte to circular buffer
        sequenceBuffer[nextFreePos] = b;
        nextFreePos++;
        if (nextFreePos >= sequenceBuffer.length) {
            nextFreePos = 0;
            sequenceBufferFilled = true;
        }
        if (sequenceBufferFilled == false) {
            return 0;
        }
        TreeNode node = root;
        int p = nextFreePos;

        node = node.add(sequenceBuffer[p], 0);
        p = nextBufferPos(p);
        node = node.add(sequenceBuffer[p], 1);
        p = nextBufferPos(p);
        node = node.add(sequenceBuffer[p], 2);

        p = nextBufferPos(p);
        int sequence = 0xFF & sequenceBuffer[p];
        p = nextBufferPos(p);
        sequence = sequence << 8 | (0xFF & sequenceBuffer[p]);
        p = nextBufferPos(p);
        sequence = sequence << 8 | (0xFF & sequenceBuffer[p]);
        return (short) (node.add(sequence << 8) - 1);
    }

    private int nextBufferPos(int p) {
        p++;
        if (p >= sequenceBuffer.length) {
            p = 0;
        }
        return p;
    }

    abstract class TreeNode {

        TreeNode(byte key, TreeNode parentNode, int depth) {
            nodesAllocatedByDepth[depth]++;
        }

        public abstract TreeNode add(byte b, int depth);

        /**
         * 
         * @param byteSequence
         *            a sequence of bytes encoded as an int
         * @return the number of times the full sequence has been seen (counting
         *         up to a maximum of 32767).
         */
        public abstract short add(int byteSequence);
    }


    class RootTreeNode extends TreeNode {

 
        TreeNode[] children;

        RootTreeNode(byte key, TreeNode parentNode, int depth) {
            super(key, parentNode, depth);
            bytesAllocated += ROOT_TREE_NODE_OBJECT_SIZE;
        }

        public TreeNode add(byte b, int depth) {
            if (children == null) {
                children = new TreeNode[256];
                bytesAllocated += (RamUsageEstimator.NUM_BYTES_OBJECT_REF * 256);
            }
            int bIndex = 0xFF & b;
            TreeNode node = children[bIndex];
            if (node == null) {
                if (depth <= 1) {
                    // Depths 0 and 1 use RootTreeNode impl and create
                    // RootTreeNodeImpl children
                    node = new RootTreeNode(b, this, depth);
                } else {
                    // Deeper-level nodes are less visited but more numerous
                    // so use a more space-friendly data structure
                    node = new LightweightTreeNode(b, this, depth);
                }
                children[bIndex] = node;
            }
            return node;
        }

        @Override
        public short add(int byteSequence) {
            throw new UnsupportedOperationException("Root nodes do not support byte sequences encoded as integers");
        }

    }

  
    final class LightweightTreeNode extends TreeNode {

        int[] children = null;

        LightweightTreeNode(byte key, TreeNode parentNode, int depth) {
            super(key, parentNode, depth);
            bytesAllocated += LIGHTWEIGHT_TREE_NODE_OBJECT_SIZE;

        }

        @Override
        public short add(int byteSequence) {
            if (children == null) {
                // Create array adding new child with the byte sequence combined with hitcount of 1.
                // Most nodes at this level we expect to have only 1 child so we start with the  
                // smallest possible child array.
                children = new int[1];
                bytesAllocated += RamUsageEstimator.NUM_BYTES_ARRAY_HEADER + Integer.BYTES;                
                children[0] = byteSequence + 1;
                return 1;
            }
            
            for (int i = 0; i < children.length; i++) {
                int child = children[i];
                if (byteSequence == (child & 0xFFFFFF00)) {
                    int hitCount = child & 0xFF;
                    if (hitCount < MAX_HIT_COUNT) {
                        children[i]++;
                    }
                    return (short) (hitCount + 1);
                }
            }
            int[] newChildren = new int[children.length + 1];
            bytesAllocated += Integer.BYTES;

            System.arraycopy(children, 0, newChildren, 0, children.length);
            children = newChildren;
            // Combine the byte sequence with a hit count of 1 into an int.
            children[newChildren.length - 1] = byteSequence + 1;
            nodesResizedByDepth++;
            return 1;
        }

        @Override
        public TreeNode add(byte b, int depth) {
            throw new UnsupportedOperationException("Leaf nodes do not take byte sequences");
        }

    }

    public final long getEstimatedSizeInBytes() {
        return bytesAllocated;
    }

    public int[] getNodesAllocatedByDepth() {
        return nodesAllocatedByDepth.clone();
    }

    public int getNodesResizedByDepth() {
        return nodesResizedByDepth;
    }

}
```

#### DuplicateSequenceAttribute

接口类

提供统计学有效的方法 去找到重复段

get和set方法

其实现类 继承了 AttributeImpl方法 内有

clear

copyTo

reflectWith
方法 
以及实现的

set和get方法