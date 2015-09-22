package com.angrycat.erp.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.angrycat.erp.condition.ConditionFactory;
import com.angrycat.erp.condition.MatchMode;
import com.angrycat.erp.format.FormatListFactory;
import com.angrycat.erp.format.ObjectFormat;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.service.QueryBaseService;

@Service
public class ExcelExporter {
	@Autowired
	@Qualifier("queryBaseService")
	private QueryBaseService<Member, Member> memberQueryService;
	
	@PostConstruct
	public void init(){
		memberQueryService.setRootAndInitDefault(Member.class);

		String rootAliasWith = QueryBaseService.DEFAULT_ROOT_ALIAS + ".";
		memberQueryService
			.addWhere(ConditionFactory.putStrCaseInsensitive(rootAliasWith+"name LIKE :pName", MatchMode.ANYWHERE))
			.addWhere(ConditionFactory.putInt(rootAliasWith+"gender=:pGender"))
			.addWhere(ConditionFactory.putSqlDate(rootAliasWith+"birthday >= :pBirthdayStart"))
			.addWhere(ConditionFactory.putSqlDate(rootAliasWith+"birthday <= :pBirthdayEnd"))
			.addWhere(ConditionFactory.putStr(rootAliasWith+"idNo LIKE :pIdNo", MatchMode.START))
			.addWhere(ConditionFactory.putStrCaseInsensitive(rootAliasWith+"fbNickname LIKE :pFbNickname", MatchMode.ANYWHERE))
			.addWhere(ConditionFactory.putStr(rootAliasWith+"mobile LIKE :pMobile", MatchMode.START))
			.addWhere(ConditionFactory.putBoolean(rootAliasWith+"important = :pImportant"))
		;
	}
	
	public void execute(){
		execute(memberQueryService);
	}
	
	public File execute(QueryBaseService<Member, Member> memberQueryService){
		File tempFile = memberQueryService.executeScrollableQuery((rs, sfw)->{
			List<ObjectFormat> formats = FormatListFactory.ofMemberForExcelExport();
			String tempPath = FileUtils.getTempDirectoryPath() + RandomStringUtils.randomAlphanumeric(8) + ".xlsx";
			
			File file = new File(tempPath);
			try(FileOutputStream fos = new FileOutputStream(file);
				SXSSFWorkbook wb = new SXSSFWorkbook(100);){				
				wb.setCompressTempFiles(true);
				
				Sheet sheet = wb.createSheet();
				int rowCount = 0;
				Row firstRow = sheet.createRow(rowCount++);
				for(int i = 0; i < formats.size(); i++){
					ObjectFormat f = formats.get(i);
					Cell cell = firstRow.createCell(i);
					cell.setCellValue(f.getName());
				}
								
				int batchSize = sfw.getBatchSize();
				Session s = sfw.currentSession();
				int currentCount = 0;
				while(rs.next()){
					Object obj = rs.get(0);
					Row row = sheet.createRow(rowCount++);
					for(int i = 0; i < formats.size(); i++){
						ObjectFormat f = formats.get(i);
						String val = f.getValue(obj);
						if(StringUtils.isNotBlank(val)){
							Cell cell = row.createCell(i);
							cell.setCellValue(val);
						}
					}
					if(++currentCount % batchSize == 0){
						s.flush();
						s.clear();
					}
				}
				wb.write(fos);
				boolean tempDeleted = wb.dispose();
				System.out.println("SXSSFWorkbook temp " + (tempDeleted ? "" : "NOT") + " deleted");
			}catch(Throwable t){
				throw new RuntimeException(t);
			}			
			return file;
		});
		
		return tempFile;
		
	}
}
