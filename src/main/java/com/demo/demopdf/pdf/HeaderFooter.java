package com.demo.demopdf.pdf;

import java.io.IOException;

import org.springframework.util.StringUtils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author 
 * @title 利用PdfPageEventHelper来完成页眉页脚的设置工作
 * @date 
 */
public class HeaderFooter extends PdfPageEventHelper {
	/**
	 * 页眉
	 */
	private String header;
	private static BaseFont baseFont;
    // 页眉字体
    private static Font font=null;
	static {
        try {
            // 中文字体依赖itext得itext-asian包
            baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 168; i++) {
                sb.append("\u00a0");
            }
            font = new Font(HeaderFooter.baseFont, 10, Font.UNDEFINED);
            font.setColor(BaseColor.LIGHT_GRAY);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public HeaderFooter() {
		
	}
	public HeaderFooter(String header) {
		this.header=header;
	}
	@Override
	public void onStartPage(PdfWriter writer, Document document) {
		try {
			//页眉为空时不进行设置
			if(!StringUtils.isEmpty(header)) {
				PdfPTable table = new PdfPTable(3);
				table.setTotalWidth(PageSize.A4.getWidth()); // A4大小
				table.setWidths(new float[]{document.rightMargin(), document.right()-document.left(), document.leftMargin()});
				table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
				table.getDefaultCell().setFixedHeight(20);
				table.addCell(new Phrase());
				PdfPCell cell = new PdfPCell(new Phrase(header,HeaderFooter.font));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorder(Rectangle.BOTTOM);
				cell.setBorderColor(BaseColor.LIGHT_GRAY);
				cell.setPaddingBottom(6f);
				cell.setPaddingLeft(10f);
				table.addCell(cell);
				table.addCell(new Phrase());

				PdfContentByte canvas = writer.getDirectContent();
				table.writeSelectedRows(0, -1,
						((document.right() + document.rightMargin()) - PageSize.A4.getWidth()) / 2, document.top() + 25,
						canvas);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		PdfPTable table = new PdfPTable(3);
		//设置每一列所占的长度
		try {
			table.setWidths(new float[]{document.rightMargin()+15, document.right()-document.left()-30, document.leftMargin()+15});
			table.setTotalWidth(PageSize.A4.getWidth()); // A4大小
			table.getDefaultCell().setFixedHeight(20);
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			table.addCell(new Phrase());
			PdfPCell cell = new PdfPCell(new Phrase("—  "+writer.getPageNumber() + "  —", HeaderFooter.font));
			if(writer.getPageNumber()%2==1) {
				//奇数页在右侧打印页码
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			}else {
				//偶数页在左侧打印页码
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			}
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			table.addCell(new Phrase());
			PdfContentByte canvas = writer.getDirectContent();
			//中间打印页码
			table.writeSelectedRows(0, -1,
					((document.right() + document.rightMargin()) - PageSize.A4.getWidth()) /2, document.bottom(),
					canvas);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	
}