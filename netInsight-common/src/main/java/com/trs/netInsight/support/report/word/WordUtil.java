package com.trs.netInsight.support.report.word;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * word 工具类
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Slf4j
public class WordUtil {

	private static WordUtil wordUtil;

	public synchronized static WordUtil getInstance() {
		if (wordUtil == null) {
			wordUtil = new WordUtil();
		}
		return wordUtil;
	}

	public Boolean mergeDocx(List<String> list, String path) {
		boolean falg = false;
		List<InputStream> inList = new ArrayList<InputStream>();
		;
		try {
			for (int i = 0; i < list.size(); i++) {
				inList.add(new FileInputStream(list.get(i)));
			}
			InputStream inputStream = mergeDocx(inList);
			saveTemplate(inputStream, path);
		} catch (Exception e) {
			log.error("word merge filed!", e);
			return falg;
		} finally {
			inList.forEach(action -> {
				try {
					action.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		falg = true;
		return falg;
	}

	public InputStream mergeDocx(List<InputStream> streams) throws Exception {
		FileInputStream fins = null;
		WordprocessingMLPackage target = null;
		final File generated = File.createTempFile("generated", ".docx");
		int chunkId = 0;
		Iterator<InputStream> it = streams.iterator();
		while (it.hasNext()) {
			InputStream is = it.next();
			if (is != null) {
				if (target == null) {
					// Copy first (master) document
					OutputStream os = new FileOutputStream(generated);
					os.write(IOUtils.toByteArray(is));
					os.close();
					target = WordprocessingMLPackage.load(generated);
				} else {
					// Attach the others (Alternative input parts)

					insertDocx(target.getMainDocumentPart(), IOUtils.toByteArray(is), chunkId++);
				}
			}
		}
		if (target != null) {
			target.save(generated);
			fins = new FileInputStream(generated);
			return fins;
		} else {
			return null;
		}
	}

	// 插入文档
	private void insertDocx(MainDocumentPart main, byte[] bytes, int chunkId) {
		try {
			// Thread.sleep(1000);
			PartName part = new PartName("/part" + chunkId + ".docx");
			AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(part);
			afiPart.setBinaryData(bytes);
			Relationship altChunkRel = main.addTargetPart(afiPart);

			CTAltChunk chunk = Context.getWmlObjectFactory().createCTAltChunk();
			chunk.setId(altChunkRel.getId());

			main.addObject(chunk);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveTemplate(InputStream fis, String toDocPath) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(toDocPath);
			ByteArrayOutputStream bou = new ByteArrayOutputStream();
			int x = 0;
			while ((x = fis.read()) != -1) {
				bou.write(x);
			}
			fos.write(bou.toByteArray());
			bou.close();
			fis.close();
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除单个文件
	 *
	 * @param fileName
	 *            要删除的文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public boolean deleteFiles(List<String> fileNames) {
		File file;
		boolean flag = false;
		for (String fileName : fileNames) {
			file = new File(fileName);
			// 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
			if (file.exists() && file.isFile()) {
				if (file.delete()) {
					log.error("删除单个文件" + fileName + "成功！");
					flag = true;
				} else {
					log.error("删除单个文件" + fileName + "失败！");
					flag = false;
				}
			} else {
				log.error("删除单个文件失败：" + fileName + "不存在！");
				flag = false;
			}
		}
		return flag;
	}
}