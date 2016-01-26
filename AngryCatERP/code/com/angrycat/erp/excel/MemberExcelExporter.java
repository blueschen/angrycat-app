package com.angrycat.erp.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.condition.ConditionFactory;
import com.angrycat.erp.condition.MatchMode;
import com.angrycat.erp.format.FormatListFactory;
import com.angrycat.erp.format.ObjectFormat;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor;
import com.angrycat.erp.service.QueryBaseService;
import com.angrycat.erp.web.WebUtils;

@Service
@Scope("prototype")
public class MemberExcelExporter extends ExcelExporter<Member>{
	@Autowired
	@Qualifier("queryBaseService")
	private QueryBaseService<Member, Member> memberQueryService;
	private String onePosTemplatePath;
	public String getOnePosTemplatePath() {
		return onePosTemplatePath;
	}
	public void setOnePosTemplatePath(String onePosTemplatePath) {
		this.onePosTemplatePath = onePosTemplatePath;
	}

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
	
	public void normal(){
		normal(memberQueryService);
	}
	
	public File onePos(){
		return onePos(memberQueryService);
	}
	
	/**
	 * 將會員轉成OnePos所需要的客戶匯入格式
	 * @param memberQueryService
	 * @return
	 */
	public File onePos(QueryBaseService<Member, Member> memberQueryService){
		String templatePath = StringUtils.isNotBlank(onePosTemplatePath) ? onePosTemplatePath : WebUtils.getWebRootFile("v36 ONE-POS Data Quick Import  快速匯入 - Empty .xls");
		File tempFile = memberQueryService.executeScrollableQuery((rs, sfw)->{
			String tempPath = FileUtils.getTempDirectoryPath() + RandomStringUtils.randomAlphanumeric(8) + ".xls";
			System.out.println("onePos temp file: " + tempPath);
			File file = new File(tempPath);
			try(FileOutputStream fos = new FileOutputStream(file);
				HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(templatePath))){
				
				CellStyle dateStyle = wb.createCellStyle();
				dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("yyyy/MM/dd"));
				
				Sheet sheet = wb.getSheet("clients");
				
				int batchSize = sfw.getBatchSize();
				Session s = sfw.currentSession();
				int currentCount = 0;
				while(rs.next()){
					Member member = (Member)rs.get(0);
					Row cRow = sheet.createRow(++currentCount);
					OnePosInitialExcelAccessor.addClient(cRow, dateStyle, member);
					if(currentCount % batchSize == 0){
						s.flush();
						s.clear();
					}
				}
				wb.write(fos);
				
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
			
			return file;
		});
		return tempFile;
	}
	
	@Override
	public List<ObjectFormat> getFormats() {
		return FormatListFactory.ofMemberForExcelExport();
	}
}
