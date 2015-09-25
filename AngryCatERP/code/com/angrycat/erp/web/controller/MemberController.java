package com.angrycat.erp.web.controller;

import static com.angrycat.erp.condition.ConditionFactory.putBoolean;
import static com.angrycat.erp.condition.ConditionFactory.putInt;
import static com.angrycat.erp.condition.ConditionFactory.putSqlDate;
import static com.angrycat.erp.condition.ConditionFactory.putStr;
import static com.angrycat.erp.condition.ConditionFactory.putStrCaseInsensitive;
import static com.angrycat.erp.condition.MatchMode.ANYWHERE;
import static com.angrycat.erp.condition.MatchMode.START;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.angrycat.erp.businessrule.MemberVipDiscount;
import com.angrycat.erp.businessrule.VipDiscountUseStatus;
import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.excel.ExcelExporter;
import com.angrycat.erp.excel.ExcelImporter;
import com.angrycat.erp.jackson.mixin.MemberIgnoreDetail;
import com.angrycat.erp.log.DataChangeLogger;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.VipDiscountDetail;
import com.angrycat.erp.security.User;
import com.angrycat.erp.service.QueryBaseService;
import com.angrycat.erp.web.WebUtils;
import com.angrycat.erp.web.component.ConditionConfig;



@Controller
@RequestMapping(value="/member")
@Scope("session")
public class MemberController {
	@Autowired
	@Qualifier("queryBaseService")
	private QueryBaseService<Member, Member> memberQueryService;
	
	@Autowired
	@Qualifier("queryBaseService")
	private QueryBaseService<Member, Member> findMemberService;
	
	@Autowired
	private SessionFactoryWrapper sfw;
	
	@Autowired
	private ExcelImporter excelImporter;
	
	@Autowired
	private ExcelExporter excelExporter;
	
	@Autowired
	private MemberVipDiscount discount;
	@Autowired
	private VipDiscountUseStatus useStatus;
	
	@Autowired
	private DataChangeLogger dataChangeLogger;
	
	@PostConstruct
	public void init(){
		memberQueryService.setRootAndInitDefault(Member.class);
		
		memberQueryService
			.addWhere(putStrCaseInsensitive("p.name LIKE :pName", ANYWHERE))
			.addWhere(putInt("p.gender=:pGender"))
			.addWhere(putSqlDate("p.birthday >= :pBirthdayStart"))
			.addWhere(putSqlDate("p.birthday <= :pBirthdayEnd"))
			.addWhere(putStr("p.idNo LIKE :pIdNo", START))
			.addWhere(putStrCaseInsensitive("p.fbNickname LIKE :pFbNickname", ANYWHERE))
			.addWhere(putStr("p.mobile LIKE :pMobile", START))
			.addWhere(putBoolean("p.important = :pImportant"))
		;
		
		findMemberService
			.createFromAlias(Member.class.getName(), "p")
			.createAssociationAlias("left join fetch p.vipDiscountDetails", "details", null)
			.addWhere(putStr("p.id = :pId"));
		
		addUserToComponent();
	}
	
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public String list(Model model){
		model.addAttribute("moduleName", getModule());
		return "member/list";
	}
	
	@RequestMapping(value="/queryAll", 
			method=RequestMethod.GET,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody String queryAll(){
		ConditionConfig<Member> cc = memberQueryService.genCondtitionsAfterExecuteQueryPageable();
		String result = memberIgnoreDetail(cc);
		return result;
	}
	
	private String memberIgnoreDetail(Object obj){
		String result = CommonUtil.parseToJson(obj, Member.class, MemberIgnoreDetail.class);
		return result;
	}
	
	@RequestMapping(value="/queryCondtional",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryCondtional(@RequestBody ConditionConfig<Member> conditionConfig){
		ConditionConfig<Member> cc = memberQueryService.executeQueryPageable(conditionConfig);
		String result = memberIgnoreDetail(cc);
		return result;
	}
	
	@RequestMapping(value="/deleteItems",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String deleteItems(@RequestBody List<String> ids){
		addUserToComponent();
		ConditionConfig<Member> cc = memberQueryService.executeQueryPageableAfterDelete(ids);
		String result = memberIgnoreDetail(cc);
		return result;
	}
	@RequestMapping(value="/add",
			method=RequestMethod.GET)
	public String add(){
		return "member/view";
	}
	@RequestMapping(value="/view/{id}",
			method=RequestMethod.GET)
	public String view(@PathVariable("id")String id, Model model){
		findMemberService.getSimpleExpressions().get("pId").setValue(id);
		List<Member> members = findMemberService.executeQueryList();
		Member member = null;
		if(!members.isEmpty()){
			member = members.get(0);
		}
		if(member!=null){
			useStatus.applyRule(member);
		}
		String result = CommonUtil.parseToJson(member);
		model.addAttribute("member", result);
		return "member/view";
	}
	@RequestMapping(value="/save",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody Member saveOrMerge(@RequestBody Member member){
		addUserToComponent();
		sfw.executeSaveOrUpdate(s->{
			Member oldSnapshot = null;
			if(StringUtils.isBlank(member.getId())){// add
				int detailCount = member.getVipDiscountDetails().size();
				if(detailCount > 0){// 連同明細一起新增
					List<VipDiscountDetail> detail = member.getVipDiscountDetails();
					member.setVipDiscountDetails(new LinkedList<VipDiscountDetail>());
					s.save(member);
					s.flush();
					detail.stream().forEach(d->{
						d.setMemberId(member.getId());
					});
					member.getVipDiscountDetails().addAll(detail);
				}
			}else{// update
				findMemberService.getSimpleExpressions().get("pId").setValue(member.getId());
				List<Member> members = findMemberService.executeQueryList(s);
				
				if(!members.isEmpty()){
					oldSnapshot = members.get(0);// old data detached
					s.evict(oldSnapshot);
					
					Member sessionMember = findMemberService.executeQueryList(s).get(0);
					Iterator<VipDiscountDetail> details = sessionMember.getVipDiscountDetails().iterator();
					boolean deleted = false;
					while(details.hasNext()){// delete vipDiscountDetails in memory
						boolean deleting = true;
						VipDiscountDetail detail = details.next();
						for(VipDiscountDetail d : member.getVipDiscountDetails()){
							if(detail.getId().equals(d.getId())){
								deleting = false;
								break;
							}
						}
						if(deleting){
							details.remove();
							deleted = true;
						}
					}
					if(deleted){// change database to really delete vipDiscountDetails
						s.saveOrUpdate(sessionMember);
						s.flush();
					}
					s.evict(sessionMember);
				}
			}
			s.saveOrUpdate(member);// update member, or add or update detail
			s.flush();
			if(oldSnapshot == null){
				dataChangeLogger.logAdd(member, s);
			}else{
				Collections.reverse(oldSnapshot.getVipDiscountDetails());
				Collections.reverse(member.getVipDiscountDetails());
				dataChangeLogger.logUpdate(oldSnapshot, member, s);
			}
			s.flush();
			Collections.reverse(member.getVipDiscountDetails());
		});
		return member;
	}
	
	@RequestMapping(
			value="/uploadExcel", 
			method=RequestMethod.POST, 
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String uploadExcel(
		@RequestPart("uploadExcelFile") byte[] uploadExcelFile){
		addUserToComponent();
		Map<String, String> msg = excelImporter.persist(uploadExcelFile, dataChangeLogger);
		ConditionConfig<Member> cc = memberQueryService.genCondtitionsAfterExecuteQueryPageable();
		cc.getMsgs().clear();
		cc.getMsgs().putAll(msg);
		String result = memberIgnoreDetail(cc);
		return result;
	}
	
	
	@RequestMapping(value="/copyCondition", method=RequestMethod.POST, produces={"application/xml", "application/json"})
	public @ResponseBody Map<String, String> copyCondition(@RequestBody ConditionConfig<Member> conditionConfig){
		memberQueryService.copyConditionConfig(conditionConfig);
		return Collections.emptyMap();
	}
	
	@RequestMapping(value="/downloadExcel", method={RequestMethod.POST, RequestMethod.GET})
	public void downloadExcel(HttpServletResponse response){
		File tempFile = excelExporter.execute(memberQueryService);
		
		try(FileInputStream fis = new FileInputStream(tempFile);){
			writeExcelToResponse(response, fis, "member.xlsx");
		}catch(Throwable t){
			throw new RuntimeException(t);
		}finally{
			try{
				FileUtils.forceDelete(tempFile);
			}catch(Throwable t){
				throw new RuntimeException(t);
			}
		}
	}
	
	private void writeExcelToResponse(HttpServletResponse response, FileInputStream fis, String fileName) throws Throwable{
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Pragma", "");
		response.setHeader("cache-control", "");
		response.setHeader("Content-Disposition", "attachment; filename="+fileName);
		
		ServletOutputStream sos = response.getOutputStream();
		IOUtils.copy(fis, sos);
		sos.close();
	}
	
	@RequestMapping(value="/updateMemberDiscount", method=RequestMethod.POST)
	public @ResponseBody Member updateMemberDiscount(@RequestBody Member member){
		discount.applyRule(member);
		useStatus.applyRule(member);
		return member;
	}
	
	@RequestMapping(value="/downloadTemplate", method={RequestMethod.GET, RequestMethod.POST})
	public void downloadTemplate(HttpServletResponse response){
		String root = WebUtils.currentServletContext().getRealPath("/");
		String filePath = root + File.separator + "member_sample.xlsx";
		try(FileInputStream fis = new FileInputStream(filePath);){
			writeExcelToResponse(response, fis, "member_template.xlsx");
		}catch(Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	String getModule(){
		return "member";
	}
	
	/**
	 * 新增頁面要開放給未登入者使用，而MemberController本身是Session Scope，<br>
	 * 如果先新增後登錄，和先登錄後新增，兩者造成的差異在於，<br>
	 * 前者初始化的元件裡面不會帶入Session User後者會，<br>
	 * 接下來要考量的，就是新增、修改、匯入、刪除這些動作都要重新檢查一遍。
	 */
	private void addUserToComponent(){
		if(dataChangeLogger.getUser() == null){
			User user = WebUtils.getSessionUser();
			dataChangeLogger.setUser(user);
			memberQueryService.setUser(user);
			findMemberService.setUser(user);
		}
	}
	
}
