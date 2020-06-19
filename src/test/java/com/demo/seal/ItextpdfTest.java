package com.demo.seal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Base64;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.demo.demopdf.pdf.ItextpdfUtils;
import com.demo.demopdf.pdf.PdfDO;
import com.demo.demopdf.pdf.PdfSignDO;
import com.demo.demopdf.seal.Seal;
import com.demo.demopdf.seal.SealCircle;
import com.demo.demopdf.seal.SealFont;
import com.itextpdf.text.DocumentException;

import cn.hutool.core.io.FileUtil;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class ItextpdfTest {

	@Before
	public void mkdir() {

	}

	@Test
	public void getPDF_1() throws Exception {
		File file = new File("demo\\test.pdf");

		PdfDO pdfDO = new PdfDO();
		// 签章设置
		PdfSignDO pdfSignDO = new PdfSignDO();
		pdfSignDO.setSignKey("审判员");
		pdfSignDO.setSignReason("电子合同");
		pdfSignDO.setSignLocation("北京北京");
		
		String signature = Base64.getEncoder().encodeToString(FileUtil.readBytes(FileUtil.getAbsolutePath("pdf/公章1.png")));
		pdfSignDO.setSignature(signature);
		pdfSignDO.setPwd("888888");
		String keystore = Base64.getEncoder().encodeToString(FileUtil.readBytes(FileUtil.getAbsolutePath("pdf/xxx-seal.p12")));
		pdfSignDO.setKeystore(keystore);

		// pdf文件设置
		pdfDO.setAuthor("XXX股份有限公司");
		pdfDO.setSubject("主体");
		pdfDO.setTitle("标题标题");
		pdfDO.setHeader("广州互联网法院");
		// 设置是否签章
		pdfDO.setStamp(true);
		pdfDO.setPdfSignDO(pdfSignDO);
		// 参数不为空则调用模板引擎进行填充
		// 如需要，可以使用freemarker对html文本进行数据填充，FreemarkerUtils可自行编写
//		TemplateEngine engine = TemplateUtil.createEngine(new TemplateConfig("pdf/template", ResourceMode.CLASSPATH));
//		Template template = engine.getTemplate("缴费通知书.ftl");
		// Dict本质上为Map，此处可用Map
		String content = "<p class=\"ql-align-center\"><span class=\"ql-size-large ql-font-FZXiaoBiaoSong-B05S\"><strong>广州互联网法院</strong></span></p><p class=\"ql-align-center\"><span class=\"ql-size-large ql-font-FZXiaoBiaoSong-B05S\"><strong>民 事 判 决 书</strong></span></p><p class=\"ql-align-right\"><span style=\"color: rgb(51, 51, 51);\" class=\"ql-font-FangSong\">（2019）粤0192民初38420号</span></p><p class=\"ql-indent-1\"><span class=\"ql-font-FangSong\">原告：中国银行股份有限公司广州番禺支行，住所地广东省广州市番禺区市桥清河东路338号。</span></p><p class=\"ql-indent-1\"><span class=\"ql-font-FangSong\">负责人：黄海明，该支行行长。</span></p><p>	<span class=\"ql-font-FangSong\">委托诉讼代理人：李倩雯，广东启源律师事务所律师。</span></p><p>	<span class=\"ql-font-FangSong\">被告：关伟斌，男，1989年12月6日出生，汉族，住广东省广州市增城区。</span></p><p>	<span class=\"ql-font-FangSong\">原告中国银行股份有限公司广州番禺支行（以下简称中国银行番禺支行）与被告关伟斌金融借款合同纠纷一案，本院于2019年10月25日立案后，依法适用简易程序，公开开庭进行了审理。原告中国银行番禺支行的委托诉讼代理人李倩雯在线参加了诉讼，被告关伟斌经公告送达开庭传票，没有上线参加诉讼。本案现已审理终结。</span></p><p><span class=\"ql-font-SimSun\"> </span><span class=\"ql-font-FangSong\">案件事实</span></p><p><span class=\"ql-font-FangSong\"> 一、贷款产品情况：</span></p><p><span class=\"ql-font-FangSong\"> 涉案产品为原告经营的“中银E贷”线上信用贷款产品，借款人申请贷款需登录原告“中国银行手机银行”APP，通过输入银行卡预留手机号收到的手机动态密码及通过外部安全介质获取的动态口令提交贷款审批，审批通过获取额度后，通过点击确认的方式确认本次贷款的要素（金额、期限、利率、还款方式、收款账户、还款账户、资金用途等），阅知并同意《中国银行股份有限公司个人网络循环贷款合同》后完成合同签订。原告通过借款人在线下开办中国银行银行卡完成的身份识别以及线下开卡设置的银行卡绑定手机号、电子银行登录密码、外部安全介质动态口令等对借款人的身份进行核实。</span></p><p><span class=\"ql-font-FangSong\"> 二、借款合同的约定情况：</span></p><p><span class=\"ql-font-FangSong\"> 借款人：关伟斌。</span></p><p><span class=\"ql-font-FangSong\"> 循环贷款额度：108000元，循环贷款额度期限：1年。</span></p><p><span class=\"ql-font-FangSong\"> 计息：按借款人向贷款人申请提款时约定的计息方式计算利息；计息天数为每笔贷款的实际放款日至该笔贷款结清为止。</span></p><p><span class=\"ql-font-FangSong\"> 罚息：若借款人未按照约定期限还款，就逾期部分，从逾期之日起按照逾期贷款罚息利率按日计收利息，直至清偿本息为止。逾期贷款罚息利率为本合同约定的贷款利率水平上加收50%。按罚息利率计收利息的，计息公式为：利息=（本金+应付未付利息）×实际天数×日罚息利率。</span></p><p><span class=\"ql-font-FangSong\"> 违约处理：未按期归还本息即构成违约，原告有权宣布本合同项下贷款本息全部或部分提前到期。</span></p><p><span class=\"ql-font-FangSong\"> 原告发放贷款及被告逾期情况：</span></p><p><span class=\"ql-font-FangSong\"> 合同签订后，原告根据被告的提款申请，共向被告发放5笔贷款，现均已到期，被告未按期还款。该5笔贷款均约定贷款年利率为6.12%，还款方式为按月付息到期还本，每月还款日为28日，其余要素约定情况及截至2019年8月8日被告逾期情况如下表所示：</span></p><p><span class=\"ql-font-FangSong\"> 四、原告的诉讼请求：</span></p><p><span class=\"ql-font-FangSong\"> 1.被告向原告偿还本金104836.96元、利息3817.26元和罚息（暂计至2019年8月8日，本金罚息为5218.59元、利息罚息为269.89元，从2019年8月9日起至实际清偿之日止，罚息以全部剩余本金及应付未付利息为基数，按约定贷款年利率6.12%上浮50%计算）；2.本案诉讼费由被告承担。</span></p><p><span class=\"ql-font-FangSong\"> 五、需要说明的其他事项：</span></p><p><span class=\"ql-font-FangSong\"> 被告未答辩亦未对原告的证据发表质证意见，视为其自行放弃相应的诉讼权利。</span></p><p><span class=\"ql-font-FangSong\"> 裁判理由与结果</span></p><p><span class=\"ql-font-FangSong\"> 本院认为，原告具有开展金融贷款业务的合法资质，本案为签订和履行均在线上完成的金融借款合同纠纷。中国银行番禺支行与关伟斌签订的《中国银行股份有限公司个人网络循环贷款合同》以及关伟斌线上申请贷款时点击确认的并经贷款人最终审批的贷款金额、贷款期限、利率等个性化要素共同构成本案借款合同。中国银行番禺支行提供的证据可以证明关伟斌申请借款所使用的银行卡和电子银行业务是其本人在中国银行开办，申请贷款时使用的上述银行卡绑定手机号码、据此获得的手机交易码以及开通电子银行时获取的安全认证工具，均由关伟斌本人掌握，具有高度私密性，关伟斌亦依约偿还了部分欠款利息，因此在无相反证据的情况下，本院确认案涉借款合同由关伟斌本人签订，是其真实意思表示。该合同无违反法律、行政法规的强制性规定，合法有效，双方均应严格遵照履行。中国银行番禺支行按照约定已向关伟斌发放了贷款本金108000元，已实际履行了放款义务，关伟斌逾期归还贷款本金和利息，已构成违约。现中国银行番禺支行请求关伟斌偿还拖欠的贷款本金104836.96元、相应的利息及按合同约定计算的罚息，于法有据，本院予以支持。</span></p><p><span class=\"ql-font-FangSong\"> 被告经公告送达开庭传票，没有上线参加诉讼，本院依法缺席判决。综上所述，依照《中华人民共和国合同法》第八条、第一百零七条，《中华人民共和国民事诉讼法》第一百四十四条，《最高人民法院关于互联网法院审理案件若干问题的规定》第十八条的规定，判决如下：</span></p><p><span class=\"ql-font-FangSong\"> 一、被告关伟斌于本判决生效之日起十日内向原告中国银行股份有限公司广州番禺支行偿还第一笔贷款本金45000元、利息1728.9元及罚息（罚息自2018年7月29日起以当期未还利息为基数，从每期逾期之日起，按年利率9.18%计至2019年2月9日，自2019年2月10日起，以实际未还本金及实际未还利息为基数，按年利率9.18%计至款项清偿之日止）；</span></p><p><span class=\"ql-font-FangSong\"> 二、被告关伟斌于本判决生效之日起十日内向原告中国银行股份有限公司广州番禺支行偿还第二笔贷款本金20000元、利息690.2元及罚息（罚息自2018年7月29日起以当期未还利息为基数，从每期逾期之日起，按年利率9.18%计至2019年1月17日，自2019年1月18日起，以实际未还本金及实际未还利息为基数，按年利率9.18%计至款项清偿之日止）；</span></p><p><span class=\"ql-font-FangSong\"> 三、被告关伟斌于本判决生效之日起十日内向原告中国银行股份有限公司广州番禺支行偿还第三笔贷款本金8000元、利息39.01元及罚息（罚息自2018年7月28日起以实际未还本金及实际未还利息为基数，按年利率9.18%计至款项清偿之日止）；</span></p><p><span class=\"ql-font-FangSong\"> 四、被告关伟斌于本判决生效之日起十日内向原告中国银行股份有限公司广州番禺支行偿还第四笔贷款本金30000元、利息1341.3元及罚息（罚息自2018年7月29日起以当期未还利息为基数，从每期逾期之日起，按年利率9.18%计至2019年3月18日，自2019年3月19日起，以实际未还本金及实际未还利息为基数，按年利率9.18%计至款项清偿之日止）；</span></p><p><span class=\"ql-font-FangSong\"> 五、被告关伟斌于本判决生效之日起十日内向原告中国银行股份有限公司广州番禺支行偿还第五笔贷款本金1836.96元、利息17.85元及罚息（罚息自2018年9月19日起以实际未还本金及实际未还利息为基数，按年利率9.18%计至款项清偿之日止）。</span></p><p><span class=\"ql-font-FangSong\"> 如未按本判决指定的期间履行给付金钱义务，应当依照《中华人民共和国民事诉讼法》第二百五十三条之规定，加倍支付迟延履行期间的债务利息。</span></p><p><span class=\"ql-font-FangSong\"> 案件受理费1291元，由被告关伟斌负担。</span></p><p><span class=\"ql-font-FangSong\"> 如不服本判决，可以在判决书送达之日起十五日内向本院递交上诉状，并按对方当事人的人数提出副本，上诉于广州市中级人民法院。</span></p><p class=\"ql-align-right\"><span class=\"ql-font-FangSong\">审判员 甘尚钊</span></p><p class=\"ql-align-right\"><span class=\"ql-font-FangSong\">二〇二〇年三月二十四日</span></p><p class=\"ql-align-right\"><br/></p><p class=\"ql-align-right\"><br/></p><p class=\"ql-align-right\"><span class=\"ql-font-FangSong\">法官助理 陈一红</span></p><p class=\"ql-align-right\"><span class=\"ql-font-FangSong\"> 书 记 员 张颖宁</span></p><p class=\"ql-align-center\"><br/></p><p></p>\r\n"
				+ "";
		ItextpdfUtils.generatePDFFromHtmlContent(pdfDO, content, file);
	}
	
	@Test
	public void sign() throws GeneralSecurityException, IOException, DocumentException {
		// 签章设置
		PdfSignDO pdfSignDO = new PdfSignDO();
		pdfSignDO.setLly(100);
		pdfSignDO.setUry(200);
		pdfSignDO.setSignKey("审判员");
		pdfSignDO.setSignReason("电子合同");
		pdfSignDO.setSignLocation("北京北京");
		String signature = Base64.getEncoder().encodeToString(FileUtil.readBytes(FileUtil.getAbsolutePath("pdf/公章3.png")));
		pdfSignDO.setSignature(signature);
		pdfSignDO.setPwd("888888");
		String keystore = Base64.getEncoder().encodeToString(FileUtil.readBytes(FileUtil.getAbsolutePath("pdf/xxx-seal.p12")));
		pdfSignDO.setKeystore(keystore);
		String src="demo\\test.pdf";
		String dest="demo\\test1.pdf";
		ItextpdfUtils.sign(pdfSignDO, src, dest);
	}
}
