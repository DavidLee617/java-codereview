/*
 * Copyright (c) 2018-2999 广州亚米信息科技有限公司 All rights reserved.
 *
 * https://www.gz-yami.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.shop.common.config;

import com.yami.shop.common.bean.ALiDaYu;
import com.yami.shop.common.bean.Qiniu;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


/**
 * 商城配置文件
 * @author lgh
 */
@Data
@Component
@PropertySource("classpath:shop.properties")
@ConfigurationProperties(prefix = "shop")
public class ShopBasicConfig {

	/**
	 * 七牛云的配置信息
	 */
	private Qiniu qiniu;

	/**
	 * 阿里大于短信平台
	 */
	private ALiDaYu aLiDaYu;

	/**
	 * 用于加解密token的密钥
	 */
	private String tokenAesKey;

	public Qiniu getQiniu() {
		return qiniu;
	}

	public void setQiniu(Qiniu qiniu) {
		this.qiniu = qiniu;
	}

	public ALiDaYu getaLiDaYu() {
		return aLiDaYu;
	}

	public void setaLiDaYu(ALiDaYu aLiDaYu) {
		this.aLiDaYu = aLiDaYu;
	}

	public String getTokenAesKey() {
		return tokenAesKey;
	}

	public void setTokenAesKey(String tokenAesKey) {
		this.tokenAesKey = tokenAesKey;
	}

	public  void setALiDaYu(ALiDaYu aLiDaYu){
		this.aLiDaYu=aLiDaYu;
	}
	public ALiDaYu getALiDaYu() {
		return aLiDaYu;
	}
}
