package com.warehouse.utils.keyword;

import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 初始化敏感词库，将敏感词加入到HashMap中，构建DFA算法模型
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SensitiveWordInit {
	private String ENCODING = "UTF-8";    //字符编码

	public SensitiveWordInit(){
		super();
	}

	/**
	 * 初始化敏感字库
	 * @return
	 * @throws Exception
	 */
	public Map initKeyWord()throws Exception{
		Set<String> keyWordSet = readSensitiveWordFile();
		// 将敏感词库加入到HashMap中,确定有穷自动机DFA
		return addSensitiveWordToHashMap(keyWordSet);
	}

	/**
	 * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型：<br> 
	 * 中 = { 
	 *      isEnd = 0 
	 *      国 = { 
	 *           isEnd = 1 
	 *           人 = {isEnd = 0 
	 *                民 = {isEnd = 1} 
	 *                } 
	 *           男  = { 
	 *                  isEnd = 0 
	 *                   人 = { 
	 *                        isEnd = 1 
	 *                       } 
	 *               } 
	 *           } 
	 *      } 
	 *  五 = { 
	 *      isEnd = 0 
	 *      星 = { 
	 *          isEnd = 0 
	 *          红 = { 
	 *              isEnd = 0 
	 *              旗 = { 
	 *                   isEnd = 1 
	 *                  } 
	 *              } 
	 *          } 
	 *      } 
	 *
	 */

    /**
     * 将HashSet中的敏感词,存入HashMap中
	 * @param keyWordSet
     * @return
     */
	private Map addSensitiveWordToHashMap(Set<String> keyWordSet) {
		Map sensitiveWordMap = new HashMap(keyWordSet.size());     //初始化敏感词容器，减少扩容操作
		String key = null;  
		Map nowMap = null;
		Map<String, String> newWorMap = null;
		//迭代keyWordSet
		Iterator<String> iterator = keyWordSet.iterator();
		while(iterator.hasNext()){
			key = iterator.next();
			nowMap = sensitiveWordMap;
			for(int i = 0 ; i < key.length() ; i++){
				char keyChar = key.charAt(i);       //转换成char型
				Object wordMap = nowMap.get(keyChar);
				if(wordMap != null){        //如果存在该key，直接赋值
					nowMap = (Map) wordMap;
				} else{     //不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
					newWorMap = new HashMap<String,String>();
					newWorMap.put("isEnd", "0");     //不是最后一个
					nowMap.put(keyChar, newWorMap);
					nowMap = newWorMap;
				}
				if(i == key.length() - 1){
					nowMap.put("isEnd", "1");    //最后一个
				}
			}
		}
		return sensitiveWordMap;
	}

	/**
     * 读取敏感词库中的内容，将内容添加到set集合中
	 * @return
     * @throws Exception
	 */
	private Set<String> readSensitiveWordFile() throws Exception{
		Set<String> set = null;
		File file = ResourceUtils.getFile("classpath:SensitiveWord.txt");    //读取文件
		InputStreamReader read = new InputStreamReader(new FileInputStream(file),ENCODING);
		try {
			if(file.isFile() && file.exists()){      //文件流是否存在
				set = new HashSet<String>();
				BufferedReader bufferedReader = new BufferedReader(read);
				String txt = null;
				while((txt = bufferedReader.readLine()) != null){    //读取文件，将文件内容放入到set中
					set.add(txt);
			    }
			} else{
				throw new Exception("文件不存在");
			}
		} catch (Exception e) {
			throw e;
		}finally{
			read.close();
		}
		return set;
	}
}
