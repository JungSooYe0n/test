package com.trs.netInsight.util;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;

import static org.apache.pdfbox.rendering.ImageType.RGB;

public class FileUtil {

	static BASE64Encoder encoder = new sun.misc.BASE64Encoder();

	public static void main(String[] args) {
		getListByJson("123456");
	}


	/**
	 * 写 
	 * @param userId
	 */
	private static void OtJson(String userId){
		List<City> list = new ArrayList<>();
		for(int i=0;i<10;i++){
			City city = new City(UUID.randomUUID().toString(),"89757","XIAOYING");
			list.add(city);
		}
		JSONArray jsonObject = JSONArray.fromObject(list);
		System.out.println(jsonObject);
		FileOutputStream fop = null;
		File file;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
		String format = df.format(new Date());// new Date()为获取当前系统时间
		try {
			String url = "F:/UTIL/"+userId;
			File dir = new File(url);
			if(!dir.exists()){
				dir.mkdir();
			}
			file = new File(url+"/"+format+".json");
			if(!file.exists()){
				file.createNewFile();
			}
			fop = new FileOutputStream(file);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			// get the content in bytes
			byte[] contentInBytes = jsonObject.toString().getBytes();
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 读
	 * @param userId
	 * @return
	 */
	private static List<City> getListByJson(String userId) {
		List<City> list = new ArrayList<>();
		String jsonStr = "";
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
			String format = df.format(new Date());// new Date()为获取当前系统时间
			File jsonFile = ResourceUtils.getFile("F:/UTIL/"+userId+"/"+format+".json");
			FileReader fileReader = new FileReader(jsonFile);
			Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
			int ch = 0;
			StringBuffer sb = new StringBuffer();
			while ((ch = reader.read()) != -1) {
				sb.append((char) ch);
			}
			fileReader.close();
			reader.close();
			jsonStr = sb.toString();
			JSONArray parseArray = JSONArray.fromObject(jsonStr);
			for (Object object : parseArray) {
				JSONObject parseObject = JSONObject.fromObject(String.valueOf(object));
				City city = new City(parseObject.getString("id"), parseObject.getString("city_code"),
						parseObject.getString("city_name"));
				city.setId(parseObject.getString("id"));
				System.err.println(city);
				list.add(city);
			}
			return list;
		} catch (IOException e) {
			return null;
		}
	}


	public static String transferFile(MultipartFile pdfFile) throws IOException{
		if (ObjectUtil.isEmpty(pdfFile)){
			return null;
		}
		String name = pdfFile.getName();

		String originalFileName = null;

		byte[] bytes2 = pdfFile.getOriginalFilename().getBytes("ISO8859-1");
		originalFileName = URLDecoder.decode(new String(bytes2, "UTF-8"), "UTF-8");

		int lastIndexOf = originalFileName.lastIndexOf(".");
		String extendName = ".pdf";
		if (lastIndexOf != -1) {
			extendName = originalFileName.substring(lastIndexOf, originalFileName.length()).toLowerCase();
		}
		// 文件扩展名
		String fileName = name + "_" + System.currentTimeMillis() + extendName;

		// 上传图片
		Environment env = SpringUtil.getBean(Environment.class);
		String pdfFilePath = env.getProperty("pdf.file.path");

		File file = new File(pdfFilePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		pdfFile.transferTo(new File(pdfFilePath, fileName));

		return fileName;
	}

	/**
	    *  将PDF转换成base64编码
	    *  1.使用BufferedInputStream和FileInputStream从File指定的文件中读取内容；
	    *  2.然后建立写入到ByteArrayOutputStream底层输出流对象的缓冲输出流BufferedOutputStream
	    *  3.底层输出流转换成字节数组，然后由BASE64Encoder的对象对流进行编码
	    * */
	public static String getPDFBinary(String filePath) {
		FileInputStream fin =null;
		BufferedInputStream bin =null;
		ByteArrayOutputStream baos = null;
		BufferedOutputStream bout =null;
		try {
			//建立读取文件的文件输出流
			fin = new FileInputStream(new File(filePath));
			//在文件输出流上安装节点流（更大效率读取）
			bin = new BufferedInputStream(fin);
			// 创建一个新的 byte 数组输出流，它具有指定大小的缓冲区容量
			baos = new ByteArrayOutputStream();
			//创建一个新的缓冲输出流，以将数据写入指定的底层输出流
			bout = new BufferedOutputStream(baos);
			byte[] buffer = new byte[1024];
			int len = bin.read(buffer);
			while(len != -1){
			bout.write(buffer, 0, len);
			len = bin.read(buffer);
			}
			//刷新此输出流并强制写出所有缓冲的输出字节，必须这行代码，否则有可能有问题
			bout.flush();
			 byte[] bytes = baos.toByteArray();
			 //sun公司的API
			return encoder.encodeBuffer(bytes).trim();
			 //apache公司的API
			 //return Base64.encodeBase64String(bytes);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
			fin.close();
			bin.close();
			//关闭 ByteArrayOutputStream 无效。此类中的方法在关闭此流后仍可被调用，而不会产生任何 IOException
			//baos.close();
			bout.close();
			} catch (IOException e) {
			e.printStackTrace();
			}
		}
		return null;
 }


	/**
	 * 对字节数组字符串进行Base64解码并生成图片
	 * @param imgStr - 图片二进制字节 imgFilePath-新生成的图片路径
	 * @return
	 */
	public static boolean GenerateImage(String imgStr,String imgFilePath) {
		if (imgStr == null){
			return false;// 图像数据为空
		}
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] b = decoder.decodeBuffer(imgStr);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}
			OutputStream out = new FileOutputStream(imgFilePath);
			out.write(b);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 转换全部的pdf
	 * @param fileAddress 文件地址
	 * @param filename PDF文件名
	 * @param type 图片类型
	 */
	public static void pdf2png(String fileAddress,String filename,String type) {
		// 将pdf装图片 并且自定义图片得格式大小
		File file = new File(fileAddress+"\\"+filename+".pdf");
		try {
			PDDocument doc = PDDocument.load(file);
			PDFRenderer renderer = new PDFRenderer(doc);
			int pageCount = doc.getNumberOfPages();
			//for (int i = 0; i < pageCount; i++) {
				BufferedImage image = renderer.renderImageWithDPI(pageCount-1, 144); // Windows native DPI
				// BufferedImage srcImage = resize(image, 240, 240);//产生缩略图
				ImageIO.write(image, type, new File(fileAddress+"\\"+filename+"_"+(pageCount-1)+"."+type));
			//}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	/**
	 *自由确定起始页和终止页
	 * @param fileAddress 文件地址
	 * @param filename pdf文件名
	 * @param indexOfStart 开始页  开始转换的页码，从0开始
	 * @param indexOfEnd 结束页  停止转换的页码，-1为全部
	 * @param type 图片类型
	 */
	public static void pdf2png(String fileAddress,String filename,int indexOfStart,int indexOfEnd,String type) {
		// 将pdf装图片 并且自定义图片得格式大小
		File file = new File(fileAddress+"\\"+filename+".pdf");
		try {
			PDDocument doc = PDDocument.load(file);
			PDFRenderer renderer = new PDFRenderer(doc);
			int pageCount = doc.getNumberOfPages();
			for (int i = indexOfStart; i < indexOfEnd; i++) {
				BufferedImage image = renderer.renderImageWithDPI(i, 144); // Windows native DPI
				// BufferedImage srcImage = resize(image, 240, 240);//产生缩略图
				ImageIO.write(image, type, new File(fileAddress+"\\"+filename+"_"+(i+1)+"."+type));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @Description pdf转成一张图片
	 * @created 2019年4月19日 下午1:54:13
	 * @param pdfFile
	 * @param outpath
	 */
	public static void pdf2multiImage(String pdfFile, String outpath) {
		try {
			InputStream is = new FileInputStream(pdfFile);
			PDDocument pdf = PDDocument.load(is);
			int actSize  = pdf.getNumberOfPages();
			List<BufferedImage> piclist = new ArrayList<BufferedImage>();
			for (int i = 0; i < actSize; i++) {
				BufferedImage  image = new PDFRenderer(pdf).renderImageWithDPI(i,130, RGB);
				piclist.add(image);
			}
			yPic(piclist, outpath);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 将宽度相同的图片，竖向追加在一起 ##注意：宽度必须相同
	 * @param piclist  文件流数组
	 * @param outPath  输出路径
	 */
	public static void yPic(List<BufferedImage> piclist, String outPath) {// 纵向处理图片
		if (piclist == null || piclist.size() <= 0) {
			System.out.println("图片数组为空!");
			return;
		}
		try {
			int height = 0, // 总高度
					width = 0, // 总宽度
					_height = 0, // 临时的高度 , 或保存偏移高度
					__height = 0, // 临时的高度，主要保存每个高度
					picNum = piclist.size();// 图片的数量
			int[] heightArray = new int[picNum]; // 保存每个文件的高度
			BufferedImage buffer = null; // 保存图片流
			List<int[]> imgRGB = new ArrayList<int[]>(); // 保存所有的图片的RGB
			int[] _imgRGB; // 保存一张图片中的RGB数据
			for (int i = 0; i < picNum; i++) {
				buffer = piclist.get(i);
				heightArray[i] = _height = buffer.getHeight();// 图片高度
				if (i == 0) {
					width = buffer.getWidth();// 图片宽度
				}
				height += _height; // 获取总高度
				_imgRGB = new int[width * _height];// 从图片中读取RGB
				_imgRGB = buffer.getRGB(0, 0, width, _height, _imgRGB, 0, width);
				imgRGB.add(_imgRGB);
			}
			_height = 0; // 设置偏移高度为0
			// 生成新图片
			BufferedImage imageResult = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			for (int i = 0; i < picNum; i++) {
				__height = heightArray[i];
				if (i != 0) _height += __height; // 计算偏移高度
				imageResult.setRGB(0, _height, width, __height, imgRGB.get(i), 0, width); // 写入流中
			}
			File outFile = new File(outPath);
			ImageIO.write(imageResult, "jpg", outFile);// 写图片
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void pdfToImage(String pdfPath) throws IOException {
		try {
			/*图像合并使用参数*/
			// 定义宽度
			int width = 0;
			// 保存一张图片中的RGB数据
			int[] singleImgRGB;
			// 定义高度，后面用于叠加
			int shiftHeight = 0;
			//保存每张图片的像素值
			BufferedImage imageResult = null;
			// 利用PdfBox生成图像
			PDDocument pdDocument = PDDocument.load(new File(pdfPath));
			PDFRenderer renderer = new PDFRenderer(pdDocument);
			/*根据总页数, 按照50页生成一张长图片的逻辑, 进行拆分*/
			// 每50页转成1张图片
			int pageLength = 100;
			// 总计循环的次数
			int totalCount = pdDocument.getNumberOfPages() / pageLength + 1;
			for (int m = 0; m < totalCount; m++) {
				for (int i = 0; i < pageLength; i++) {
					int pageIndex = i + (m * pageLength);
					if (pageIndex == pdDocument.getNumberOfPages()) {
						System.out.println("m = " + m);
						break;
					}
					// 96为图片的dpi，dpi越大，则图片越清晰，图片越大，转换耗费的时间也越多
					BufferedImage image = renderer.renderImageWithDPI(pageIndex, 96, RGB);
					int imageHeight = image.getHeight();
					int imageWidth = image.getWidth();
					if (i == 0) {
						//计算高度和偏移量
						//使用第一张图片宽度;
						width = imageWidth;
						// 保存每页图片的像素值
						// 加个判断：如果m次循环后所剩的图片总数小于pageLength，则图片高度按剩余的张数绘制，否则会出现长图片下面全是黑色的情况
						if ((pdDocument.getNumberOfPages() - m * pageLength) < pageLength) {
							imageResult = new BufferedImage(width, imageHeight * (pdDocument.getNumberOfPages() - m * pageLength), BufferedImage.TYPE_INT_RGB);
						} else {
							imageResult = new BufferedImage(width, imageHeight * pageLength, BufferedImage.TYPE_INT_RGB);
						}
					} else {
						// 将高度不断累加
						shiftHeight += imageHeight;
					}
					singleImgRGB = image.getRGB(0, 0, width, imageHeight, null, 0, width);
					imageResult.setRGB(0, shiftHeight, width, imageHeight, singleImgRGB, 0, width);
				}
				System.out.println("m = " + m);
				File outFile = new File(pdfPath.replace("pdf", "png"));
				System.out.println(outFile.getName());
				// 写图片
				ImageIO.write(imageResult, "png", outFile);
				// 这个很重要，下面会有说明
				shiftHeight = 0;
			}
			pdDocument.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static File multipartFileToFile(MultipartFile file) throws Exception {

		File toFile = null;
		if (file.equals("") || file.getSize() <= 0) {
			file = null;
		} else {
			InputStream ins = null;
			ins = file.getInputStream();
			toFile = new File(file.getOriginalFilename());
			inputStreamToFile(ins, toFile);
			ins.close();
		}
		return toFile;
	}

	//获取流文件
	private static void inputStreamToFile(InputStream ins, File file) {
		try {
			OutputStream os = new FileOutputStream(file);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			ins.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
