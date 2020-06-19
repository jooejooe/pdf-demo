package com.demo.demopdf.pdf;


import java.awt.FontMetrics;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Base64;

import javax.swing.JLabel;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliers;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItextpdfUtils {

	/**
	 * 将指定的html内容转化成pdf文档之后,写入到指定的输出流.
	 */
	public static void write(PdfDO pdfDO, String htmlContent, OutputStream os) {
		if (htmlContent == null || htmlContent.length() == 0) {
			return;
		}
		if (os == null) {
			return;
		}
		htmlContent = getIntactHtml(htmlContent);
		doWrite(pdfDO, htmlContent, os);
	}

	/**
	 * html完整内容的前缀标识
	 */
	private static final String INTACT_FLAG = "<!DOCTYPE html";
	/**
	 * html模板,当待转换的html只是片断时,需将其插入到模板的body内.
	 */
	private static final String TEMPLATE_HTML = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\"><head></head><body><div class=\"ql-editor\">${content}</div></body></html>";

	/**
	 * 根据提供的html内容,获取完整的html内容.
	 *
	 * @param htmlContent
	 *            html内容
	 * @return 完整的html内容
	 */
	private static String getIntactHtml(String htmlContent) {
		boolean intact = htmlContent.trim().toLowerCase().startsWith(INTACT_FLAG);
		if (!intact) {
			htmlContent = TEMPLATE_HTML.replaceFirst("\\$\\{content}", htmlContent);
		}
		return htmlContent;
	}

	/**
	 * 实施写操作.
	 *
	 * @param htmlContent
	 *            html内容
	 * @param os
	 *            outputStream
	 */
	private static void doWrite(PdfDO pdfDO, String htmlContent, OutputStream os) {
		InputStream is = new ByteArrayInputStream(htmlContent.getBytes(Charset.forName("UTF-8")));
		Document document = new Document();
		PdfWriter writer = null;
		try {
			writer = PdfWriter.getInstance(document, os);
			if(null!=pdfDO.getPdfSignDO()&&StrUtil.isNotBlank(pdfDO.getPdfSignDO().getOwnPassword())) {
				byte[] OWN_PASSWORD_BYTES = pdfDO.getPdfSignDO().getOwnPassword().getBytes();
				byte[] USER_PASSWORD_BYTES = pdfDO.getPdfSignDO().getUserPassword().getBytes();
				/*
					权限参数
					PdfWriter.ALLOW_MODIFY_CONTENTS
					允许打印,编辑，复制，签名 加密级别：40-bit-RC4
					PdfWriter.ALLOW_COPY
					**允许复制，签名 不允许打印，编辑 加密级别：40-bit-RC ***
					PdfWriter.ALLOW_MODIFY_ANNOTATIONS
					允许打印,编辑，复制，签名 加密级别：40-bit-RC4
					PdfWriter.ALLOW_FILL_IN
					允许打印,编辑，复制，签名 加密级别：40-bit-RC4
					PdfWriter.ALLOW_SCREENREADERS
					允许打印,编辑，复制，签名 加密级别：40-bit-RC4
					PdfWriter.ALLOW_ASSEMBLY
					允许打印,编辑，复制，签名 加密级别：40-bit-RC4
					PdfWriter.EMBEDDED_FILES_ONLY
					允许打印,编辑，复制，签名 加密级别：40-bit-RC4
					PdfWriter.DO_NOT_ENCRYPT_METADATA
					允许打印,编辑，复制，签名 加密级别：40-bit-RC4
					PdfWriter.ENCRYPTION_AES_256
					允许打印,编辑，复制，签名 加密级别：256-bit-AES
					PdfWriter.ENCRYPTION_AES_128
					允许打印,编辑，复制，签名 加密级别：128-bit-AES
					PdfWriter.STANDARD_ENCRYPTION_128
					允许打印,编辑，复制，签名 加密级别：128-bit-RC4
					PdfWriter.STANDARD_ENCRYPTION_40
					允许打印,编辑，复制，签名 加密级别：40-bit-RC4 
				 */
				// 不可打印
				writer.setEncryption(USER_PASSWORD_BYTES, OWN_PASSWORD_BYTES, PdfWriter.ALLOW_PRINTING, PdfWriter.STANDARD_ENCRYPTION_40);
				// 不可更改
				writer.setEncryption(USER_PASSWORD_BYTES, OWN_PASSWORD_BYTES, PdfWriter.ALLOW_MODIFY_CONTENTS, PdfWriter.STANDARD_ENCRYPTION_40);
				// 不可复制
				writer.setEncryption(USER_PASSWORD_BYTES, OWN_PASSWORD_BYTES, PdfWriter.ALLOW_COPY, PdfWriter.STANDARD_ENCRYPTION_40);
				// 不可注释
				writer.setEncryption(USER_PASSWORD_BYTES, OWN_PASSWORD_BYTES, PdfWriter.ALLOW_MODIFY_ANNOTATIONS, PdfWriter.STANDARD_ENCRYPTION_40);
				// 不可填写表单域
				writer.setEncryption(USER_PASSWORD_BYTES, OWN_PASSWORD_BYTES, PdfWriter.ALLOW_FILL_IN, PdfWriter.STANDARD_ENCRYPTION_40);
				// 不可页面提取
				writer.setEncryption(USER_PASSWORD_BYTES, OWN_PASSWORD_BYTES, PdfWriter.ALLOW_SCREENREADERS, PdfWriter.STANDARD_ENCRYPTION_40);
				// 不可文档组合
				writer.setEncryption(USER_PASSWORD_BYTES, OWN_PASSWORD_BYTES, PdfWriter.ALLOW_ASSEMBLY, PdfWriter.STANDARD_ENCRYPTION_40);
				// 不可签名
				writer.setEncryption(USER_PASSWORD_BYTES, OWN_PASSWORD_BYTES, PdfWriter.ALLOW_DEGRADED_PRINTING, PdfWriter.STANDARD_ENCRYPTION_40);
			}
			// 标题
			document.addTitle(pdfDO.getTitle());
			// 作者
			document.addAuthor(pdfDO.getAuthor());
			// 关键字
			document.addKeywords(pdfDO.getKeywords());
			// 主题
			document.addSubject(pdfDO.getSubject());
			
			//设置行距
			writer.setInitialLeading(12.5f);
			// 设置页眉页脚
			writer.setPageEvent(new HeaderFooter(pdfDO.getHeader()));
			
			document.open();
			// 设置CSS样式
			CSSResolver cssResolver = new StyleAttrCSSResolver();
			//读取配置信息
			JSON settingJson = JSONUtil.readJSON(new File(FileUtil.getAbsolutePath("font/setting.json")), Charset.defaultCharset());
			JSONArray cssFilesJSON= (JSONArray) settingJson.getByPath("css");
			JSONArray fontFilesJSON= (JSONArray) settingJson.getByPath("font");
			for (Object cssFile : cssFilesJSON) {
				cssResolver.addCss(XMLWorkerHelper.getCSS(new FileInputStream(FileUtil.getAbsolutePath((String) cssFile))));
			}
			// 设置字体
			XMLWorkerFontProvider fontProvider = new XMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);
			// 注意！！！如果你外部字体为ttc文件，比如simsun.ttc，在引入的地方就要注意写法，如下：后面有个[,1]
			for (Object fontFile : fontFilesJSON) {
				String path = (String) ((JSON)fontFile).getByPath("path");
				String alias = (String) ((JSON)fontFile).getByPath("alias");
				if(path.toLowerCase().endsWith("ttc")) {
					fontProvider.register(FileUtil.getAbsolutePath(path)+",1", alias);
				}
				if(path.toLowerCase().endsWith("ttf")) {
					fontProvider.register(FileUtil.getAbsolutePath(path), alias); 
				}
			}
			
			CssAppliers cssAppliers = new CssAppliersImpl(fontProvider);
			// HTML
			HtmlPipelineContext htmlContext = new HtmlPipelineContext(cssAppliers);
			htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
			
			// Pipelines
			PdfWriterPipeline pdf = new PdfWriterPipeline(document, writer);
			HtmlPipeline html = new HtmlPipeline(htmlContext, pdf);
			CssResolverPipeline css = new CssResolverPipeline(cssResolver, html);

			// XML Worker
			XMLWorker worker = new XMLWorker(css, true);
			XMLParser p = new XMLParser(worker);
			p.parse(is,Charset.forName("UTF-8"));
		} catch (Exception e) {
			log.error("write pdf error ", e);
		} finally {
			try {
				if (null != is) {
					is.close();
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			if (null != writer) {
				writer.flush();
			}
			document.close();
		}
	}

	/**
	 * @param src
	 *            需要签章的pdf文件路径
	 * @param dest
	 *            签完章的pdf文件路径 chain 证书链 pk 签名私钥 digestAlgorithm 摘要算法名称，例如SHA-1
	 *            provider 密钥算法提供者，可以为null cryptoStandard 数字签名格式，itext有2种 reason
	 *            签名的原因，显示在pdf签名属性中，随便填 location 签名的地点，显示在pdf签名属性中，随便填
	 * @throws GeneralSecurityException
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static void sign(PdfSignDO pdfSignDO, String src, String dest)
			throws GeneralSecurityException, IOException, DocumentException {
		char[] PASSWORD = pdfSignDO.getPwd().toCharArray();
		// Creating the reader and the stamper，开始pdf reader
		PdfReader reader = new PdfReader(src, StrUtil.isBlank(pdfSignDO.getOwnPassword())?null:pdfSignDO.getOwnPassword().getBytes());
		// 目标文件输出流
		FileOutputStream os = new FileOutputStream(dest);
		// 创建签章工具PdfStamper ，最后一个boolean参数
		// false的话，pdf文件只允许被签名一次，多次签名，最后一次有效
		// true的话，pdf可以被追加签名，验签工具可以识别出每次签名之后文档是否被修改
		PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', null, true);
		
		// 获取数字签章属性对象，设定数字签章的属性
		PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
		String reason = pdfSignDO.getSignReason();
		String location = pdfSignDO.getSignLocation();
		appearance.setReason(reason);
		appearance.setLocation(location);
		// 设置签名的位置，页码，签名域名称，多次追加签名的时候，签名预名称不能一样
		// 签名的位置，是图章相对于pdf页面的位置坐标，原点为pdf页面左下角
		// 四个参数的分别是，图章左下角x，图章左下角y，图章右上角x，图章右上角y
		// appearance.setVisibleSignature(new Rectangle(450, 747, 550, 847), 1, "sig1");
		// 读取图章图片，这个image是itext包的image
//		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
//		//使用图片盖章
//		BufferedImage seal = GoogleBarCodeUtils.insertWords(GoogleBarCodeUtils.getBarCode("1322222"), "1322222");
//		ImageIO.write(seal, "png", imOut);
//		InputStream imageInputStream = new ClassPathResource(SIGNATURE).getInputStream();
		
//		//动态盖章
//		try {
//			Seal.builder()
//			.size(200)
//			.borderCircle(SealCircle.builder().line(4).width(95).height(95).build())
//			.mainFont(SealFont.builder().text("中国四大天王股份有限公司").size(22).space(22.0).margin(4).build())
//			.centerFont(SealFont.builder().text("★").size(60).build())
//			.titleFont(SealFont.builder().text("电子签章").size(16).space(8.0).margin(54).build())
//			.build()
//			.draw(imOut);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		InputStream imageInputStream = new ByteArrayInputStream(bs.toByteArray());
//		
//		byte[] imageByte=IOUtils.toByteArray(imageInputStream);
		//base64图片
		byte[] imageByte=Base64.getDecoder().decode(pdfSignDO.getSignature());
		Image image = Image.getInstance(imageByte);
//		Image image = Image.getInstance(new ClassPathResource(SIGNATURE).getFile().getAbsolutePath());
		appearance.setSignatureGraphic(image);
		appearance.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);

		int pageNum = reader.getNumberOfPages();
		for (int i = 1; i <= pageNum; i++) {
			int currentPageNum = i;
			new PdfReaderContentParser(reader).processContent(currentPageNum, new RenderListener() {
				public void beginTextBlock() {
					
				}
				public void renderText(TextRenderInfo textRenderInfo) {
					String text = textRenderInfo.getText();
					if (text != null && text.contains(pdfSignDO.getSignKey())) {
						// 文字在page中的横坐标、纵坐标
						Rectangle2D.Float textFloat = textRenderInfo.getBaseline().getBoundingRectange();
						float x = textFloat.x;
						float y = textFloat.y;
						// 设置图片位置
						appearance.setVisibleSignature(new Rectangle(x - pdfSignDO.getLlx(), y - pdfSignDO.getLly(),
								x + pdfSignDO.getUrx(), y + pdfSignDO.getUry()), currentPageNum, System.currentTimeMillis()+"");
					}
				}
				
				public void endTextBlock() {
					
				}
				
				public void renderImage(ImageRenderInfo renderInfo) {
					
				}
			});
		}

		// 设置图章的显示方式，如下选择的是只显示图章（还有其他的模式，可以图章和签名描述一同显示）
		// appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
		appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
		// 这里的itext提供了2个用于签名的接口，可以自己实现，后边着重说这个实现
		// 摘要算法
		ExternalDigest digest = new BouncyCastleDigest();
		// 签名算法
		String provider = null;
		String digestAlgorithm = DigestAlgorithms.SHA1;
		KeyStore ks = KeyStore.getInstance("PKCS12");
		InputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(pdfSignDO.getKeystore()));//new ClassPathResource(KEYSTORE).getInputStream();
		ks.load(inputStream, PASSWORD);
		String alias = ks.aliases().nextElement();
		PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
		ExternalSignature signature = new PrivateKeySignature(pk, digestAlgorithm, provider);
		// 调用itext签名方法完成pdf签章
		MakeSignature.CryptoStandard cryptoStandard = MakeSignature.CryptoStandard.CMS;
		Certificate[] chain = ks.getCertificateChain(alias);
		MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, cryptoStandard);
		// Closes the document. No more content can be written after the document is closed.
		stamper.close();
//		imageInputStream.close();
		inputStream.close();
	}
	

	public void getPdfContract() throws IOException {

	}

	/**
	 * 把html形式的模板转换为PDF
	 * @param pdfSignDO
	 *            pdf相关信息
	 * @param content
	 *            合同模板内容-未设值
	 * @param object
	 *            数据（对象/Map），为空则不填充
	 * @param file
	 */
	public static void generatePDFFromHtmlContent(PdfDO pdfDO, String content, File file) {
		File copyFile = new File("copy-" + file.getName());
		FileOutputStream os = null;
		try {
			if (!copyFile.exists()) {
				boolean result = copyFile.createNewFile();
				if (result) {
					System.out.println("The named copy file does not exist and was successfully created");
				} else {
					System.out.println("The named copy file already exists");
				}
			}

			os = new FileOutputStream(copyFile);
			// HTML转PDF
			ItextpdfUtils.write(pdfDO, content, os);
			if (pdfDO.getStamp()) {
				// 数字签名
				ItextpdfUtils.sign(pdfDO.getPdfSignDO(), copyFile.getAbsolutePath(), file.getAbsolutePath());
			} else {
				// 不签名，直接输出文件
				FileUtil.copy(copyFile, file, true);
			}
		} catch (Exception e) {
			log.error("html convert to PDF error", e);
		} finally {
			IoUtil.close(os);
			FileUtil.del(copyFile);
		}
	}
	
	
	/**
     * 添加文字水印，并附加UUID
     * @param srcFile 待加水印文件
     * @param destFile 加水印后输出文件
     * @param text 文本内容
     * @throws Exception
     */
    public static void addWaterMark(String srcFile, OutputStream os, String text) throws Exception {
        // 待加水印的文件
        PdfReader reader = new PdfReader(srcFile);
        // 加完水印的文件
        PdfStamper stamper = new PdfStamper(reader, os);

        int total = reader.getNumberOfPages() + 1;
        PdfContentByte content;

        // 设置透明度
        PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.3f);
        // 设置字体
        BaseFont base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
        // 循环对每页插入水印
        for (int i = 1; i < total; i++)
        {
            // 水印的起始
            content = stamper.getOverContent(i);
            content.setGState(gs);
            content.setFontAndSize(base, 15);
            // 开始
            content.beginText();
            // 设置颜色 默认为黑色
            content.setColorFill(BaseColor.BLACK);
            
            // 开始写入水印
            content.showTextAligned(Element.ALIGN_MIDDLE, text, 180,340, 45);
            content.showTextAligned(Element.ALIGN_MIDDLE, UUID.randomUUID().toString(), 140,240, 45);
            content.endText();
        }
        stamper.close();
    }
}