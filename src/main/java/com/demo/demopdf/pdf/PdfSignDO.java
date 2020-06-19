package com.demo.demopdf.pdf;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PdfSignDO {
	/** 盖章位置关键字，需要唯一 */
	private String signKey;
	/**签名理由*/
	private String signReason="";
	/**签名地点*/
	private String signLocation="";
	/**图章左下角x偏移量*/
	private float llx = 50f;
	/**图章左下角y偏移量*/
	private float lly = 50f;
	/**图章右上角x偏移量*/
	private float urx = 50f;
	/**图章右上角y偏移量*/
	private float ury = 50f;
	/**签章图片,需base64编码*/
	private String signature;
	/**签名密码*/
	private String pwd;
	/**证书,需base64转码*/
	private String keystore;
	/**pdf文件管理员密码，不设置则不进行加密，可进行修改操作*/
	private String ownPassword=null;
	/**pdf用户密码,需配合管理员密码进行设置，用户只读*/
	private String userPassword=null;
}
