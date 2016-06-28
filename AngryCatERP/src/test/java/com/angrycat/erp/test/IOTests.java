package com.angrycat.erp.test;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.Test;

import com.angrycat.erp.initialize.StartupWebAppInitializer;
public class IOTests {
	@Test
	public void pathFileName(){
		String sep = File.separator;
		String rootPath = "C:/dts/apache-tomcat-8.0.23";
		String root = rootPath.replace("/", sep);
		String subPath = root + StartupWebAppInitializer.getUploadsTempSubPath();
		String imgPath = subPath + sep + "img";
		long start = System.currentTimeMillis();
		try(Stream<Path> pathStream = Files.walk(Paths.get(imgPath));){
			pathStream.filter(p->Files.isRegularFile(p) && "CM022.jpg".equals(p.toFile().getName())).forEach(p->{System.out.println(p.toFile().getAbsolutePath().replace(root, ""));});
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		long end = System.currentTimeMillis();
		System.out.println("Executing time: " + (end-start) + "毫秒");
	}
	@Test
	public void modelIdFromImg(){
		String imgName = "CM022.jpg";
		System.out.println(imgName.substring(0, imgName.indexOf(".jpg")));
	}
}
