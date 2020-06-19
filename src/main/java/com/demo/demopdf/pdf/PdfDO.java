package com.demo.demopdf.pdf;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PdfDO {
	/**页眉*/
	private String header;
	/**标题*/
	private String title="";
	/**作者*/
	private String author="";
	/**关键字*/
	private String keywords="";
	/**主题*/
	private String subject="";
	/** 是否需要盖章 */
	private Boolean stamp=true;
	/**签章信息*/
	private PdfSignDO pdfSignDO;

}
