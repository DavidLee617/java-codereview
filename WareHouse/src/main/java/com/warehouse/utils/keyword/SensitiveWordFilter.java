package com.warehouse.utils.keyword;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class SensitiveWordFilter {
	private Map sensitiveWordMap = null;
	public static int minMatchTYpe = 1;      //最小匹配规则
	public static int maxMatchType = 2;      //最大匹配规则
	private static SensitiveWordFilter instance = null; //单例

	/**
	 * 构造函数，初始化敏感词库
	 * @throws Exception
	 */
	public SensitiveWordFilter()throws Exception{
		sensitiveWordMap = new SensitiveWordInit().initKeyWord();
	}

	/**
	 * 获取单例
	 * @return
	 * @throws Exception
	 */
	public static SensitiveWordFilter getInstance() throws Exception {
		if (null == instance) {
			instance = new SensitiveWordFilter();
		}
		return instance;
	}

	/**
	 * 判断文字是否包含敏感字符
	 * @param txt
	 * @param matchType
	 * @return
	 */
	public boolean isContaintSensitiveWord(String txt,int matchType){
		boolean flag = false;
		for(int i = 0 ; i < txt.length() ; i++){
			int matchFlag = this.CheckSensitiveWord(txt, i, matchType); //判断是否包含敏感字符
			if(matchFlag > 0){    //大于0存在，返回true
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 获取敏感词
	 * @param txt
	 * @param matchType
	 * @return
	 */
	public Set<String> getSensitiveWord(String txt , int matchType){
		Set<String> sensitiveWordList = new HashSet<String>();
		for(int i = 0 ; i < txt.length() ; i++){
			int length = CheckSensitiveWord(txt, i, matchType);    //判断是否包含敏感字符
			if(length > 0){
				sensitiveWordList.add(txt.substring(i, i+length));  //存在,加入list中
				i = i + length - 1;    //减1的原因，是因为for会自增
			}
		}
		return sensitiveWordList;
	}

	/**
	 * 替换敏感词汇
	 * @param txt
	 * @param matchType
	 * @param replaceChar
	 * @return
	 */
	public String replaceSensitiveWord(String txt,int matchType,String replaceChar){
		String resultTxt = txt;
		Set<String> set = getSensitiveWord(txt, matchType);     //获取所有的敏感词
		Iterator<String> iterator = set.iterator();
		String word = null;
		String replaceString = null;
		while (iterator.hasNext()) {
			word = iterator.next();
			replaceString = getReplaceChars(replaceChar, word.length());
			resultTxt = resultTxt.replaceAll(word, replaceString);
		}
		return resultTxt;
	}

	/**
	 * 获取替换字符串
	 * @param replaceChar
	 * @param length
	 * @return
	 */
	private String getReplaceChars(String replaceChar,int length){
		String resultReplace = replaceChar;
		for(int i = 1 ; i < length ; i++){
			resultReplace += replaceChar;
		}
		return resultReplace;
	}

	/**
	 * 检查文字中是否包含敏感字符
	 * 如果存在，则返回敏感词字符的长度，不存在返回0
	 * @param txt
	 * @param beginIndex
	 * @param matchType
	 * @return
	 */
	public int CheckSensitiveWord(String txt,int beginIndex,int matchType){
		boolean  flag = false;    //敏感词结束标识位：用于敏感词只有1位的情况
		int matchFlag = 0;     //匹配标识数默认为0
		char word = 0;
		Map nowMap = sensitiveWordMap;
		for(int i = beginIndex; i < txt.length() ; i++){
			word = txt.charAt(i);
			nowMap = (Map) nowMap.get(word);     //获取指定key
			if(nowMap != null){     //存在，则判断是否为最后一个
				matchFlag++;     //找到相应key，匹配标识+1
				if("1".equals(nowMap.get("isEnd"))){       //如果为最后一个匹配规则,结束循环，返回匹配标识数
					flag = true;       //结束标志位为true
					if(SensitiveWordFilter.minMatchTYpe == matchType){    //最小规则，直接返回,最大规则还需继续查找
						break;
					}
				}
			}
			else{     //不存在，直接返回
				break;
			}
		}
		if(matchFlag < 2 || !flag){        //长度必须大于等于1，为词
			matchFlag = 0;
		}
		return matchFlag;
	}
}
